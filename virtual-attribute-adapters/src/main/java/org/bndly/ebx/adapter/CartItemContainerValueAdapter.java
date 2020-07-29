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
import org.bndly.ebx.model.CartItem;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.MixinAttribute;
import org.bndly.schema.model.NamedAttributeHolder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class CartItemContainerValueAdapter implements VirtualAttributeAdapter<MixinAttribute> {

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
	public boolean supports(MixinAttribute attribute, NamedAttributeHolder holder) {
		return SchemaInspectionUtil.isAssignableTo(holder, CartItem.class.getSimpleName()) && attribute.getName().equals("container");
	}

	@Override
	public Object read(MixinAttribute attribute, Record r) {
		CartItem ci = schemaBeanFactory.getSchemaBean(CartItem.class, r);
		Cart c = ci.getCart();
		if (c != null) {
			return schemaBeanFactory.getRecordFromSchemaBean(c);
		}
		return null;
	}

	@Override
	public void write(MixinAttribute attribute, Record r, Object value) {
	}

}
