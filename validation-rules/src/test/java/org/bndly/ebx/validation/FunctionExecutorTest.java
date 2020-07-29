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

import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.common.service.validation.interpreter.CollectionUtil;
import org.bndly.common.service.validation.interpreter.FunctionExecutor;
import org.bndly.ebx.validation.rules.AddressRestBeanRuleBuilderImpl;
import org.bndly.rest.beans.ebx.AddressRestBean;
import org.bndly.rest.beans.ebx.CountryRestBean;
import org.bndly.rest.beans.ebx.SalutationRestBean;
import org.bndly.rest.beans.ebx.TranslatedObjectRestBean;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FunctionExecutorTest {

	@Test
	public void testFunctionExecutor(){
		RulesRestBean rulesWrapper = new AddressRestBeanRuleBuilderImpl().buildRules();
		
		AddressRestBean badAddress = new AddressRestBean();
		badAddress.setAdditionalInfo(generateStringWithLength(101));
		badAddress.setHouseNumber(generateStringWithLength(11));
		FunctionExecutor<AddressRestBean> executor = new FunctionExecutor<AddressRestBean>(badAddress);
		List<RuleFunctionRestBean> rules = CollectionUtil.getItemsAs(rulesWrapper, RuleFunctionRestBean.class);
		int failedRulesCounter = 0;
		for (RuleFunctionRestBean ruleFunctionRestBean : rules) {
			Boolean result = executor.evaluate(ruleFunctionRestBean);
			if(result) {
				System.out.println("rule succeeded. rule: "+ruleFunctionRestBean.getField()+" "+ruleFunctionRestBean.getName());
			} else {
				failedRulesCounter++;
				System.out.println("rule failed. rule: "+ruleFunctionRestBean.getField()+" "+ruleFunctionRestBean.getName());
			}
		}
		
		Assert.assertEquals(failedRulesCounter, rules.size());
		
		
		CountryRestBean country = new CountryRestBean();
		country.setIsoCode2("DE");
		country.setIsoCode3("DEU");
		country.setName(new TranslatedObjectRestBean());

		AddressRestBean goodAddress = new AddressRestBean();
		goodAddress.setAdditionalInfo("bndly");
		goodAddress.setCity("Bonn");
		goodAddress.setCountry(country);
		goodAddress.setFirstName("Thomas");
		goodAddress.setHouseNumber("374");
		goodAddress.setLastName("Heß");
		goodAddress.setPostCode("12345");
                SalutationRestBean salutation = new SalutationRestBean();
                salutation.setName(new TranslatedObjectRestBean());
		goodAddress.setSalutation(salutation);
		goodAddress.setStreet("KöWi");
		
		executor = new FunctionExecutor<AddressRestBean>(goodAddress);
		failedRulesCounter = 0;
		for (RuleFunctionRestBean ruleFunctionRestBean : rules) {
			Boolean result = executor.evaluate(ruleFunctionRestBean);
			if(result) {
				System.out.println("rule succeeded. rule: "+ruleFunctionRestBean.getField()+" "+ruleFunctionRestBean.getName());
			} else {
				failedRulesCounter++;
				System.out.println("rule failed. rule: "+ruleFunctionRestBean.getField()+" "+ruleFunctionRestBean.getName());
			}
		}
		
		Assert.assertEquals(failedRulesCounter, 0);
	}
	
	private String generateStringWithLength(int length) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<length; i++) {
			sb.append("a");
		}
		return sb.toString();
	}
}
