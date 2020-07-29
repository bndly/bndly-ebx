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

import org.bndly.ebx.model.Cart;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.InverseAttribute;
import org.bndly.schema.model.NamedAttributeHolder;
import java.util.ArrayList;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class TaxableItemsValueAdapter implements VirtualAttributeAdapter<InverseAttribute> {

	private static final Logger LOG = LoggerFactory.getLogger(TaxableItemsValueAdapter.class);
	
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().register(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().unregister(this);
	}

	@Override
	public boolean supports(InverseAttribute attribute, NamedAttributeHolder holder) {
		if (!"taxableItems".equals(attribute.getName())) {
			return false;
		}
		return SchemaInspectionUtil.isAssignableTo(holder, Cart.class.getSimpleName()) || SchemaInspectionUtil.isAssignableTo(holder, PurchaseOrder.class.getSimpleName());
	}

	@Override
	public Object read(InverseAttribute attribute, Record r) {
		Object c = schemaBeanFactory.getSchemaBean(r);
		if (Cart.class.isInstance(c)) {
			return getRecordsFrom(((Cart) c).getCartItems());
		} else if (PurchaseOrder.class.isInstance(c)) {
			return getRecordsFrom(((PurchaseOrder) c).getItems());
		} else {
			LOG.warn("could not get taxable items, because the found schemabean was not supported. got record of type {}", r.getType().getName());
			return null;
		}
	}

	private List<Record> getRecordsFrom(List<?> items) {
		if (items == null) {
			return null;
		}
		List<Record> i = new ArrayList<>();
		for (Object cartItem : items) {
			i.add(schemaBeanFactory.getRecordFromSchemaBean(cartItem));
		}
		return i;
	}

	@Override
	public void write(InverseAttribute attribute, Record r, Object value) {
	}
}
