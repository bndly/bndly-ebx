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

import org.bndly.common.service.validation.ORFunctionRestBean;
import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.ebx.validation.RestBeanRelatedRuleBuilder;
import org.bndly.ebx.validation.RuleBuilder;
import org.bndly.rest.beans.ebx.PurchaseOrderRestBean;
import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = {PurchaseOrderRestBeanRuleBuilderImpl.class, RestBeanRelatedRuleBuilder.class}, immediate = true)
public class PurchaseOrderRestBeanRuleBuilderImpl extends AbstractRestBeanRelatedRuleBuilder<PurchaseOrderRestBean> {

	@Reference
	private AddressRestBeanRuleBuilderImpl addressRuleBuilder;

	@Override
	public Class<PurchaseOrderRestBean> getRestModelType() {
		return PurchaseOrderRestBean.class;
	}

	@Override
	protected void injectRules(RulesRestBean rules) {
		rules.add(noteLength());
		rules.add(RuleBuilder.buildNotEmptyRule("shipmentOffer"));
		rules.add(shipmentDate());
		addRulesToOtherRules(deliveryAddress(), rules);
		addRulesToOtherRules(billingAddress(), rules);
		addRulesToOtherRules(address(), rules);
	}

	private RuleFunctionRestBean shipmentDate() {
		return RuleBuilder.buildBeforeNowRule("shipmentDate");
	}

	private List<RuleFunctionRestBean> address() {
		RuleFunctionRestBean rule = addressNotEmpty();
		List<RuleFunctionRestBean> result = RuleBuilder.joinRuleWithRuleBuilder(rule, addressRuleBuilder);
		return result;
	}

	private List<RuleFunctionRestBean> deliveryAddress() {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField("deliveryAddress");
		ORFunctionRestBean orFn = RuleBuilder.createParameterIn(ORFunctionRestBean.class, rule);
		RuleBuilder.addParameterTo(RuleBuilder.empty("deliveryAddress"), orFn);
		List<RuleFunctionRestBean> result = RuleBuilder.joinRuleWithRuleBuilder(rule, orFn, addressRuleBuilder);
		return result;
	}

	private List<RuleFunctionRestBean> billingAddress() {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField("billingAddress");
		ORFunctionRestBean orFn = RuleBuilder.createParameterIn(ORFunctionRestBean.class, rule);
		RuleBuilder.addParameterTo(RuleBuilder.empty("billingAddress"), orFn);
		List<RuleFunctionRestBean> result = RuleBuilder.joinRuleWithRuleBuilder(rule, orFn, addressRuleBuilder);
		return result;
	}

	private RuleFunctionRestBean paymentCategoryNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("paymentCategory");
	}

	private RuleFunctionRestBean addressNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("address");
	}

	private RuleFunctionRestBean statusNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("status");
	}

	private RuleFunctionRestBean noteLength() {
		return RuleBuilder.buildMaxLengthRule("note", 1000);
	}

	public void setAddressRuleBuilder(AddressRestBeanRuleBuilderImpl addressRuleBuilder) {
		this.addressRuleBuilder = addressRuleBuilder;
	}
}
