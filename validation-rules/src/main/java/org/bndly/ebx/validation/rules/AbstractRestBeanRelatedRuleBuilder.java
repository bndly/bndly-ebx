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

import org.bndly.common.service.validation.FunctionReferenceRestBean;
import org.bndly.common.service.validation.FunctionsRestBean;
import org.bndly.common.service.validation.RuleFunctionReferenceRestBean;
import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.ebx.validation.RestBeanRelatedRuleBuilder;
import org.bndly.ebx.validation.RuleBuilderProvider;
import java.util.List;

public abstract class AbstractRestBeanRelatedRuleBuilder<RESTMODEL> implements RestBeanRelatedRuleBuilder<RESTMODEL> {

    @Override
    public boolean appliesForClassName(String className) {
        return getRestModelType().getSimpleName().equals(className);
    }

    @Override
    public RulesRestBean buildRules() {
        RulesRestBean rules = new RulesRestBean();
        injectRules(rules);
	// set the position values for each function because the JSON response of the bean does not carry out the objects in order, if their types are not equal
	injectPositionNumbers(rules);
	
        return rules;
    }

    protected abstract void injectRules(RulesRestBean rules);

    protected void addRulesToOtherRules(List<RuleFunctionRestBean> itemsToAdd, RulesRestBean target) {
	if(itemsToAdd != null) {
	    for (RuleFunctionRestBean ruleFunctionRestBean : itemsToAdd) {
		target.add(ruleFunctionRestBean);
	    }
	}
    }
    
    private void injectPositionNumbers(RulesRestBean rules) {
	List<RuleFunctionReferenceRestBean> items = rules.getItems();
	injectPositionNumbers(items);
    }
    
    private void injectPositionNumbers(List<? extends FunctionReferenceRestBean> items) {
	if(items != null) {
	    int i = 0;
	    for (FunctionReferenceRestBean fn : items) {
		FunctionsRestBean params = fn.getParameters();;
		if(params != null) {
		    injectPositionNumbers(params.getItems());
		}
		fn.setPosition(new Long(i));
		i++;
	    }
	}
    }

    public void setRuleBuilderProvider(RuleBuilderProvider ruleBuilderProvider) {
        if(ruleBuilderProvider != null) {
            ruleBuilderProvider.addRuleBuilder(this);
        }
    }
    
}
