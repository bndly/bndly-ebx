package org.bndly.ebx.validation;

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

import org.bndly.common.service.validation.RuleFunctionReferenceRestBean;
import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.common.service.validation.interpreter.CollectionUtil;
import org.bndly.common.service.validation.interpreter.FunctionExecutor;
import org.bndly.ebx.validation.rules.AbstractRestBeanRelatedRuleBuilder;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractRuleBuilderTest<RESTMODEL, RULEBUILDER extends AbstractRestBeanRelatedRuleBuilder<RESTMODEL>> /*extends AbstractTestNGSpringContextTests*/ {

	protected final RULEBUILDER ruleBuilder;
	RESTMODEL validBean;
	RESTMODEL invalidBean;
	RulesRestBean rules;

	public AbstractRuleBuilderTest(RULEBUILDER ruleBuilder) {
		this.ruleBuilder = ruleBuilder;
	}

	protected abstract Class<RULEBUILDER> getRuleBuilderType();

	protected abstract Class<RESTMODEL> getRestModelType();
	private List<RestBeanRelatedRuleBuilder<?>> builders;

	@BeforeClass
	public void setup() throws IllegalAccessException, InstantiationException {
		Assert.assertNotNull(ruleBuilder);
		rules = ruleBuilder.buildRules();

		invalidBean = getRestModelType().newInstance();
		fillInvalidBean(invalidBean);

		validBean = getRestModelType().newInstance();
		fillValidBean(validBean);
	}

	@Test
	public void testAppliesFor() {
		boolean result = ruleBuilder.appliesForClassName(getRestModelType().getSimpleName());
		Assert.assertTrue(result);
		result = ruleBuilder.appliesForClassName(Object.class.getSimpleName());
		Assert.assertTrue(!result);
	}

	@Test
	public void testInvalidBean() {
		System.out.println("testing invalid bean validation: " + getRestModelType().getSimpleName());
		FunctionExecutor<RESTMODEL> executor = new FunctionExecutor<RESTMODEL>(invalidBean);
		List<RuleFunctionRestBean> rawRules = CollectionUtil.getItemsAs(rules, RuleFunctionRestBean.class);
		int failedRulesCounter = 0;
		for (RuleFunctionRestBean ruleFunctionRestBean : rawRules) {
			Boolean result = executor.evaluate(ruleFunctionRestBean);
			if (!result) {
				failedRulesCounter++;
			}
		}

		Assert.assertEquals(failedRulesCounter, numberOfFailedRules(), "for invalid bean");
	}

	@Test
	public void testValidBean() {
		System.out.println("testing valid bean validation: " + getRestModelType().getSimpleName());
		FunctionExecutor<RESTMODEL> executor = new FunctionExecutor<RESTMODEL>(validBean);
		List<RuleFunctionRestBean> rawRules = CollectionUtil.getItemsAs(rules, RuleFunctionRestBean.class);
		int succeededRulesCounter = 0;
		for (RuleFunctionRestBean ruleFunctionRestBean : rawRules) {
			Boolean result = executor.evaluate(ruleFunctionRestBean);
			if (result) {
				succeededRulesCounter++;
			}
		}

		Assert.assertEquals(succeededRulesCounter, numberOfSucceededRules(), "for valid bean");
	}

	protected abstract void fillValidBean(RESTMODEL validBean);

	protected abstract void fillInvalidBean(RESTMODEL invalidBean);

	protected int numberOfFailedRules() {
		List<RuleFunctionReferenceRestBean> items = rules.getItems();
		if (items == null) {
			return 0;
		}
		return items.size();
	}

	protected int numberOfSucceededRules() {
		List<RuleFunctionReferenceRestBean> items = rules.getItems();
		if (items == null) {
			return 0;
		}
		return items.size();
	}

	protected void validateValidBean(RESTMODEL bean) {
		FunctionExecutor<RESTMODEL> executor = new FunctionExecutor<RESTMODEL>(bean);
		List<RuleFunctionRestBean> rawRules = CollectionUtil.getItemsAs(rules, RuleFunctionRestBean.class);
		for (RuleFunctionRestBean ruleFunctionRestBean : rawRules) {
			Boolean result = executor.evaluate(ruleFunctionRestBean);
			if (!result) {
				result = executor.evaluate(ruleFunctionRestBean);
				Assert.fail("the rule " + ruleFunctionRestBean.getName() + " for the field " + ruleFunctionRestBean.getField() + " failed for a bean that was considered to be valid.");
			}
		}

	}

	protected String buildString(int length) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			sb.append('a');
		}
		return sb.toString();
	}

	public void setBuilders(List<RestBeanRelatedRuleBuilder<?>> builders) {
		this.builders = builders;
	}
}
