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

import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.ShipmentOffer;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.DecimalAttribute;
import org.bndly.schema.model.NamedAttributeHolder;
import java.math.BigDecimal;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = TotalTaxValueAdapter.Configuration.class)
public class TotalTaxValueAdapter implements VirtualAttributeAdapter<DecimalAttribute> {

	@ObjectClassDefinition
	public @interface Configuration {
		@AttributeDefinition(
				name = "Calculation mode",
				description = "The mode defines how the total tax is calculated. CLASSIC means, that the total tax will be calculated by adding up the difference between merchandise value and merchandise value gross and shipment price and shipment price gross. TOTAL means, that the total tax will be calculated by building the difference between 'total' and 'totalGross'.",
				options = {
					@Option(label = "CLASSIC", value = "CLASSIC"),
					@Option(label = "TOTAL", value = "TOTAL")
				}
		)
		String mode() default "CLASSIC";
		
	}
	
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private String mode;
	private AdapterImpl adapter;
	private final AdapterImpl adapterClassic = new AdapterImpl(this) {
		@Override
		public Object read(DecimalAttribute e, Record record) {
			PurchaseOrder order = schemaBeanFactory.getSchemaBean(PurchaseOrder.class, record);
			BigDecimal result = BigDecimal.ZERO;
			BigDecimal mv = order.getMerchandiseValue();
			BigDecimal mvGross = order.getMerchandiseValueGross();
			if (mv != null && mvGross != null) {
				result = result.add(mvGross.subtract(mv));
			}
			ShipmentOffer so = order.getShipmentOffer();
			if (so != null) {
				BigDecimal sp = so.getPrice();
				BigDecimal spGross = so.getPriceGross();
				if (sp != null && spGross != null) {
					result = result.add(spGross.subtract(sp));
				}
			}
			return result;
		}

		@Override
		public void write(DecimalAttribute e, Record record, Object o) {
		}
	};
	
	private final AdapterImpl adapterTotal = new AdapterImpl(this) {
		@Override
		public Object read(DecimalAttribute e, Record record) {
			PurchaseOrder order = schemaBeanFactory.getSchemaBean(PurchaseOrder.class, record);
			
			BigDecimal result = BigDecimal.ZERO;
			BigDecimal total = order.getTotal();
			BigDecimal totalGross = order.getTotalGross();
			
			if (total != null && totalGross != null) {
				result = result.add(totalGross.subtract(total));
			}
			return result;
		}

		@Override
		public void write(DecimalAttribute e, Record record, Object o) {
		}
	};

	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties());
		mode = dictionaryAdapter.getString("mode", "CLASSIC");
		if ("CLASSIC".equals(mode)) {
			adapter = adapterClassic;
		} else {
			adapter = adapterTotal;
		}
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().register(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().unregister(this);
	}

	private static abstract class AdapterImpl implements VirtualAttributeAdapter<DecimalAttribute> {
		private final TotalTaxValueAdapter adapter;

		public AdapterImpl(TotalTaxValueAdapter adapter) {
			this.adapter = adapter;
		}
		
		@Override
		public final boolean supports(DecimalAttribute e, NamedAttributeHolder nah) {
			return adapter.supports(e, nah);
		}
	}

	@Override
	public boolean supports(DecimalAttribute attribute, NamedAttributeHolder holder) {
		if (!"totalTax".equals(attribute.getName())) {
			return false;
		}
		return SchemaInspectionUtil.isAssignableTo(holder, PurchaseOrder.class.getSimpleName());
	}

	@Override
	public Object read(DecimalAttribute attribute, Record r) {
		return adapter.read(attribute, r);
	}

	@Override
	public void write(DecimalAttribute attribute, Record r, Object value) {
		adapter.write(attribute, r, value);
	}

}
