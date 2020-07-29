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

import org.bndly.ebx.model.LineItem;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.DecimalAttribute;
import java.math.BigDecimal;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PurchaseOrderMerchandiseValueGrossAdapter extends AbstractMerchandiseValueGrossAdapter<PurchaseOrder, LineItem> {

	private static final PriceCallback<LineItem> GROSS = new PriceCallback<LineItem>() {
		@Override
		public BigDecimal getPrice(LineItem item) {
			return item.getPriceGross();
		}
	};
	private static final PriceCallback<LineItem> NET = new PriceCallback<LineItem>() {
		@Override
		public BigDecimal getPrice(LineItem item) {
			return item.getPrice();
		}
	};

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	public PurchaseOrderMerchandiseValueGrossAdapter() {
		super(PurchaseOrder.class.getSimpleName());
	}

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().register(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().unregister(this);
	}

	@Override
	protected SchemaBeanFactory getSchemaBeanFactory() {
		return schemaBeanFactory;
	}

	@Override
	protected PriceCallback getPriceCallback(DecimalAttribute attribute, Record record) {
		if (attribute.getName().equals(ATTRIBUTE_MERCHANDISE_VALUE_GROSS)) {
			return GROSS;
		} else {
			return NET;
		}
	}

	@Override
	protected Iterable<LineItem> getMerchandiseItemsFromContainer(PurchaseOrder container) {
		return container.getItems();
	}

}
