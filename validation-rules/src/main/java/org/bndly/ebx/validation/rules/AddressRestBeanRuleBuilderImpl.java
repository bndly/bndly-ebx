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

import org.bndly.common.service.validation.ANDFunctionRestBean;
import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.ebx.validation.RestBeanRelatedRuleBuilder;
import org.bndly.ebx.validation.RuleBuilder;
import org.bndly.rest.beans.ebx.AddressRestBean;
import org.osgi.service.component.annotations.Component;

@Component(service = {AddressRestBeanRuleBuilderImpl.class, RestBeanRelatedRuleBuilder.class}, immediate = true)
public class AddressRestBeanRuleBuilderImpl extends AbstractRestBeanRelatedRuleBuilder<AddressRestBean> {
	@Override
	public Class<AddressRestBean> getRestModelType() {
		return AddressRestBean.class;
	}

	@Override
	protected void injectRules(RulesRestBean rules) {
		rules.add(additionalInfoLength());
		rules.add(cityNotEmpty());
		rules.add(cityLength());
		rules.add(salutationNotEmpty());
		rules.add(firstNameNotEmpty());
		rules.add(firstNameLength());
		rules.add(lastNameNotEmpty());
		rules.add(lastNameLength());
		rules.add(streetNotEmpty());
		rules.add(streetLength());
		rules.add(houseNumberLength());
		rules.add(postCodeLength());
		rules.add(countryNotEmpty());
	}

	// simplified rule generation calls
	protected RuleFunctionRestBean additionalInfoLength() {
		return RuleBuilder.buildMaxLengthRule("additionalInfo", 100);
	}

	protected RuleFunctionRestBean cityNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("city");
	}

	protected RuleFunctionRestBean cityLength() {
		return RuleBuilder.buildMinLengthRule("city", 2);
	}

	protected RuleFunctionRestBean salutationNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("salutation");
	}

	protected RuleFunctionRestBean firstNameNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("firstName");
	}

	protected RuleFunctionRestBean firstNameLength() {
		return RuleBuilder.buildIntervalLengthRule("firstName", 1, 50);
	}

	protected RuleFunctionRestBean lastNameNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("lastName");
	}

	protected RuleFunctionRestBean lastNameLength() {
		return RuleBuilder.buildIntervalLengthRule("lastName", 1, 50);
	}

	protected RuleFunctionRestBean streetNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("street");
	}

	protected RuleFunctionRestBean streetLength() {
		return RuleBuilder.buildMinLengthRule("street", 2);
	}

	protected RuleFunctionRestBean houseNumberLength() {
		return RuleBuilder.buildMaxLengthRule("houseNumber", 10);
	}

	protected RuleFunctionRestBean postCodeLength() {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		String fieldName = "postCode";
		rule.setField(fieldName);
		
		ANDFunctionRestBean and = RuleBuilder.createParameterIn(ANDFunctionRestBean.class, rule);
		RuleBuilder.addParameterTo(RuleBuilder.notEmpty(fieldName), and);
		RuleBuilder.addParameterTo(RuleBuilder.intervallLength(fieldName, 4, 5), and);
		return rule;
	}

	protected RuleFunctionRestBean countryNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("country");
	}

}
