package org.bndly.ebx.validation.rules;

/*-
 * #%L
 * org.bndly.ebx.validation-rules
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

import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.ebx.validation.RestBeanRelatedRuleBuilder;
import org.bndly.ebx.validation.RuleBuilder;
import org.bndly.rest.beans.ebx.ShipmentOfferRestBean;
import org.osgi.service.component.annotations.Component;

@Component(service = RestBeanRelatedRuleBuilder.class, immediate = true)
public class ShipmentOfferRestBeanRuleBuilderImpl extends AbstractRestBeanRelatedRuleBuilder<ShipmentOfferRestBean> {

	@Override
	protected void injectRules(RulesRestBean rules) {
		rules.add(RuleBuilder.buildNotEmptyRule("shipmentMode"));
		rules.add(RuleBuilder.buildNotEmptyRule("shipmentOfferRequest"));
		rules.add(RuleBuilder.buildNotEmptyRule("price"));
		rules.add(RuleBuilder.buildPositiveNumberRule("price"));
		rules.add(RuleBuilder.buildNotEmptyRule("priceGross"));
		rules.add(RuleBuilder.buildPositiveNumberRule("priceGross"));
		rules.add(RuleBuilder.buildNotEmptyRule("createdOn"));
		rules.add(RuleBuilder.buildNotEmptyRule("currency"));
	}

	@Override
	public Class<ShipmentOfferRestBean> getRestModelType() {
		return ShipmentOfferRestBean.class;
	}

}
