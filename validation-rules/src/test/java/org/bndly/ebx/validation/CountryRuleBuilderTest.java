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

import org.bndly.ebx.validation.rules.CountryRestBeanRuleBuilderImpl;
import org.bndly.rest.beans.ebx.CountryRestBean;
import org.bndly.rest.beans.ebx.TranslatedObjectRestBean;

public class CountryRuleBuilderTest extends AbstractRuleBuilderTest<CountryRestBean, CountryRestBeanRuleBuilderImpl>{

	public CountryRuleBuilderTest() {
		super(new CountryRestBeanRuleBuilderImpl());
	}


    @Override
    protected Class<CountryRestBeanRuleBuilderImpl> getRuleBuilderType() {
        return CountryRestBeanRuleBuilderImpl.class;
    }

    @Override
    protected Class<CountryRestBean> getRestModelType() {
        return CountryRestBean.class;
    }

    @Override
    protected void fillValidBean(CountryRestBean validBean) {
        validBean.setIsoCode2(buildString(2));
        validBean.setIsoCode3(buildString(3));
        validBean.setName(new TranslatedObjectRestBean());
    }

    @Override
    protected void fillInvalidBean(CountryRestBean invalidBean) {
        invalidBean.setIsoCode2(buildString(3));
        invalidBean.setIsoCode3(buildString(4));
        invalidBean.setName(new TranslatedObjectRestBean());
    }

    @Override
    protected int numberOfFailedRules() {
        return super.numberOfFailedRules()-3;
    }

}
