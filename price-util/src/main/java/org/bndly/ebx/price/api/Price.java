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
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * The price class can be used to properly calculate prices.
 * This means that the developer has to think about rounding issues, when it is necessary.
 * Furthermore the price class checks, that only prices of the same currency can be added or subtracted.
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class Price implements Comparable<Price> {

	private final BigDecimal value;
	private final Currency javaCurrency;

	static Currency createCurrencyFromCurrencyCode(final String currencyCode) {
		// Currency.getInstance is nullsafe
		final java.util.Currency javaCurrency = java.util.Currency.getInstance(currencyCode);
		final Long fraction = Long.valueOf(javaCurrency.getDefaultFractionDigits());
		return new Currency() {
			@Override
			public String getCode() {
				return currencyCode;
			}

			@Override
			public void setCode(String code) {
			}

			@Override
			public String getSymbol() {
				return javaCurrency.getSymbol();
			}

			@Override
			public void setSymbol(String symbol) {
			}

			@Override
			public Long getDecimalPlaces() {
				return fraction;
			}

			@Override
			public void setDecimalPlaces(Long decimalPlaces) {
			}
		};
	}

	/**
	 * Creates a price instance with the provided value and the provided currency. The rounding mode of the initial value is null.
	 * @param value the numeric value of the price
	 * @param currency the currency of the price
	 * @throws ArithmeticException if rounding is required to initialize the provided value scale for the provided currency
	 * @return a price instance. never null.
	 */
	public static Price valueOf(BigDecimal value, Currency currency) throws ArithmeticException {
		return valueOf(value, currency, null);
	}

	/**
	 * Creates a price instance with the provided value, the provided currency and the provided rounding mode for the initial value.
	 * @param value the numeric value of the price
	 * @param currency the currency of the price
	 * @param roundingMode the rounding mode to apply on the initial value. may be null
	 * @throws ArithmeticException if rounding is required to initialize the provided value scale for the provided currency, but no rounding mode has been provided
	 * @return a price instance. never null.
	 */
	public static Price valueOf(BigDecimal value, Currency currency, RoundingMode roundingMode) {
		if (value == null) {
			throw new IllegalArgumentException("value can not be null");
		}
		if (currency == null) {
			throw new IllegalArgumentException("currency can not be null");
		}
		if (currency.getCode() == null) {
			throw new IllegalArgumentException("currencyCode can not be null");
		}
		return new Price(value, currency, roundingMode);
	}

	/**
	 * Creates a price instance with the provided value and the provided currency code. The rounding mode of the initial value is null.
	 * The currency of the price will be restored by using {@link #createCurrencyFromCurrencyCode(java.lang.String)}.
	 * @param value the numeric value of the price
	 * @param currencyCode the currency code of the currency of the price
	 * @throws ArithmeticException if rounding is required to initialize the provided value scale for the provided currency
	 * @return a price instance. never null.
	 */
	public static Price valueOf(BigDecimal value, String currencyCode) {
		return valueOf(value, currencyCode, null);
	}

	/**
	 * Creates a price instance with the provided value, the provided currency code and the provided rounding mode for the initial value.
	 * The currency of the price will be restored by using {@link #createCurrencyFromCurrencyCode(java.lang.String)}.
	 * @param value the numeric value of the price
	 * @param currencyCode the currency code of the currency of the price
	 * @param roundingMode the rounding mode to apply on the initial value. may be null
	 * @throws ArithmeticException if rounding is required to initialize the provided value scale for the provided currency, but no rounding mode has been provided
	 * @return a price instance. never null.
	 */
	public static Price valueOf(BigDecimal value, String currencyCode, RoundingMode roundingMode) {
		if (value == null) {
			throw new IllegalArgumentException("value can not be null");
		}
		if (currencyCode == null) {
			throw new IllegalArgumentException("currencyCode can not be null");
		}
		return new Price(value, createCurrencyFromCurrencyCode(currencyCode), roundingMode);
	}

	private Price(BigDecimal value, Currency currency, RoundingMode roundingMode) {
		Long fraction = currency.getDecimalPlaces();
		if (fraction != -1 && fraction != null) {
			// NOTE: this method will throw an exception, if rounding would be involved. we assume, that we do not need rounding, because with rounding, we open space for many problems.
			if (roundingMode == null) {
				value = value.setScale(fraction.intValue());
			} else {
				value = value.setScale(fraction.intValue(), roundingMode);
			}
		}
		this.value = value;
		this.javaCurrency = currency;
		if (currency.getCode() == null) {
			throw new IllegalArgumentException("currency code of provided currency is not allowed to be null");
		}
	}

	public final boolean isSameCurrency(Price other) {
		return javaCurrency.getCode().equals(other.javaCurrency.getCode());
	}

	public final String getCurrencyCode() {
		return javaCurrency.getCode();
	}

	public final BigDecimal getValue() {
		return value;
	}

	public final Price add(Price other) {
		assertCurrenciesAreIdentical(other);
		return new Price(value.add(other.value), javaCurrency, null);
	}

	public final Price subtract(Price substrahend) {
		assertCurrenciesAreIdentical(substrahend);
		return new Price(value.subtract(substrahend.value), javaCurrency, null);
	}

	public final Price multiply(BigDecimal factor) {
		return new Price(value.multiply(factor), javaCurrency, null);
	}

	public final Price multiply(BigDecimal factor, RoundingMode roundingMode) {
		Long decimalPlaces = javaCurrency.getDecimalPlaces();
		if (decimalPlaces == -1 || decimalPlaces == null) {
			throw new IllegalStateException("currency instance " + javaCurrency.getCode() + " did not provide a number of decimal places");
		}
		BigDecimal newValue = value.multiply(factor, MathContext.DECIMAL128).setScale(decimalPlaces.intValue(), roundingMode);
		return new Price(newValue, javaCurrency, null);
	}

	public final Price divide(BigDecimal divisor) {
		return new Price(value.divide(divisor), javaCurrency, null);
	}

	public final Price divide(BigDecimal divisor, RoundingMode roundingMode) {
		return new Price(value.divide(divisor, roundingMode), javaCurrency, null);
	}

	private void assertCurrenciesAreIdentical(Price other) {
		if (!isSameCurrency(other)) {
			throw new IllegalArgumentException("prices of different currencies can not be combined");
		}
	}

	@Override
	public int compareTo(Price o) {
		assertCurrenciesAreIdentical(o);
		return value.compareTo(o.value);
	}

	@Override
	public String toString() {
		return value + getCurrencyCode();
	}

}
