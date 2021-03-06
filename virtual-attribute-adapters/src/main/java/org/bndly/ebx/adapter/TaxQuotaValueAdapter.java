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
import org.bndly.ebx.model.TaxQuota;
import org.bndly.ebx.model.TaxQuotaInfo;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.NamedAttributeHolder;
import org.bndly.schema.model.TypeAttribute;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class TaxQuotaValueAdapter implements VirtualAttributeAdapter<TypeAttribute> {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference
	private TaxQuotaCalculationUtil taxQuotaCalculationUtil;

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().register(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().unregister(this);
	}

	@Override
	public boolean supports(TypeAttribute attribute, NamedAttributeHolder holder) {
		return attribute.getName().equals("taxQuotaInfo") && SchemaInspectionUtil.isAssignableTo(holder, Cart.class.getSimpleName());
	}

	@Override
	public Object read(TypeAttribute attribute, Record cartRecord) {
		RecordContext context = cartRecord.getContext();
		Cart cart = schemaBeanFactory.getSchemaBean(Cart.class, cartRecord);
		TaxQuotaInfo info = schemaBeanFactory.getSchemaBean(TaxQuotaInfo.class, context.create(TaxQuotaInfo.class.getSimpleName()));

		Collection<TaxQuota> quotas = taxQuotaCalculationUtil.getTaxQuotasFor(cart, context);
		BigDecimal sideCostTaxRate = taxQuotaCalculationUtil.getSideCostsTaxRate(cart, context);
		if (sideCostTaxRate != null) {
			Iterator<Record> res = schemaBeanFactory.getEngine().getAccessor().query("PICK " + ValueAddedTax.class.getSimpleName() + " v IF v.value=? LIMIT ?", context, null, sideCostTaxRate, 1);
			if (res.hasNext()) {
				ValueAddedTax vat = schemaBeanFactory.getSchemaBean(ValueAddedTax.class, res.next());
				info.setValueAddedTaxForSideCosts(vat);
			}
		}
		if (quotas != null) {
			info.setQuotas(new ArrayList<>(quotas));
		}

		return schemaBeanFactory.getRecordFromSchemaBean(info);
	}

	@Override
	public void write(TypeAttribute attribute, Record r, Object value) {
	}

}
