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

/**
 * A price adapter is a component, that can compute a specific price from a provided price data object and a price context.
 * @author bndly &lt;bndly@cybercon.de&gt;
 * @param <T> the compatible price data object type
 */
public interface PriceAdapter<T extends Price> {
	
	/**
	 * This method computes a specific price from a price data object and a price context
	 * @param price data object of the price
	 * @param priceContext a context for the determination of a specific price
	 * @return a specific price, that has been computed from the provided data object and context
	 */
	ComputedPrice getPrice(T price, PriceContext priceContext);

	/**
	 * This method returns the schema bean interface of the supported price data objects.
	 * @return schema bean interface of supported price data objects
	 */
	Class<T> getApplicableSchemaType();
}
