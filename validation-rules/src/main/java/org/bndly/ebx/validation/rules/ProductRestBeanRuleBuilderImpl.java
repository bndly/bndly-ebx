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
import org.bndly.common.service.validation.IntervallFunctionRestBean;
import org.bndly.common.service.validation.MultiplyFunctionRestBean;
import org.bndly.common.service.validation.ORFunctionRestBean;
import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.common.service.validation.ValueFunctionRestBean;
import org.bndly.ebx.validation.RestBeanRelatedRuleBuilder;
import org.bndly.ebx.validation.RuleBuilder;
import org.bndly.rest.beans.ebx.ProductRestBean;
import org.osgi.service.component.annotations.Component;

@Component(service = RestBeanRelatedRuleBuilder.class, immediate = true)
public class ProductRestBeanRuleBuilderImpl extends AbstractRestBeanRelatedRuleBuilder<ProductRestBean> {

	@Override
	public Class<ProductRestBean> getRestModelType() {
		return ProductRestBean.class;
	}

	@Override
	protected void injectRules(RulesRestBean rules) {
		rules.add(RuleBuilder.buildNotEmptyRule("manufacturer"));
		rules.add(RuleBuilder.buildOptionalMaxLengthRule("model", 100));
		rules.add(price());
		rules.add(name());
		rules.add(stockAdjustment());
		rules.add(gtin());
	}

	private RuleFunctionRestBean gtin() {
		RuleFunctionRestBean r = new RuleFunctionRestBean();
		String fieldName = "gtin";
		r.setField(fieldName);
		ORFunctionRestBean or = RuleBuilder.createParameterIn(ORFunctionRestBean.class, r);
		RuleBuilder.addParameterTo(RuleBuilder.empty(fieldName), or);
		ANDFunctionRestBean and = RuleBuilder.createParameterIn(ANDFunctionRestBean.class, or);
		RuleBuilder.addParameterTo(RuleBuilder.maxLength(fieldName, 14), and);
		RuleBuilder.addParameterTo(RuleBuilder.charset(fieldName, "1234567890"), and);
		return r;
	}

	private RuleFunctionRestBean stockAdjustment() {
		RuleFunctionRestBean r = new RuleFunctionRestBean();
		r.setField("stockAdjustment");
		ORFunctionRestBean or = RuleBuilder.createParameterIn(ORFunctionRestBean.class, r);
		RuleBuilder.addParameterTo(RuleBuilder.empty("stockAdjustment"), or);
		ANDFunctionRestBean and = RuleBuilder.createParameterIn(ANDFunctionRestBean.class, or);
		RuleBuilder.addParameterTo(RuleBuilder.notEmpty("stockAdjustment"), and);
		IntervallFunctionRestBean intervall = RuleBuilder.createParameterIn(IntervallFunctionRestBean.class, and);
		ValueFunctionRestBean v = RuleBuilder.createParameterIn(ValueFunctionRestBean.class, intervall);
		v.setName("value");
		v.setField("stockAdjustment");

		MultiplyFunctionRestBean mul = RuleBuilder.createParameterIn(MultiplyFunctionRestBean.class, intervall);
		mul.setName("left");
		RuleBuilder.createParameterIn(ValueFunctionRestBean.class, mul).setNumeric(-1);
		RuleBuilder.createParameterIn(ValueFunctionRestBean.class, mul).setField("stock");
		return r;
	}

	private RuleFunctionRestBean name() {
		RuleFunctionRestBean r = new RuleFunctionRestBean();
		String field = "name";
		r.setField(field);
		RuleBuilder.addParameterTo(RuleBuilder.notEmpty(field), r);
		RuleBuilder.addParameterTo(RuleBuilder.maxLength(field, 255), r);
		return r;
	}

	private RuleFunctionRestBean price() {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		String field = "priceModel";
		rule.setField(field);
		RuleBuilder.addParameterTo(RuleBuilder.notEmpty(field), rule);
		return rule;
	}
}
