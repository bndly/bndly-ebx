package org.bndly.ebx.resources.listener;

/*-
 * #%L
 * org.bndly.ebx.resources
 * %%
 * Copyright (C) 2013 - 2020 Cybercon GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.bndly.common.reflection.GetterBeanPropertyAccessor;
import org.bndly.schema.api.ObjectReference;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.db.AttributeColumn;
import org.bndly.schema.api.db.TypeTable;
import org.bndly.schema.api.query.Delete;
import org.bndly.schema.api.query.Query;
import org.bndly.schema.api.query.QueryContext;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.api.services.TableRegistry;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Attribute;
import org.bndly.schema.model.InverseAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListSynchronization {

	private final Engine engine;
	private final SchemaBeanFactory schemaBeanFactory;

	public ListSynchronization(SchemaBeanFactory schemaBeanFactory) {
		this.engine = schemaBeanFactory.getEngine();
		this.schemaBeanFactory = schemaBeanFactory;
	}

	private <VALUE> void saveOrUpdate(VALUE existingValue) {
		// simply persist the property
		ActiveRecord ar = (ActiveRecord) existingValue;
		if (ar.getId() != null) {
			ar.update();
		} else {
			ar.persist();
		}
	}

	public static interface TypeSpecificCallback<KEY, ASSOCIATION, VALUE> {

		KEY getKey(ASSOCIATION value);

		KEY getKeyFromValue(VALUE value);

		void applyValuesToNewObject(ASSOCIATION value, VALUE a);
	}

	public <KEY, ASSOCIATION, VALUE> void synchronize(Record record, InverseAttribute localAttribute, Attribute attributeForLookup, String lookedUpAssociationTypeName, TypeSpecificCallback<KEY, ASSOCIATION, VALUE> cb) {
		synchronize(record, localAttribute.getName(), attributeForLookup.getName(), lookedUpAssociationTypeName, cb);
	}

	public <KEY, ASSOCIATION, VALUE> void synchronize(Record record, String localAttributeName, String attributeNameForLookup, String lookedUpAssociationTypeName, TypeSpecificCallback<KEY, ASSOCIATION, VALUE> cb) {
		Object ps = schemaBeanFactory.getSchemaBean(record);
		boolean attributeIsPresent = record.isAttributePresent(localAttributeName);
		if (attributeIsPresent) {
			// get the items from the list by invoking the schema bean getter
			List<VALUE> valuesInRecord = (List<VALUE>) new GetterBeanPropertyAccessor().get(localAttributeName, ps);
			if (valuesInRecord != null && !valuesInRecord.isEmpty()) {
				final Class<?> lookedUpAssociationJavaType = schemaBeanFactory.getTypeBindingForType(lookedUpAssociationTypeName);
				final Map<KEY, ASSOCIATION> existingAssociationsAsMap = new HashMap<>();
				// load all items from the DB (not via the schema bean getter, which may contain a list of items, that are passed in via a REST API call.
				Iterator<Record> tmpRecordsIter = engine.getAccessor().query("PICK " + lookedUpAssociationTypeName + " a IF a." + attributeNameForLookup + ".id=? AND a." + attributeNameForLookup + " TYPED ?", record.getContext(), null, record.getId(), record.getType().getName());
				while (tmpRecordsIter.hasNext()) {
					Record next = tmpRecordsIter.next();
					ASSOCIATION existingAssociationFromDatabase = (ASSOCIATION) schemaBeanFactory.getSchemaBean(next);
					// put the fresh loaded items in a map. the key creation is delegated to a callback object.
					existingAssociationsAsMap.put(cb.getKey(existingAssociationFromDatabase), existingAssociationFromDatabase);
				}

				// make sure that these properties are associated to the property set
				Set<KEY> existingValuesToKeep = new HashSet<>();
				for (VALUE existingValue : valuesInRecord) {
					
					// create a key for the passed in item by delegating the key creation to the callback
					KEY key = cb.getKeyFromValue(existingValue);
					Record newAssocRecord;
					if (key == null) {
						// if no key can be created, then the list item is new
						// create the property
						newAssocRecord = record.getContext().create(lookedUpAssociationTypeName);
						ASSOCIATION newAssoc = (ASSOCIATION) schemaBeanFactory.getSchemaBean(lookedUpAssociationJavaType, newAssocRecord);
						if (!newAssoc.getClass().equals(existingValue.getClass())) {
							saveOrUpdate(existingValue);
						}
						cb.applyValuesToNewObject(newAssoc, existingValue);
						((ActiveRecord) newAssoc).persist();
						key = cb.getKey(newAssoc);
						existingAssociationsAsMap.put(key, newAssoc);
					} else if (existingAssociationsAsMap.containsKey(key)) {
						
						// if the key is already found, then simply save the incoming list item
						saveOrUpdate(existingValue);
					} else {
						// if we have a key, but it is not found in the list, then we create a new list item
						// only create a new assoc
						newAssocRecord = record.getContext().create(lookedUpAssociationTypeName);
						ASSOCIATION newAssoc = (ASSOCIATION) schemaBeanFactory.getSchemaBean(lookedUpAssociationJavaType, newAssocRecord);
						// the callback will shovel all the data from the incoming list item to the newly created list item.
						cb.applyValuesToNewObject(newAssoc, existingValue);
						((ActiveRecord) newAssoc).persist();
						existingAssociationsAsMap.put(key, newAssoc);
					}
					existingValuesToKeep.add(key);
				}
				
				// for all keyed items, that are not tracked as 'to keep', we trigger their deletion
				for (Map.Entry<KEY, ASSOCIATION> entry : existingAssociationsAsMap.entrySet()) {
					ASSOCIATION association = entry.getValue();
					if (!existingValuesToKeep.contains(entry.getKey())) {
						((ActiveRecord) association).delete();
					}
				}
			} else {
				// remove all associations
				QueryContext qc = engine.getQueryContextFactory().buildQueryContext();
				Delete d = qc.delete();
				TableRegistry tr = engine.getTableRegistry();
				TypeTable tt = tr.getTypeTableByType(lookedUpAssociationTypeName);
				List<AttributeColumn> cols = tt.getColumns();
				AttributeColumn col = null;
				for (AttributeColumn attributeColumn : cols) {
					if (attributeColumn.getAttribute().getName().equals(attributeNameForLookup)) {
						col = attributeColumn;
						break;
					}
				}
				if (col != null) {
					d.from(tt.getTableName());
					d.where().expression().criteria().field(col.getColumnName()).equal().value(new ObjectReference(record.getId()));
					Query q = qc.build(record.getContext());
					Transaction tx = engine.getQueryRunner().createTransaction();
					tx.getQueryRunner().run(q);
					tx.commit();
				}
			}
		}
	}
}
