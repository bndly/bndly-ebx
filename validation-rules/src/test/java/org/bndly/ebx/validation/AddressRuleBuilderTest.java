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
import org.bndly.rest.beans.ebx.AddressRestBean;
import org.bndly.rest.beans.ebx.CountryRestBean;
import org.bndly.rest.beans.ebx.SalutationRestBean;
import org.bndly.rest.beans.ebx.TranslatedObjectRestBean;

public class AddressRuleBuilderTest extends AbstractRuleBuilderTest<AddressRestBean, AddressRestBeanRuleBuilderImpl>{

	public AddressRuleBuilderTest() {
		super(new AddressRestBeanRuleBuilderImpl());
	}

    @Override
    protected Class<AddressRestBeanRuleBuilderImpl> getRuleBuilderType() {
        return AddressRestBeanRuleBuilderImpl.class;
    }

    @Override
    protected Class<AddressRestBean> getRestModelType() {
        return AddressRestBean.class;
    }

    @Override
    protected void fillValidBean(AddressRestBean validBean) {
        CountryRestBean country = new CountryRestBean();
		country.setIsoCode2("DE");
		country.setIsoCode3("DEU");
                {
                    country.setName(new TranslatedObjectRestBean());
                }
		validBean.setAdditionalInfo("bndly");
		validBean.setCity("Bonn");
		validBean.setCountry(country);
		validBean.setFirstName("Thomas");
		validBean.setHouseNumber("374");
		validBean.setLastName("Heß");
		validBean.setPostCode("12345");
                SalutationRestBean salutation = new SalutationRestBean();
                {
                    salutation.setName(new TranslatedObjectRestBean());
                }
		validBean.setSalutation(salutation);
		validBean.setStreet("KöWi");
    }

    @Override
    protected void fillInvalidBean(AddressRestBean invalidBean) {
        invalidBean.setAdditionalInfo(buildString(101));
        invalidBean.setHouseNumber(buildString(11));
    }

}
