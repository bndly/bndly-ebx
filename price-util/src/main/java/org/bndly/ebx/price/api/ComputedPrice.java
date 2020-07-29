package org.bndly.ebx.price.api;

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

import org.bndly.ebx.model.Currency;
import org.bndly.ebx.price.api.Price;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ComputedPrice {

	private final BigDecimal taxRate;
	private final Currency currency;
	private final Price netPrice;
	private final Price grossPrice;
	private final Price containingTax;

	public ComputedPrice(BigDecimal netValue, BigDecimal grossValue, BigDecimal taxRate, BigDecimal containingTax, Currency currency, RoundingMode roundingMode) {
		this.netPrice = Price.valueOf(netValue, currency, roundingMode);
		this.grossPrice = Price.valueOf(grossValue, currency, roundingMode);
		this.containingTax = Price.valueOf(containingTax, currency, roundingMode);
		this.currency = currency;
		this.taxRate = taxRate;
	}

	public BigDecimal getNetValue() {
		return netPrice.getValue();
	}

	public Price getNetPrice() {
		return netPrice;
	}

	public BigDecimal getGrossValue() {
		return grossPrice.getValue();
	}

	public Price getGrossPrice() {
		return grossPrice;
	}

	public BigDecimal getContainingTaxValue() {
		return containingTax.getValue();
	}

	public Price getContainingTax() {
		return containingTax;
	}

	public BigDecimal getTaxRate() {
		return taxRate;
	}

	public Currency getCurrency() {
		return currency;
	}
}
