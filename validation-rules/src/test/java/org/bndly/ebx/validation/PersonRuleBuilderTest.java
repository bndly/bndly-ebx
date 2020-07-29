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

import org.bndly.ebx.validation.rules.PersonAddressRestBeanRuleBuilderImpl;
import org.bndly.ebx.validation.rules.PersonRestBeanRuleBuilderImpl;
import org.bndly.rest.beans.ebx.AddressRestBean;
import org.bndly.rest.beans.ebx.CountryReferenceRestBean;
import org.bndly.rest.beans.ebx.CountryRestBean;
import org.bndly.rest.beans.ebx.PersonRestBean;
import org.testng.annotations.Test;

@Test
public class PersonRuleBuilderTest extends AbstractRuleBuilderTest<PersonRestBean, PersonRestBeanRuleBuilderImpl> {

	public PersonRuleBuilderTest() {
		super(new PersonRestBeanRuleBuilderImpl());
		((PersonRestBeanRuleBuilderImpl)ruleBuilder).setPersonAddressRestBeanRuleBuilder(new PersonAddressRestBeanRuleBuilderImpl());
	}

    @Override
    protected Class<PersonRestBeanRuleBuilderImpl> getRuleBuilderType() {
        return PersonRestBeanRuleBuilderImpl.class;
    }

    @Override
    protected Class<PersonRestBean> getRestModelType() {
        return PersonRestBean.class;
    }

    @Override
    protected void fillValidBean(PersonRestBean validBean) {
        validBean.setExternalUserId(buildString(50));
        AddressRestBean shortAddress = new AddressRestBean();
        shortAddress.setFirstName(buildString(50));
        shortAddress.setLastName(buildString(50));
        shortAddress.setAdditionalInfo(buildString(100));
        shortAddress.setHouseNumber(buildString(10));
        CountryReferenceRestBean country = new CountryReferenceRestBean();
        country.setId(1l);
        shortAddress.setCountry(country);
        shortAddress.setPostCode(buildString(5));
        shortAddress.setStreet(buildString(2));
        shortAddress.setCity(buildString(2));
        validBean.setAddress(shortAddress);
    }

    @Override
    protected void fillInvalidBean(PersonRestBean invalidBean) {
        invalidBean.setExternalUserId(buildString(51));
        AddressRestBean shortAddress = new AddressRestBean();
        shortAddress.setFirstName(buildString(51));
        shortAddress.setLastName(buildString(51));
        shortAddress.setAdditionalInfo(buildString(101));
        shortAddress.setHouseNumber(buildString(11));
        invalidBean.setAddress(shortAddress);
    }

    @Test
    public void simplePerson() {
        PersonRestBean person = new PersonRestBean();
        CountryRestBean country = new CountryRestBean();
        country.setId(1l);
        AddressRestBean address = new AddressRestBean();
        address.setCountry(country);
        address.setFirstName("Max");
        address.setLastName("Mustermann");
        person.setAddress(address);
        person.setEmail("ljksdjkndsfdsjkn@cybercon.de");
        person.setExternalUserId("0123456789abcdef01abcdef");

        validateValidBean(person);
    }

    @Override
    protected int numberOfFailedRules() {
        return super.numberOfFailedRules() - 2;
    }
}
