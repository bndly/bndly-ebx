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

import org.bndly.ebx.model.PriceConstraint;
import org.bndly.ebx.price.api.PriceConstraintEvaluator;
import org.bndly.ebx.price.api.PriceContext;
import java.util.Date;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service= PriceConstraintEvaluator.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class StartDatePriceConstraintEvaluator implements PriceConstraintEvaluator {

	@Override
	public boolean constraintApplies(org.bndly.ebx.model.Price price, PriceConstraint priceConstraint, PriceContext priceContext) {
		return checkStartDate(priceConstraint, priceContext.getDate());
	}

	public static boolean checkStartDate(PriceConstraint priceConstraint, Date compareToDate) {
		if (compareToDate == null) {
			return true;
		}
		Date sd = priceConstraint.getStartDate();
		if (sd != null) {
			return compareToDate.getTime() >= sd.getTime();
		}
		return true;
	}

}
