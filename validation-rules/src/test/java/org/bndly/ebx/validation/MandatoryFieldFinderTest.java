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
import org.bndly.common.service.validation.interpreter.MandatoryFieldFinder;
import org.bndly.ebx.validation.rules.AddressRestBeanRuleBuilderImpl;
import org.bndly.rest.beans.ebx.AddressRestBean;
import java.lang.reflect.Field;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MandatoryFieldFinderTest {

    @Test
    public void testFunctionExecutor() {
        RulesRestBean rulesWrapper = new AddressRestBeanRuleBuilderImpl().buildRules();

        List<Field> mandatory = new MandatoryFieldFinder().findMandatoryFields(CollectionUtil.getItemsAs(rulesWrapper, RuleFunctionRestBean.class), AddressRestBean.class);
        Assert.assertNotNull(mandatory);
        Assert.assertEquals(mandatory.size(), 9);
    }
}
