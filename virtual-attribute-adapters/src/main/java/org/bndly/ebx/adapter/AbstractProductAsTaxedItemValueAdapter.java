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

import org.bndly.ebx.model.Price;
import org.bndly.ebx.model.PriceModel;
import org.bndly.ebx.model.Product;
import org.bndly.ebx.model.Purchasable;
import org.bndly.ebx.model.SimplePrice;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.ebx.model.Variant;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.json.beans.StreamingObject;
import org.bndly.schema.model.DecimalAttribute;
import org.bndly.schema.model.NamedAttributeHolder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class AbstractProductAsTaxedItemValueAdapter implements VirtualAttributeAdapter<DecimalAttribute> {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	
	private static final BigDecimal HUNDRED = new BigDecimal("100");

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().register(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().unregister(this);
	}

	@Override
	public boolean supports(DecimalAttribute attribute, NamedAttributeHolder holder) {
		if (!"taxedItemPriceGross".equals(attribute.getName())) {
			return false;
		}
		return SchemaInspectionUtil.isAssignableTo(holder, Product.class.getSimpleName()) || SchemaInspectionUtil.isAssignableTo(holder, Variant.class.getSimpleName());
	}

	@Override
	public Object read(DecimalAttribute attribute, Record r) {
		Purchasable p = (Purchasable) schemaBeanFactory.getSchemaBean(r);
		PriceModel pm = p.getPriceModel();
		if (pm != null) {
			List<Price> prices = pm.getPrices();
			if (prices != null) {
				for (Price price : prices) {
					if (SimplePrice.class.isInstance(price)) {
						SimplePrice sp = (SimplePrice) price;
						ValueAddedTax tm = sp.getTaxModel();
						BigDecimal netValue = sp.getNetValue();
						if (netValue != null && tm != null) {
							BigDecimal rate = tm.getValue();
							if (rate == null) {
								if (StreamingObject.class.isInstance(tm)) {
									Long id = ((StreamingObject) tm).getId();
									if (id != null) {
										RecordContext context = r.getContext();
										tm = schemaBeanFactory.getSchemaBean(ValueAddedTax.class, context.create(ValueAddedTax.class.getSimpleName(), id));
										if (tm != null) {
											rate = tm.getValue();
										}
									}
								}

							}
							if (rate != null) {
								BigDecimal multiplicant = rate.add(HUNDRED).divide(HUNDRED, 2, RoundingMode.HALF_UP);
								return netValue.multiply(multiplicant);
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void write(DecimalAttribute attribute, Record r, Object value) {
	}

}
