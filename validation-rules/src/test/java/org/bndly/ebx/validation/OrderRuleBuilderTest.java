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

import org.bndly.ebx.validation.rules.AddressRestBeanRuleBuilderImpl;
import org.bndly.ebx.validation.rules.PurchaseOrderRestBeanRuleBuilderImpl;
import org.bndly.rest.beans.ebx.PurchaseOrderRestBean;
import org.testng.annotations.Test;

@Test
public class OrderRuleBuilderTest extends AbstractRuleBuilderTest<PurchaseOrderRestBean, PurchaseOrderRestBeanRuleBuilderImpl> {

	public OrderRuleBuilderTest() {
		super(new PurchaseOrderRestBeanRuleBuilderImpl());
		((PurchaseOrderRestBeanRuleBuilderImpl)ruleBuilder).setAddressRuleBuilder(new AddressRestBeanRuleBuilderImpl());
	}

    @Override
    protected Class<PurchaseOrderRestBeanRuleBuilderImpl> getRuleBuilderType() {
        return PurchaseOrderRestBeanRuleBuilderImpl.class;
    }

    @Override
    protected Class<PurchaseOrderRestBean> getRestModelType() {
        return PurchaseOrderRestBean.class;
    }

    @Override
    protected void fillValidBean(PurchaseOrderRestBean validBean) {
        // TODO: actually fill a valid bean
    }

    @Override
    protected void fillInvalidBean(PurchaseOrderRestBean invalidBean) {
    }

    @Override
    protected int numberOfFailedRules() {
        return super.numberOfFailedRules() - 30;
    }

    @Override
    protected int numberOfSucceededRules() {
        return super.numberOfSucceededRules() - 15;
    }
}
