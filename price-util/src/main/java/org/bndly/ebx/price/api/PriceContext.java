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

import java.math.RoundingMode;
import java.util.Date;

/**
 * The price context holds context information to be used while computing a specific price from a price data object.
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public interface PriceContext {
	
	final PriceContext ONE_ITEM = new PriceContext() {
		@Override
		public int getQuantity() {
			return 1;
		}

		@Override
		public RoundingMode getRoundingMode() {
			return null;
		}

		@Override
		public Date getDate() {
			return null;
		}

		@Override
		public String getCountryCode() {
			return null;
		}
		
	};
	
	final PriceContext ONE_ITEM_HALF_UP = new PriceContext() {
		@Override
		public int getQuantity() {
			return 1;
		}

		@Override
		public RoundingMode getRoundingMode() {
			return RoundingMode.HALF_UP;
		}

		@Override
		public Date getDate() {
			return null;
		}

		@Override
		public String getCountryCode() {
			return null;
		}
		
	};
	
	/**
	 * The desired quantity of the priced item.
	 * @return natural per piece quantity of the priced item.
	 */
	int getQuantity();
	
	/**
	 * Returns the rounding mode to use, when rounding is required. Rounding may be required when initializing a {@link Price} instance. This method may return null.
	 * If no rounding mode is available but rounding is required, then exceptions will be thrown by the {@link Price} class.
	 * @return a rounding mode or null
	 */
	RoundingMode getRoundingMode();

	/**
	 * Returns the date for the price. The date may be evaluated by a {@link PriceConstraintEvaluator} in order to validate a price constraint.
	 * @return a date or null
	 */
	Date getDate();

	/**
	 * Returns the country code for the price. The country code may be evaluated by a {@link PriceConstraintEvaluator} in order to validate a price constraint.
	 * @return a country code or null
	 */
	String getCountryCode();
}
