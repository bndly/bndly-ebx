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
import java.math.RoundingMode;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class PriceTest {

	Currency EURO = Price.createCurrencyFromCurrencyCode("EUR");
	Currency USD = Price.createCurrencyFromCurrencyCode("USD");

	@Test
	public void testAdd() {
		Price shouldBeTwo = Price.valueOf(BigDecimal.ONE, EURO).add(Price.valueOf(BigDecimal.ONE, EURO));
		assertThat(shouldBeTwo.getValue()).isEqualTo(new BigDecimal("2.00"));
		shouldBeTwo = Price.valueOf(new BigDecimal("0.99"), EURO).add(Price.valueOf(new BigDecimal("1.01"), EURO));
		assertThat(shouldBeTwo.getValue()).isEqualTo(new BigDecimal("2.00"));
	}

	@Test
	public void testSubstract() {
		Price shouldBeZero = Price.valueOf(BigDecimal.ONE, EURO).subtract(Price.valueOf(BigDecimal.ONE, EURO));
		assertThat(shouldBeZero.getValue()).isEqualTo(new BigDecimal("0.00"));
		shouldBeZero = Price.valueOf(new BigDecimal("0.99"), EURO).add(Price.valueOf(new BigDecimal("-0.99"), EURO));
		assertThat(shouldBeZero.getValue()).isEqualTo(new BigDecimal("0.00"));
	}

	@Test
	public void testMultiplyNoRounding() {
		Price p = Price.valueOf(new BigDecimal("0.99"), EURO).multiply(new BigDecimal("1.00"));
		assertThat(p.getValue()).isEqualTo(new BigDecimal("0.99"));
		p = Price.valueOf(new BigDecimal("0.99"), EURO).multiply(new BigDecimal("2"));
		assertThat(p.getValue()).isEqualTo(new BigDecimal("1.98"));
	}

	@Test
	public void testMultiplyRoundingInvolved() {
		try {
			Price p = Price.valueOf(new BigDecimal("0.99"), EURO).multiply(new BigDecimal("1.19"));
			fail("expected an exception");
		} catch (java.lang.ArithmeticException e) {
			// this is expected
		}
		Price p = Price.valueOf(new BigDecimal("0.99"), EURO).multiply(new BigDecimal("1.19"), RoundingMode.HALF_UP);
		assertThat(p.getValue()).isEqualTo(new BigDecimal("1.18"));
	}

	@Test
	public void testDivideNoRounding() {
		Price p = Price.valueOf(new BigDecimal("0.99"), EURO).divide(new BigDecimal("0.33"));
		assertThat(p.getValue()).isEqualTo(new BigDecimal("3.00"));
	}

	@Test
	public void testDivideRoundingInvolved() {
		try {
			Price.valueOf(BigDecimal.ONE, EURO).divide(new BigDecimal("3"));
			fail("expected an exception");
		} catch (java.lang.ArithmeticException e) {
			// this is expected
		}
		Price p = Price.valueOf(BigDecimal.ONE, EURO).divide(new BigDecimal("3"), RoundingMode.HALF_UP);
		assertThat(p.getValue()).isEqualTo(new BigDecimal("0.33"));
	}

	@Test
	public void testInitPriceNoRounding() {
		Price oneEuro = Price.valueOf(BigDecimal.ONE, EURO);
		assertThat(oneEuro.getValue()).isEqualTo(new BigDecimal("1.00"));
		oneEuro = Price.valueOf(new BigDecimal("1.00"), EURO);
		assertThat(oneEuro.getValue()).isEqualTo(new BigDecimal("1.00"));
		oneEuro = Price.valueOf(new BigDecimal("1.0000"), EURO);
		assertThat(oneEuro.getValue()).isEqualTo(new BigDecimal("1.00"));
	}

	@Test
	public void testInitPriceRoundingInvolved() {
		try {
			Price.valueOf(new BigDecimal("1.001"), EURO);
			fail("expected an exception");
		} catch (java.lang.ArithmeticException e) {
			// this is expected
		}
	}
}
