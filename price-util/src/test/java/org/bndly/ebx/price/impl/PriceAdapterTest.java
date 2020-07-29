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

import org.bndly.ebx.model.Country;
import org.bndly.ebx.price.api.ComputedPrice;
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.Price;
import org.bndly.ebx.model.PriceConstraint;
import org.bndly.ebx.model.PriceModel;
import org.bndly.ebx.model.SimplePrice;
import org.bndly.ebx.model.StaggeredPrice;
import org.bndly.ebx.model.StaggeredPriceItem;
import org.bndly.ebx.model.UserPrice;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.ebx.price.api.PriceContext;
import org.bndly.ebx.price.api.PriceDataInspector;
import org.bndly.ebx.price.exception.NoSuitablePriceAdapterAvailableException;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(MockitoJUnitRunner.class)
public class PriceAdapterTest {
	@Mock
	private PriceModel priceModel;

	@Mock
	private SimplePrice simplePrice;
	
	@Mock
	private SimplePrice simplePriceConstraintDateOpenStart;
	
	@Mock
	private SimplePrice simplePriceConstraintDateOpenEnd;
	
	@Mock
	private SimplePrice simplePriceConstraintDateOpenStartAndOpenEnd;
	
	@Mock
	private SimplePrice simplePriceConstraintDateClosedInterval;
	
	@Mock
	private SimplePrice simplePriceConstraintCountryDE;
	
	@Mock
	private SimplePrice simplePriceConstraintCountryUS;

	@Mock
	private StaggeredPrice staggeredPrice;

	@Mock
	private StaggeredPriceItem item1;

	@Mock
	private StaggeredPriceItem item2;

	@Mock
	private StaggeredPriceItem item3;

	@Mock
	private ValueAddedTax taxModel;

	@Mock
	private Currency currency;

	@Mock
	private PriceContext priceContextNoDate;

	@Mock
	private PriceContext priceContextNow;

	@Mock
	private PriceContext priceContextFuture;
	
	@Mock
	private PriceContext priceContextFuturePlus;
	
	@Mock
	private PriceContext priceContextPast;
	
	@Mock
	private PriceContext priceContextPastPlus;
	
	@Mock
	private PriceContext priceContextDE;
	
	@Mock
	private PriceContext priceContextUS;
	
	@Mock
	private PriceContext priceContextFR;

	@Mock
	private UserPrice userPrice;
	
	private static final Date DATE_NOW = new Date();
	private static final Date DATE_PAST = new Date(DATE_NOW.getTime() - 100000);
	private static final Date DATE_PAST_PLUS = new Date(DATE_NOW.getTime() - 2 * 100000);
	private static final Date DATE_FUTURE = new Date(DATE_NOW.getTime() + 100000);
	private static final Date DATE_FUTURE_PLUS = new Date(DATE_NOW.getTime() + 2 * 100000);
	
	@Mock
	private Country countryDE;

	@Mock
	private Country countryUS;
	
	@Mock
	private PriceConstraint constraintDateOpenStart;
	
	@Mock
	private PriceConstraint constraintDateOpenEnd;
	
	@Mock
	private PriceConstraint constraintDateOpenStartAndOpenEnd;
	
	@Mock
	private PriceConstraint constraintDateClosedInterval;
	
	@Mock
	private PriceConstraint constraintCountryDE;

	@Mock
	private PriceConstraint constraintCountryUS;
	
	private PriceAdapterManagerImpl priceAdapterManager;

	@Before
	public void setup() {
		priceAdapterManager = new PriceAdapterManagerImpl();
		priceAdapterManager.setPriceDataInspector(new PriceDataInspector() {
			@Override
			public String getKey(Price price) {
				return price.getClass().getSimpleName();
			}
		});
		
		Mockito.when(countryDE.getIsoCode2()).thenReturn("DE");
		Mockito.when(countryUS.getIsoCode2()).thenReturn("US");
		
		Mockito.when(constraintDateOpenStart.getEndDate()).thenReturn(DATE_FUTURE);
		Mockito.when(constraintDateOpenEnd.getStartDate()).thenReturn(DATE_PAST);
		Mockito.when(constraintDateOpenStartAndOpenEnd.getStartDate()).thenReturn(DATE_FUTURE);
		Mockito.when(constraintDateOpenStartAndOpenEnd.getEndDate()).thenReturn(DATE_PAST);
		Mockito.when(constraintDateClosedInterval.getStartDate()).thenReturn(DATE_PAST);
		Mockito.when(constraintDateClosedInterval.getEndDate()).thenReturn(DATE_FUTURE);
		Mockito.when(constraintCountryDE.getCountry()).thenReturn(countryDE);
		Mockito.when(constraintCountryUS.getCountry()).thenReturn(countryUS);
		
		Mockito.when(currency.getCode()).thenReturn("EUR");
		Mockito.when(currency.getDecimalPlaces()).thenReturn(2L);

		Mockito.when(taxModel.getValue()).thenReturn(new BigDecimal(19));
		
		Mockito.when(priceContextNoDate.getQuantity()).thenReturn(33);
		Mockito.when(priceContextNoDate.getRoundingMode()).thenReturn(RoundingMode.HALF_UP);
		
		Mockito.when(priceContextNow.getQuantity()).thenReturn(33);
		Mockito.when(priceContextNow.getRoundingMode()).thenReturn(RoundingMode.HALF_UP);
		Mockito.when(priceContextNow.getDate()).thenReturn(DATE_NOW);
		
		Mockito.when(priceContextFuture.getQuantity()).thenReturn(33);
		Mockito.when(priceContextFuture.getRoundingMode()).thenReturn(RoundingMode.HALF_UP);
		Mockito.when(priceContextFuture.getDate()).thenReturn(DATE_FUTURE);
		
		Mockito.when(priceContextFuturePlus.getDate()).thenReturn(DATE_FUTURE_PLUS);
		
		Mockito.when(priceContextPast.getQuantity()).thenReturn(33);
		Mockito.when(priceContextPast.getRoundingMode()).thenReturn(RoundingMode.HALF_UP);
		Mockito.when(priceContextPast.getDate()).thenReturn(DATE_PAST);
		
		Mockito.when(priceContextPastPlus.getDate()).thenReturn(DATE_PAST_PLUS);
		
		Mockito.when(priceContextDE.getCountryCode()).thenReturn("DE");
		Mockito.when(priceContextUS.getCountryCode()).thenReturn("US");
		Mockito.when(priceContextFR.getCountryCode()).thenReturn("FR");

		Mockito.when(simplePrice.getNetValue()).thenReturn(new BigDecimal(5));
		Mockito.when(simplePrice.getTaxModel()).thenReturn(taxModel);
		Mockito.when(simplePrice.getCurrency()).thenReturn(currency);
		
		Mockito.when(simplePriceConstraintDateOpenStart.getNetValue()).thenReturn(new BigDecimal(5));
		Mockito.when(simplePriceConstraintDateOpenStart.getTaxModel()).thenReturn(taxModel);
		Mockito.when(simplePriceConstraintDateOpenStart.getCurrency()).thenReturn(currency);
		Mockito.when(simplePriceConstraintDateOpenStart.getConstraints()).thenReturn(Arrays.asList(constraintDateOpenStart));
		
		Mockito.when(simplePriceConstraintDateOpenEnd.getNetValue()).thenReturn(new BigDecimal(5));
		Mockito.when(simplePriceConstraintDateOpenEnd.getTaxModel()).thenReturn(taxModel);
		Mockito.when(simplePriceConstraintDateOpenEnd.getCurrency()).thenReturn(currency);
		Mockito.when(simplePriceConstraintDateOpenEnd.getConstraints()).thenReturn(Arrays.asList(constraintDateOpenEnd));
		
		Mockito.when(simplePriceConstraintDateOpenStartAndOpenEnd.getNetValue()).thenReturn(new BigDecimal(5));
		Mockito.when(simplePriceConstraintDateOpenStartAndOpenEnd.getTaxModel()).thenReturn(taxModel);
		Mockito.when(simplePriceConstraintDateOpenStartAndOpenEnd.getCurrency()).thenReturn(currency);
		Mockito.when(simplePriceConstraintDateOpenStartAndOpenEnd.getConstraints()).thenReturn(Arrays.asList(constraintDateOpenStartAndOpenEnd));
		
		Mockito.when(simplePriceConstraintDateClosedInterval.getNetValue()).thenReturn(new BigDecimal(5));
		Mockito.when(simplePriceConstraintDateClosedInterval.getTaxModel()).thenReturn(taxModel);
		Mockito.when(simplePriceConstraintDateClosedInterval.getCurrency()).thenReturn(currency);
		Mockito.when(simplePriceConstraintDateClosedInterval.getConstraints()).thenReturn(Arrays.asList(constraintDateClosedInterval));

		Mockito.when(simplePriceConstraintCountryDE.getNetValue()).thenReturn(new BigDecimal(5));
		Mockito.when(simplePriceConstraintCountryDE.getTaxModel()).thenReturn(taxModel);
		Mockito.when(simplePriceConstraintCountryDE.getCurrency()).thenReturn(currency);
		Mockito.when(simplePriceConstraintCountryDE.getConstraints()).thenReturn(Arrays.asList(constraintCountryDE));
		
		Mockito.when(simplePriceConstraintCountryUS.getNetValue()).thenReturn(new BigDecimal(5));
		Mockito.when(simplePriceConstraintCountryUS.getTaxModel()).thenReturn(taxModel);
		Mockito.when(simplePriceConstraintCountryUS.getCurrency()).thenReturn(currency);
		Mockito.when(simplePriceConstraintCountryUS.getConstraints()).thenReturn(Arrays.asList(constraintCountryUS));

		List<StaggeredPriceItem> items = Arrays.asList(item1, item2, item3);

		Mockito.when(item1.getNetValue()).thenReturn(new BigDecimal(40));
		Mockito.when(item1.getMinQuantity()).thenReturn(10L);
		Mockito.when(item1.getMaxQuantity()).thenReturn(null);

		Mockito.when(item2.getNetValue()).thenReturn(new BigDecimal(37.5));
		Mockito.when(item2.getMinQuantity()).thenReturn(20L);
		Mockito.when(item2.getMaxQuantity()).thenReturn(null);

		Mockito.when(item3.getNetValue()).thenReturn(new BigDecimal(35.4));
		Mockito.when(item3.getMinQuantity()).thenReturn(30L);
		Mockito.when(item3.getMaxQuantity()).thenReturn(null);

		Mockito.when(staggeredPrice.getItems()).thenReturn(items);
		Mockito.when(staggeredPrice.getTaxModel()).thenReturn(taxModel);
		Mockito.when(staggeredPrice.getCurrency()).thenReturn(currency);

		List<Price> prices = Arrays.asList(simplePrice, staggeredPrice);

		Mockito.when(priceModel.getPrices()).thenReturn(prices);

		SimplePriceAdapter simplePriceAdapter = new SimplePriceAdapter();
		SimplePriceAdapter simpleSpy = Mockito.spy(simplePriceAdapter);
		Mockito.when(simpleSpy.getApplicableSchemaType()).thenReturn((Class<SimplePrice>) simplePrice.getClass());

		StaggeredPriceAdapter staggeredPriceAdapter = new StaggeredPriceAdapter();
		StaggeredPriceAdapter staggeredSpy = Mockito.spy(staggeredPriceAdapter);
		Mockito.when(staggeredSpy.getApplicableSchemaType()).thenReturn((Class<StaggeredPrice>) staggeredPrice.getClass());

		priceAdapterManager.bind(simpleSpy);
		priceAdapterManager.bind(staggeredSpy);
		
		priceAdapterManager.bindPriceConstraintEvaluator(new StartDatePriceConstraintEvaluator());
		priceAdapterManager.bindPriceConstraintEvaluator(new EndDatePriceConstraintEvaluator());
		priceAdapterManager.bindPriceConstraintEvaluator(new CountryPriceConstraintEvaluator());
	}

	@Test
	public void given_a_simple_price_the_service_should_return_a_computed_price() throws NoSuitablePriceAdapterAvailableException {
		ComputedPrice computedPrice = priceAdapterManager.getPrice(simplePrice, priceContextNow);

		testSimplePrice(computedPrice);
	}

	@Test
	public void given_a_staggered_price_the_service_should_return_a_computed_price() throws NoSuitablePriceAdapterAvailableException {
		ComputedPrice computedPrice = priceAdapterManager.getPrice(staggeredPrice, priceContextNow);

		testStaggeredPrice(computedPrice);
	}

	@Test
	public void given_a_price_model_the_service_should_return_a_list_of_computed_prices() throws NoSuitablePriceAdapterAvailableException {
		List<ComputedPrice> computedPrices = priceAdapterManager.getPrices(priceModel, priceContextNow);

		assertThat(computedPrices.size()).isEqualTo(2);

		ComputedPrice computedSimplePrice = computedPrices.get(0);
		ComputedPrice computedStaggeredPrice2 = computedPrices.get(1);

		testSimplePrice(computedSimplePrice);
		testStaggeredPrice(computedStaggeredPrice2);
	}

	@Test
	public void given_a_user_price_the_service_should_throw_an_exception_because_no_suitable_adapter_was_bound() {
		try {
			priceAdapterManager.getPrice(userPrice, priceContextNow);

			failBecauseExceptionWasNotThrown(NoSuitablePriceAdapterAvailableException.class);
		} catch (NoSuitablePriceAdapterAvailableException e) {
		}
	}

	private List<Price> asList(Price... prices) {
		List<Price> list = new ArrayList<>();
		for (Price price : prices) {
			list.add(price);
		}
		return list;
	}
	
	@Test
	public void given_an_open_start_constraint_the_price_should_be_returned_or_not_returned() throws NoSuitablePriceAdapterAvailableException {
		List<ComputedPrice> r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStart), priceContextNow);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStart), priceContextFuture);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStart), priceContextFuturePlus);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStart), priceContextNoDate);
		assertThat(r.size()).isEqualTo(1);
	}
	
	@Test
	public void given_an_open_end_constraint_the_price_should_be_returned_or_not_returned() throws NoSuitablePriceAdapterAvailableException {
		List<ComputedPrice> r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenEnd), priceContextNow);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenEnd), priceContextPast);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenEnd), priceContextPastPlus);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenEnd), priceContextNoDate);
		assertThat(r.size()).isEqualTo(1);
	}
	
	@Test
	public void given_an_open_start_and_open_end_constraint_the_price_should_be_returned_or_not_returned() throws NoSuitablePriceAdapterAvailableException {
		List<ComputedPrice> r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStartAndOpenEnd), priceContextNow);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStartAndOpenEnd), priceContextPast);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStartAndOpenEnd), priceContextFuture);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStartAndOpenEnd), priceContextPastPlus);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStartAndOpenEnd), priceContextFuturePlus);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateOpenStartAndOpenEnd), priceContextNoDate);
		assertThat(r.size()).isEqualTo(1);
	}
	
	@Test
	public void given_a_closed_interval_constraint_the_price_should_be_returned_or_not_returned() throws NoSuitablePriceAdapterAvailableException {
		List<ComputedPrice> r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateClosedInterval), priceContextNow);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateClosedInterval), priceContextPast);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateClosedInterval), priceContextFuture);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateClosedInterval), priceContextPastPlus);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateClosedInterval), priceContextFuturePlus);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintDateClosedInterval), priceContextNoDate);
		assertThat(r.size()).isEqualTo(1);
	}
	
	@Test
	public void given_a_price_bound_to_a_country_the_price_should_be_returned_or_not_returned() throws NoSuitablePriceAdapterAvailableException {
		List<ComputedPrice> r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintCountryDE, simplePriceConstraintCountryUS), priceContextDE);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintCountryDE, simplePriceConstraintCountryUS), priceContextUS);
		assertThat(r.size()).isEqualTo(1);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintCountryDE, simplePriceConstraintCountryUS), priceContextNow);
		assertThat(r.size()).isEqualTo(2);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintCountryDE, simplePriceConstraintCountryUS), priceContextFR);
		assertThat(r.size()).isEqualTo(0);
		r = priceAdapterManager.getValidPrices(asList(simplePriceConstraintCountryDE, simplePriceConstraintCountryUS), priceContextNoDate);
		assertThat(r.size()).isEqualTo(2);
	}
	
	private void testSimplePrice(ComputedPrice computedPrice) {
		Offset<BigDecimal> offset = Offset.offset(new BigDecimal(0.1));

		assertThat(computedPrice.getNetValue()).isCloseTo(new BigDecimal(165), offset);
		assertThat(computedPrice.getGrossValue()).isCloseTo(new BigDecimal(196.35), offset);
		assertThat(computedPrice.getContainingTaxValue()).isCloseTo(new BigDecimal(31.35), offset);
		assertThat(computedPrice.getTaxRate()).isCloseTo(new BigDecimal(19), offset);
	}

	private void testStaggeredPrice(ComputedPrice computedPrice) {
		Offset<BigDecimal> offset = Offset.offset(new BigDecimal(0.1));

		assertThat(computedPrice.getNetValue()).isCloseTo(new BigDecimal(1168.2), offset);
		assertThat(computedPrice.getGrossValue()).isCloseTo(new BigDecimal(1390.158), offset);
		assertThat(computedPrice.getContainingTaxValue()).isCloseTo(new BigDecimal(221.985), offset);
		assertThat(computedPrice.getTaxRate()).isCloseTo(new BigDecimal(19), offset);
	}
}
