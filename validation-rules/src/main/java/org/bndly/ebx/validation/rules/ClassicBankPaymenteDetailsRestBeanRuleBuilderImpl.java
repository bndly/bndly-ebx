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

import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.ebx.validation.RestBeanRelatedRuleBuilder;
import org.bndly.ebx.validation.RuleBuilder;
import org.bndly.rest.beans.ebx.ClassicBankPaymentDetailsRestBean;
import org.osgi.service.component.annotations.Component;

@Component(service = RestBeanRelatedRuleBuilder.class, immediate = true)
public class ClassicBankPaymenteDetailsRestBeanRuleBuilderImpl extends AbstractRestBeanRelatedRuleBuilder<ClassicBankPaymentDetailsRestBean> {

	@Override
	protected void injectRules(RulesRestBean rules) {
		rules.add(ownerNotEmpty());
		rules.add(ownerLength());
		rules.add(bankNameNotEmpty());
		rules.add(bankNameLength());
		rules.add(bankCodeNotEmpty());
		rules.add(bankCodeLength());
		rules.add(accountNumberNotEmpty());
		rules.add(accountNumberLength());
	}

	@Override
	public Class<ClassicBankPaymentDetailsRestBean> getRestModelType() {
		return ClassicBankPaymentDetailsRestBean.class;
	}

	private RuleFunctionRestBean ownerNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("owner");
	}

	private RuleFunctionRestBean ownerLength() {
		return RuleBuilder.buildMaxLengthRule("owner", 255);
	}

	private RuleFunctionRestBean bankNameNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("bankName");
	}

	private RuleFunctionRestBean bankNameLength() {
		return RuleBuilder.buildMaxLengthRule("bankName", 255);
	}

	private RuleFunctionRestBean bankCodeNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("bankCode");
	}

	private RuleFunctionRestBean bankCodeLength() {
		return RuleBuilder.buildMaxLengthRule("bankCode", 20);
	}

	private RuleFunctionRestBean accountNumberNotEmpty() {
		return RuleBuilder.buildNotEmptyRule("accountNumber");
	}

	private RuleFunctionRestBean accountNumberLength() {
		return RuleBuilder.buildMaxLengthRule("accountNumber", 20);
	}
}
