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
import org.bndly.common.service.validation.FunctionReferenceRestBean;
import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.ebx.validation.RestBeanRelatedRuleBuilder;
import org.bndly.ebx.validation.RuleBuilder;
import org.bndly.rest.beans.ebx.CountryRestBean;
import org.osgi.service.component.annotations.Component;

@Component(service = RestBeanRelatedRuleBuilder.class, immediate = true)
public class CountryRestBeanRuleBuilderImpl extends AbstractRestBeanRelatedRuleBuilder<CountryRestBean> {

	@Override
	protected void injectRules(RulesRestBean rules) {
		rules.add(isoCode2NotEmpty());
		rules.add(isoCode2intervallLength());
		rules.add(isoCode3NotEmpty());
		rules.add(isoCode3intervallLength());
		rules.add(nameNotEmpty());
	}

	@Override
	public Class<CountryRestBean> getRestModelType() {
		return CountryRestBean.class;
	}

	private RuleFunctionRestBean isoCode2NotEmpty() {
		return RuleBuilder.buildNotEmptyRule("isoCode2");
	}

	private RuleFunctionRestBean isoCode2intervallLength() {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		String fieldName = "isoCode2";
		rule.setField(fieldName);
		rule.setName("length");

		FunctionReferenceRestBean and = RuleBuilder.createParameterIn(ANDFunctionRestBean.class, rule);
		RuleBuilder.addParameterTo(RuleBuilder.notEmpty(fieldName), and);
		RuleBuilder.addParameterTo(RuleBuilder.intervallLength(fieldName, 1, 2), and);
		return rule;
	}

	private RuleFunctionRestBean isoCode3NotEmpty() {
		return RuleBuilder.buildNotEmptyRule("isoCode3");
	}

	private RuleFunctionRestBean isoCode3intervallLength() {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		String fieldName = "isoCode3";
		rule.setField(fieldName);
		rule.setName("length");

		FunctionReferenceRestBean and = RuleBuilder.createParameterIn(ANDFunctionRestBean.class, rule);
		RuleBuilder.addParameterTo(RuleBuilder.notEmpty(fieldName), and);
		RuleBuilder.addParameterTo(RuleBuilder.intervallLength(fieldName, 1, 3), and);

		return rule;
	}

	private RuleFunctionRestBean nameNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("name");
	}
}
