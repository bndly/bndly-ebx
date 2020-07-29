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

import org.bndly.ebx.model.Property;
import org.bndly.ebx.model.PropertySet;
import org.bndly.ebx.model.PropertySetAssociation;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = {PropertySetPersistListener.class, PersistListener.class, MergeListener.class}, immediate = true)
public class PropertySetPersistListener implements PersistListener, MergeListener {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().addListener(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().removeListener(this);
	}

	@Override
	public void onMerge(Record record) {
		synchronizeProperties(record);
	}

	@Override
	public void onPersist(Record record) {
		synchronizeProperties(record);
	}

	private void synchronizeProperties(final Record record) {
		if (PropertySet.class.getSimpleName().equals(record.getType().getName())) {
			new ListSynchronization(schemaBeanFactory).synchronize(record, "properties", "propertySet", PropertySetAssociation.class.getSimpleName(), new ListSynchronization.TypeSpecificCallback<Long, PropertySetAssociation, Property>() {
				@Override
				public Long getKey(PropertySetAssociation value) {
					return ((ActiveRecord) value.getProperty()).getId();
				}

				@Override
				public Long getKeyFromValue(Property value) {
					return ((ActiveRecord) value).getId();
				}

				@Override
				public void applyValuesToNewObject(PropertySetAssociation association, Property value) {
					PropertySet ps = schemaBeanFactory.getSchemaBean(PropertySet.class, record);
					association.setProperty(value);
					association.setPropertySet(ps);
				}
			});
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}
}
