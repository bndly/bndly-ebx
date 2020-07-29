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
import org.bndly.ebx.model.StaggeredPrice;
import org.bndly.ebx.model.StaggeredPriceItem;
import org.bndly.ebx.price.PriceUtil;
import org.bndly.ebx.price.api.PriceAdapter;
import org.bndly.ebx.price.api.PriceContext;
import static org.bndly.ebx.price.impl.SimplePriceAdapter.compute;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

@Component(service = PriceAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class StaggeredPriceAdapter implements PriceAdapter<StaggeredPrice> {
	@Override
	public ComputedPrice getPrice(StaggeredPrice price, PriceContext priceContext) {
		StaggeredPriceItem staggeredPriceItem = PriceUtil.getLowestStaggeredPriceItemForQuantity(priceContext.getQuantity(), price);

		if (staggeredPriceItem == null) {
			return null;
		}

		return compute(staggeredPriceItem.getNetValue(), priceContext.getQuantity(), price.getTaxModel(), price.getCurrency(), priceContext.getRoundingMode());
	}

	@Override
	public Class<StaggeredPrice> getApplicableSchemaType() {
		return StaggeredPrice.class;
	}
}
