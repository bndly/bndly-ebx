package org.bndly.ebx.price.impl;

/*-
 * #%L
 * org.bndly.ebx.price-util
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

import org.bndly.ebx.price.api.ComputedPrice;
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.SimplePrice;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.ebx.price.api.PriceAdapter;
import org.bndly.ebx.price.api.PriceContext;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

@Component(service = PriceAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SimplePriceAdapter implements PriceAdapter<SimplePrice> {

	static ComputedPrice compute(BigDecimal netValue, int quantity, ValueAddedTax taxModel, Currency currency, RoundingMode roundingMode) {
		BigDecimal totalNetValue = netValue.multiply(new BigDecimal(quantity));
		BigDecimal taxRate = taxModel.getValue();

		BigDecimal containingTax = totalNetValue.multiply(taxRate).divide(new BigDecimal(100), MathContext.DECIMAL128);
		BigDecimal totalGrossValue = totalNetValue.add(containingTax);

		return new ComputedPrice(totalNetValue, totalGrossValue, taxRate, containingTax, currency, roundingMode);
	}
	
	@Override
	public ComputedPrice getPrice(SimplePrice price, PriceContext priceContext) {
		return compute(price.getNetValue(), priceContext.getQuantity(), price.getTaxModel(), price.getCurrency(), priceContext.getRoundingMode());
	}

	@Override
	public Class<SimplePrice> getApplicableSchemaType() {
		return SimplePrice.class;
	}
}
