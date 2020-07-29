package org.bndly.ebx.adapter;

/*-
 * #%L
 * org.bndly.ebx.virtual-attribute-adapters
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

import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.BooleanAttribute;
import org.bndly.schema.model.NamedAttributeHolder;
import java.util.Iterator;

public abstract class AbstractPurchaseOrderHasSomethingValueAdapter implements VirtualAttributeAdapter<BooleanAttribute> {

	protected abstract SchemaBeanFactory getSchemaBeanFactory();

	private final String attributeName;
	private final Class<?> lookedUpObjectType;

	public AbstractPurchaseOrderHasSomethingValueAdapter(String attributeName, Class<?> lookedUpObjectType) {
		this.attributeName = attributeName;
		this.lookedUpObjectType = lookedUpObjectType;
	}

	@Override
	public boolean supports(BooleanAttribute attribute, NamedAttributeHolder holder) {
		if (!attributeName.equals(attribute.getName())) {
			return false;
		}
		return SchemaInspectionUtil.isAssignableTo(holder, PurchaseOrder.class.getSimpleName());
	}

	@Override
	public Object read(BooleanAttribute attribute, Record r) {
		if (r.getId() == null) {
			return false;
		}
		Accessor accessor = getSchemaBeanFactory().getEngine().getAccessor();
		// we can not use count here, because we want to link the cache entries of the purchase order to the other object.
		// if we used count, we would not know, which other objects shall be linked to the purchase order
		Iterator<Record> theOtherRecords = accessor.query("PICK " + lookedUpObjectType.getSimpleName() + " t IF t.purchaseOrder.id=? AND t.purchaseOrder TYPED ?", r.getContext(), null, r.getId(), r.getType().getName());
		if (theOtherRecords.hasNext()) {
			return true;
		}
		return false;
	}

	@Override
	public void write(BooleanAttribute attribute, Record r, Object value) {
	}

}
