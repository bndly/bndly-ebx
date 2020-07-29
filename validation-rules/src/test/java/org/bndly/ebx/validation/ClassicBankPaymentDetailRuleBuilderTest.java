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

import org.bndly.ebx.validation.rules.ClassicBankPaymenteDetailsRestBeanRuleBuilderImpl;
import org.bndly.rest.beans.ebx.ClassicBankPaymentDetailsRestBean;

public class ClassicBankPaymentDetailRuleBuilderTest extends AbstractRuleBuilderTest<ClassicBankPaymentDetailsRestBean, ClassicBankPaymenteDetailsRestBeanRuleBuilderImpl>{

	public ClassicBankPaymentDetailRuleBuilderTest() {
		super(new ClassicBankPaymenteDetailsRestBeanRuleBuilderImpl());
	}


    @Override
    protected Class<ClassicBankPaymenteDetailsRestBeanRuleBuilderImpl> getRuleBuilderType() {
        return ClassicBankPaymenteDetailsRestBeanRuleBuilderImpl.class;
    }

    @Override
    protected Class<ClassicBankPaymentDetailsRestBean> getRestModelType() {
        return ClassicBankPaymentDetailsRestBean.class;
    }

    @Override
    protected void fillValidBean(ClassicBankPaymentDetailsRestBean validBean) {
        validBean.setOwner(buildString(255));
        validBean.setBankName(buildString(255));
        validBean.setAccountNumber(buildString(20));
        validBean.setBankCode(buildString(20));
    }

    @Override
    protected void fillInvalidBean(ClassicBankPaymentDetailsRestBean invalidBean) {
        invalidBean.setOwner(buildString(256));
        invalidBean.setBankName(buildString(256));
        invalidBean.setAccountNumber(buildString(21));
        invalidBean.setBankCode(buildString(21));
    }

    @Override
    protected int numberOfFailedRules() {
        return super.numberOfFailedRules()-4;
    }

}
