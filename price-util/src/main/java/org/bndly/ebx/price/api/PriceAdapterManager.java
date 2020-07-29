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

import org.bndly.ebx.model.Price;
import org.bndly.ebx.model.PriceModel;
import org.bndly.ebx.price.exception.NoSuitablePriceAdapterAvailableException;

import java.util.List;

/**
 * The price adapter manager bundles all available {@link PriceAdapter} instances. The manager is able to determine specific prices by delegating to the individual adapters.
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public interface PriceAdapterManager {
	
	/**
	 * This method looks up a suitable adapter for the provided price data object instance and then calls 
	 * {@link PriceAdapter#getPrice(org.bndly.ebx.model.Price, org.bndly.ebx.price.api.PriceContext)}.
	 * @param price the price data object
	 * @param priceContext the context for getting a specific price
	 * @return a computed specific price. never null.
	 * @throws NoSuitablePriceAdapterAvailableException 
	 */
	ComputedPrice getPrice(Price price, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException;

	/**
	 * This method iterates over all price data objects in the provided price model and calls the individual price adapters to get specific computed prices.
	 * @param priceModel the price model that holds the available price data objects
	 * @param priceContext the context for getting a specific price
	 * @return a list of computed specific prices. never null
	 * @throws NoSuitablePriceAdapterAvailableException 
	 */
	List<ComputedPrice> getPrices(PriceModel priceModel, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException;
	
	/**
	 * This method iterates over all price data objects in the provided price model and calls the individual price adapters to get specific computed prices.
	 * Furthermore this method will evaluate the constraints on the price. Only those prices with no violated constraints will be returned.
	 * @param priceModel the price model that holds the available price data objects
	 * @param priceContext the context for getting a specific price
	 * @return a list of computed specific prices. never null
	 * @throws NoSuitablePriceAdapterAvailableException 
	 */
	List<ComputedPrice> getValidPrices(PriceModel priceModel, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException;
	
	/**
	 * This method iterates over all price data objects and calls the individual price adapters to get specific computed prices.
	 * Furthermore this method will evaluate the constraints on the price. Only those prices with no violated constraints will be returned.
	 * @param prices the price data objects
	 * @param priceContext the context for getting a specific price
	 * @return a list of computed specific prices. never null
	 * @throws NoSuitablePriceAdapterAvailableException 
	 */
	List<ComputedPrice> getValidPrices(Iterable<Price> prices, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException;
}
