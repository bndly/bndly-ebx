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

import org.bndly.common.service.validation.ANDFunctionRestBean;
import org.bndly.common.service.validation.CharsetFunctionRestBean;
import org.bndly.common.service.validation.EmptyFunctionRestBean;
import org.bndly.common.service.validation.EqualsFunctionRestBean;
import org.bndly.common.service.validation.FieldRelated;
import org.bndly.common.service.validation.FunctionReferenceRestBean;
import org.bndly.common.service.validation.FunctionsRestBean;
import org.bndly.common.service.validation.IntervallFunctionRestBean;
import org.bndly.common.service.validation.MaxSizeFunctionRestBean;
import org.bndly.common.service.validation.NotFunctionRestBean;
import org.bndly.common.service.validation.ORFunctionRestBean;
import org.bndly.common.service.validation.PrecisionFunctionRestBean;
import org.bndly.common.service.validation.RuleFunctionRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.common.service.validation.ValueFunctionRestBean;
import org.bndly.common.service.validation.interpreter.CollectionUtil;
import org.bndly.rest.common.beans.ListRestBean;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RuleBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RuleBuilder.class);
	
	/**
	 * this class is just a utility class, that should not be instantiated
	 * outside the AbstractRestBeanRelatedRuleBuilder
	 *
	 * @author bndly &lt;bndly@cybercon.de&gt;
	 *
	 */
	protected static final class MarkerFunction extends FunctionReferenceRestBean {

		@Override
		public void setParameters(FunctionsRestBean parameters) {
		}

		@Override
		public FunctionsRestBean getParameters() {
			return null;
		}
	}

	private RuleBuilder() {
	}

	/**
	 * &lt;not&gt;&lt;empty&gt;&lt;value&gt;&lt;field&gt;fieldName&lt;/field&gt;&lt;/value&gt;&lt;/empty&gt;&lt;/not&gt;
	 *
	 * @param fieldName
	 * @return
	 */
	public static FunctionReferenceRestBean notEmpty(String fieldName) {
		FunctionReferenceRestBean not = new NotFunctionRestBean();
		addParameterTo(empty(fieldName), not);
		return not;
	}

	public static FunctionReferenceRestBean empty(String fieldName) {
		FunctionReferenceRestBean result = new EmptyFunctionRestBean();
		ValueFunctionRestBean value = createParameterIn(ValueFunctionRestBean.class, result);
		value.setField(fieldName);

		return result;
	}

	public static FunctionReferenceRestBean minLength(String fieldName, int length) {
		FunctionReferenceRestBean min = new NotFunctionRestBean();
		addParameterTo(maxLength(fieldName, length - 1), min);
		return min;
	}

	public static FunctionReferenceRestBean maxLength(String fieldName, int length) {
		FunctionReferenceRestBean result = new MaxSizeFunctionRestBean();
		ValueFunctionRestBean value = createParameterIn(ValueFunctionRestBean.class, result);
		value.setName("value");
		value.setField(fieldName);
		value = createParameterIn(ValueFunctionRestBean.class, result);
		value.setName("size");
		value.setNumeric(new BigDecimal(length));
		return result;
	}

	public static FunctionReferenceRestBean intervallLength(String fieldName, int minLength, int maxLength) {
		FunctionReferenceRestBean and = new ANDFunctionRestBean();
		addParameterTo(minLength(fieldName, minLength), and);
		addParameterTo(maxLength(fieldName, maxLength), and);

		return and;
	}

	public static FunctionReferenceRestBean greaterThan(String fieldName, int minValue) {
		IntervallFunctionRestBean intervall = new IntervallFunctionRestBean();
		ValueFunctionRestBean v = createParameterIn(ValueFunctionRestBean.class, intervall);
		v.setField(fieldName);
		v.setName("value");

		ValueFunctionRestBean r = createParameterIn(ValueFunctionRestBean.class, intervall);
		r.setNumeric(minValue);
		r.setName("left");
		return intervall;
	}

	public static FunctionReferenceRestBean lowerThan(String fieldName, int maxValue) {
		IntervallFunctionRestBean intervall = new IntervallFunctionRestBean();
		ValueFunctionRestBean v = createParameterIn(ValueFunctionRestBean.class, intervall);
		v.setField(fieldName);
		v.setName("value");

		ValueFunctionRestBean r = createParameterIn(ValueFunctionRestBean.class, intervall);
		r.setNumeric(maxValue);
		r.setName("right");
		return intervall;
	}

	public static FunctionReferenceRestBean emptyOrIntervallLength(String fieldName, int minLength, int maxLength) {
		FunctionReferenceRestBean or = new ORFunctionRestBean();
		addParameterTo(empty(fieldName), or);
		addParameterTo(intervallLength(fieldName, minLength, maxLength), or);
		return or;
	}

	public static FunctionReferenceRestBean exactLength(String fieldName, int length) {
		return intervallLength(fieldName, length, length);
	}

	public static RuleFunctionRestBean buildNotEmptyRule(String fieldName) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		addParameterTo(notEmpty(fieldName), rule);
		rule.setField(fieldName);
		rule.setName("notempty");
		return rule;
	}

	public static RuleFunctionRestBean buildMinLengthRule(String fieldName, int minLength) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("length");

		addParameterTo(RuleBuilder.minLength(fieldName, minLength), rule);
		return rule;
	}

	public static RuleFunctionRestBean buildOptionalMaxLengthRule(String fieldName, int maxLength) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("length");

		ORFunctionRestBean or = createParameterIn(ORFunctionRestBean.class, rule);
		addParameterTo(empty(fieldName), or);
		addParameterTo(RuleBuilder.maxLength(fieldName, maxLength), or);
		return rule;
	}

	public static RuleFunctionRestBean buildMaxLengthRule(String fieldName, int maxLength) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("length");

		addParameterTo(RuleBuilder.maxLength(fieldName, maxLength), rule);
		return rule;
	}

	public static RuleFunctionRestBean buildIntervalLengthRule(String fieldName, int minLength, int maxLength) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("length");

		addParameterTo(RuleBuilder.intervallLength(fieldName, minLength, maxLength), rule);
		return rule;
	}

	public static FunctionReferenceRestBean positiveNumber(String fieldName) {
		return greaterThan(fieldName, 0);
	}

	public static RuleFunctionRestBean buildPositiveNumberRule(String fieldName) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setName("positive");
		addParameterTo(positiveNumber(fieldName), rule);
		return rule;
	}

	public static FunctionReferenceRestBean oneFieldValueNotEmpty(String... fieldNames) {
		ORFunctionRestBean or = new ORFunctionRestBean();

		if (fieldNames != null && fieldNames.length > 0) {
			for (String fieldName : fieldNames) {
				addParameterTo(notEmpty(fieldName), or);
			}
		} else {
			throw new IllegalArgumentException("oneFieldValueNotEmpty requires at least one fieldName");
		}

		return or;
	}

	public static FunctionReferenceRestBean ifFieldValueEqualsStringOneOtherFieldNotEmpty(String fieldName, String expectedValue, String... notEmptyFields) {

		ANDFunctionRestBean and = new ANDFunctionRestBean();
		EqualsFunctionRestBean equalsCC = createParameterIn(EqualsFunctionRestBean.class, and);
		createParameterIn(ValueFunctionRestBean.class, equalsCC).setField(fieldName);
		createParameterIn(ValueFunctionRestBean.class, equalsCC).setString(expectedValue);

		addParameterTo(RuleBuilder.oneFieldValueNotEmpty(notEmptyFields), and);

		return and;
	}

	public static FunctionReferenceRestBean fieldValueNotEqualsEitherString(String fieldName, String... unallowedValues) {
		ANDFunctionRestBean and = new ANDFunctionRestBean();
		if (unallowedValues != null && unallowedValues.length > 0) {
			for (String unallowedValue : unallowedValues) {
				addParameterTo(fieldValueNotEquals(fieldName, unallowedValue), and);
			}
		} else {
			throw new IllegalArgumentException("fieldValueNotEqualsEitherString requires at least one unallowedValue");
		}
		return and;
	}

	public static FunctionReferenceRestBean fieldValueNotEquals(String fieldName, String unallowedValue) {
		NotFunctionRestBean not = new NotFunctionRestBean();
		addParameterTo(fieldValueEquals(fieldName, unallowedValue), not);
		return not;
	}

	public static FunctionReferenceRestBean fieldValueEquals(String fieldName, String expectedValue) {
		EqualsFunctionRestBean eq = new EqualsFunctionRestBean();
		createParameterIn(ValueFunctionRestBean.class, eq).setField(fieldName);
		createParameterIn(ValueFunctionRestBean.class, eq).setString(expectedValue);
		return eq;
	}

	public static FunctionReferenceRestBean beforeNow(String fieldName) {
		IntervallFunctionRestBean intervall = new IntervallFunctionRestBean();
		ValueFunctionRestBean v = createParameterIn(ValueFunctionRestBean.class, intervall);
		v.setField(fieldName);
		v.setName("value");

		ValueFunctionRestBean r = createParameterIn(ValueFunctionRestBean.class, intervall);
		r.setDateOfNow(Boolean.TRUE);
		r.setName("right");
		return intervall;
	}

	public static FunctionReferenceRestBean afterNow(String fieldName) {
		NotFunctionRestBean not = new NotFunctionRestBean();
		addParameterTo(beforeNow(fieldName), not);
		return not;
	}

	public static FunctionReferenceRestBean beforeDate(String fieldName, Date date) {
		IntervallFunctionRestBean intervall = new IntervallFunctionRestBean();
		ValueFunctionRestBean v = createParameterIn(ValueFunctionRestBean.class, intervall);
		v.setField(fieldName);
		v.setName("value");

		ValueFunctionRestBean r = createParameterIn(ValueFunctionRestBean.class, intervall);
		r.setDate(date);
		r.setName("right");
		return intervall;
	}

	public static FunctionReferenceRestBean afterDate(String fieldName, Date date) {
		NotFunctionRestBean not = new NotFunctionRestBean();
		addParameterTo(beforeDate(fieldName, date), not);
		return not;
	}

	public static FunctionReferenceRestBean precision(String fieldName, int integer, int decimal) {
		FunctionReferenceRestBean fn = decimalPrecision(fieldName, decimal);

		ValueFunctionRestBean v = createParameterIn(ValueFunctionRestBean.class, fn);
		v.setNumeric(integer);
		v.setName("integer");

		return fn;
	}

	public static FunctionReferenceRestBean decimalPrecision(String fieldName, int decimal) {
		PrecisionFunctionRestBean p = new PrecisionFunctionRestBean();
		ValueFunctionRestBean v = createParameterIn(ValueFunctionRestBean.class, p);
		v.setField(fieldName);
		v.setName("value");

		v = createParameterIn(ValueFunctionRestBean.class, p);
		v.setNumeric(decimal);
		v.setName("decimal");
		return p;
	}

	public static FunctionReferenceRestBean charset(String fieldName, String chars) {
		CharsetFunctionRestBean charSet = new CharsetFunctionRestBean();
		ValueFunctionRestBean v = createParameterIn(ValueFunctionRestBean.class, charSet);
		v.setName("value");
		v.setField(fieldName);

		v = createParameterIn(ValueFunctionRestBean.class, charSet);
		v.setName("allowed");
		v.setString(chars);
		return charSet;
	}

	public static FunctionReferenceRestBean stringInValueList(String fieldName, String... allowedValues) {
		ORFunctionRestBean or = new ORFunctionRestBean();
		for (String allowedValue : allowedValues) {
			EqualsFunctionRestBean eq = createParameterIn(EqualsFunctionRestBean.class, or);
			createParameterIn(ValueFunctionRestBean.class, eq).setField(fieldName);
			createParameterIn(ValueFunctionRestBean.class, eq).setString(allowedValue);
		}
		return or;
	}

	public static RuleFunctionRestBean oneFieldValueNotEmptyRule(String... fieldNames) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setName("notempty");
		addParameterTo(oneFieldValueNotEmpty(fieldNames), rule);
		return rule;
	}

	public static RuleFunctionRestBean buildBeforeDateRule(String fieldName, Date date) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("before");
		addParameterTo(beforeDate(fieldName, date), rule);
		return rule;
	}

	public static RuleFunctionRestBean buildAfterDateRule(String fieldName, Date date) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("after");
		addParameterTo(afterDate(fieldName, date), rule);
		return rule;
	}

	public static RuleFunctionRestBean buildBeforeNowRule(String fieldName) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("before");
		addParameterTo(beforeNow(fieldName), rule);
		return rule;
	}

	public static RuleFunctionRestBean buildAfterNowRule(String fieldName) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("after");
		addParameterTo(afterNow(fieldName), rule);
		return rule;
	}

	public static RuleFunctionRestBean buildPrecisionRule(String fieldName, int integer, int decimal) {
		RuleFunctionRestBean rule = new RuleFunctionRestBean();
		rule.setField(fieldName);
		rule.setName("length");
		addParameterTo(precision(fieldName, integer, decimal), rule);
		return rule;
	}

	/**
	 * takes two functionRestBeans and performs a recursive deep copy from one
	 * to another
	 *
	 * @param toCloneTo
	 * @param toCloneFrom
	 */
	public static void cloneParametersInto(FunctionReferenceRestBean toCloneTo, FunctionReferenceRestBean toCloneFrom) {
		if (toCloneFrom.getParameters() == null) {
			return;
		}
		List<FunctionReferenceRestBean> originalParameters = defensiveParameters(toCloneFrom);
		if (originalParameters != null) {
			for (FunctionReferenceRestBean functionRestEasyBean : originalParameters) {
				FunctionReferenceRestBean clone = clone(functionRestEasyBean);
				if (clone != null) {
					addParameterTo(clone, toCloneTo);
					cloneParametersInto(clone, functionRestEasyBean);
				}
			}
		}
	}

	private static List<FunctionReferenceRestBean> defensiveParameters(FunctionReferenceRestBean toCloneFrom) {
		FunctionsRestBean p = toCloneFrom.getParameters();
		if (p == null) {
			return null;
		}
		return CollectionUtil.defensiveCopy(p.getItems());
	}

	/**
	 * takes a functionRestBean and creates a clone of it. parameters will not
	 * be cloned.
	 *
	 * @param functionRestEasyBean
	 * @return
	 */
	public static FunctionReferenceRestBean clone(FunctionReferenceRestBean functionRestEasyBean) {
		Class<? extends FunctionReferenceRestBean> clazz = functionRestEasyBean.getClass();
		try {
			FunctionReferenceRestBean clone = clazz.newInstance();
			clone.setName(functionRestEasyBean.getName());
			if (ValueFunctionRestBean.class.isAssignableFrom(clazz)) {
				ValueFunctionRestBean v = (ValueFunctionRestBean) functionRestEasyBean;
				ValueFunctionRestBean c = (ValueFunctionRestBean) clone;
				c.setField(v.getField());
				c.setNumeric(v.getNumeric());
				c.setString(v.getString());
			}
			if (RuleFunctionRestBean.class.isAssignableFrom(clazz)) {
				RuleFunctionRestBean v = (RuleFunctionRestBean) functionRestEasyBean;
				RuleFunctionRestBean c = (RuleFunctionRestBean) clone;
				c.setField(v.getField());
				c.setName(v.getName());
			}
			return clone;
		} catch (InstantiationException | IllegalAccessException ex) {
			LOG.error("could not instantiate class while creating clone: "+ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * takes a functionRestBean and creates a clone of it. parameters will be
	 * cloned as well.
	 *
	 * @param functionRestEasyBean
	 * @return
	 */
	public static <FN extends FunctionReferenceRestBean> FN cloneDeep(FN functionRestEasyBean) {
		@SuppressWarnings("unchecked")
		Class<FN> clazz = (Class<FN>) functionRestEasyBean.getClass();
		try {
			FN clone = clazz.newInstance();
			clone.setName(functionRestEasyBean.getName());
			if (ValueFunctionRestBean.class.isAssignableFrom(clazz)) {
				ValueFunctionRestBean v = (ValueFunctionRestBean) functionRestEasyBean;
				ValueFunctionRestBean c = (ValueFunctionRestBean) clone;
				c.setField(v.getField());
				c.setNumeric(v.getNumeric());
				c.setString(v.getString());
			}
			if (RuleFunctionRestBean.class.isAssignableFrom(clazz)) {
				RuleFunctionRestBean v = (RuleFunctionRestBean) functionRestEasyBean;
				RuleFunctionRestBean c = (RuleFunctionRestBean) clone;
				c.setField(v.getField());
				c.setName(v.getName());
			}

			cloneParametersInto(clone, functionRestEasyBean);

			return clone;
		} catch (InstantiationException | IllegalAccessException ex) {
			LOG.error("could not instantiate class while creating deep clone: "+ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * takes a rule and joins it with the rules generated by another
	 * ruleBuilder.
	 *
	 * @param rule
	 * @param ruleBuilder
	 * @return a list of rules that represent one big rule that validates
	 * everything and multiple smaller rules that are a combinatio of the base
	 * rule and the individual joined rules
	 */
	public static List<RuleFunctionRestBean> joinRuleWithRuleBuilder(RuleFunctionRestBean rule, RestBeanRelatedRuleBuilder<?> ruleBuilder) {
		return joinRuleWithRuleBuilder(rule, rule, ruleBuilder);
	}

	/**
	 * takes a rule and joins it with the rules generated by another
	 * ruleBuilder. the injectionTarget specifies where the rules from the
	 * ruleBuilder should be injected. this makes sense when a joined rule
	 * depends on another rule fragment of the base rule example "firstName
	 * should not be empty only if the deliveryAddress is not empty in total"
	 *
	 * @param rule
	 * @param ruleBuilder
	 * @return a list of rules that represent one big rule that validates
	 * everything and multiple smaller rules that are a combinatio of the base
	 * rule and the individual joined rules
	 */
	public static List<RuleFunctionRestBean> joinRuleWithRuleBuilder(RuleFunctionRestBean rule, FunctionReferenceRestBean injectionTarget, RestBeanRelatedRuleBuilder<?> ruleBuilder) {
		String rootField = rule.getField();
		RulesRestBean rules = ruleBuilder.buildRules();
		List<RuleFunctionRestBean> rawRules = CollectionUtil.getItemsAs(rules, RuleFunctionRestBean.class);
		// the marker function is just a utility to find the injectionTarget in a cloned function tree
		MarkerFunction marker = new MarkerFunction();
		addParameterTo(marker, injectionTarget);

		// add the rootField name as a prefix to all rules that shall be joined into the rule
		List<FunctionReferenceRestBean> touchedFunctions = new ArrayList<FunctionReferenceRestBean>();
		for (RuleFunctionRestBean ruleFunctionRestBean : rawRules) {
			appendRootFieldNameToSubFields(rootField, ruleFunctionRestBean, touchedFunctions);
		}

	// once all rules that will be joined are named correctly, the joined rules will create multiple rules as a result
		// - one big rule that validates everything in total
		// - multiple smaller rules, that will the combination of the base rule and a single rule that will joined into it
		List<RuleFunctionRestBean> result = new ArrayList<RuleFunctionRestBean>();
		for (RuleFunctionRestBean ruleFunctionRestBean : rawRules) {
			RuleFunctionRestBean deepRule = RuleBuilder.cloneDeep(rule);
			deepRule.setField(ruleFunctionRestBean.getField());
			deepRule.setName(ruleFunctionRestBean.getName());
			FunctionReferenceRestBean deepRuleInjectionTarget = extractDeepRuleInjectionTarget(deepRule);
			ANDFunctionRestBean and = createParameterIn(ANDFunctionRestBean.class, deepRuleInjectionTarget);
			addAllParametersTo(ruleFunctionRestBean.getParameters(), and);
			result.add(deepRule);
		}

		// this will create the big rule that validates everything
		ANDFunctionRestBean and = createParameterIn(ANDFunctionRestBean.class, injectionTarget);
		for (RuleFunctionRestBean ruleFunctionRestBean : rawRules) {
			addAllParametersTo(ruleFunctionRestBean.getParameters(), and);
		}

		removeParameterFrom(marker, injectionTarget);

		result.add(rule);
		return result;
	}

	private static FunctionReferenceRestBean extractDeepRuleInjectionTarget(RuleFunctionRestBean deepRule) {
		return extractDeepRuleInjectionTargetRecursive(deepRule);
	}

	/**
	 * scans a functionRestBean graph for the markerFunction and removes it from
	 * the graph.
	 *
	 * @param inspectedFunction
	 * @return
	 */
	private static FunctionReferenceRestBean extractDeepRuleInjectionTargetRecursive(FunctionReferenceRestBean inspectedFunction) {
		FunctionReferenceRestBean injectionTarget = null;

		if (inspectedFunction.getParameters() != null) {
			List<FunctionReferenceRestBean> params = defensiveParameters(inspectedFunction);
			if (params != null) {
				for (FunctionReferenceRestBean markerCandidate : params) {
					if (MarkerFunction.class.isAssignableFrom(markerCandidate.getClass())) {
						injectionTarget = inspectedFunction;
						removeParameterFrom(markerCandidate, inspectedFunction);
					} else {
						injectionTarget = extractDeepRuleInjectionTargetRecursive(markerCandidate);
					}

					if (injectionTarget != null) {
						break;
					}
				}
			}

		}

		return injectionTarget;
	}

	/**
	 * recursively appends a rootFieldName to all FieldRelated functions in the
	 * provided function graph
	 *
	 * @param rootField the prefix to add
	 * @param functionRestBean function graph root
	 */
	private static void appendRootFieldNameToSubFields(String rootField, FunctionReferenceRestBean functionRestBean, List<FunctionReferenceRestBean> touchedFunctions) {
		if (FieldRelated.class.isAssignableFrom(functionRestBean.getClass())) {
			FieldRelated fr = FieldRelated.class.cast(functionRestBean);
			boolean usesField = true;
			if (ValueFunctionRestBean.class.isAssignableFrom(functionRestBean.getClass())) {
				ValueFunctionRestBean vf = ValueFunctionRestBean.class.cast(functionRestBean);
				usesField = vf.getDate() == null && vf.getNumeric() == null && vf.getString() == null;
			}
			if (usesField) {
				if (!touchedFunctions.contains(functionRestBean)) {
					String field = fr.getField();
					if (field != null) {
						field = rootField + "." + field;
					} else {
						field = rootField;
					}
					fr.setField(field);
					touchedFunctions.add(functionRestBean);
				}
			}
		}
		if (functionRestBean.getParameters() != null) {
			List<FunctionReferenceRestBean> params = functionRestBean.getParameters().getItems();
			if (params != null) {
				for (FunctionReferenceRestBean functionRestBean2 : params) {
					appendRootFieldNameToSubFields(rootField, functionRestBean2, touchedFunctions);
				}
			}

		}

	}

	private static void addAllParametersTo(FunctionsRestBean parameters, final FunctionReferenceRestBean target) {
		if (parameters != null) {
			parameters.each(new ListRestBean.ItemHandler<FunctionReferenceRestBean>() {
				@Override
				public void handle(FunctionReferenceRestBean item) {
					addParameterTo(item, target);
				}
			});
		}
	}

	public static void addParameterTo(FunctionReferenceRestBean newParameter, FunctionReferenceRestBean target) {
		FunctionsRestBean parameters = target.getParameters();
		if (parameters == null) {
			parameters = new FunctionsRestBean();
			target.setParameters(parameters);
		}
		parameters.addParameter(newParameter);
	}

	public static void removeParameterFrom(FunctionReferenceRestBean toRemove, FunctionReferenceRestBean target) {
		FunctionsRestBean parameters = target.getParameters();
		if (parameters != null) {
			parameters.removeParameter(toRemove);
		}
	}

	public static <E extends FunctionReferenceRestBean> E createParameterIn(Class<E> type, FunctionReferenceRestBean target) {
		FunctionsRestBean parameters = target.getParameters();
		if (parameters == null) {
			parameters = new FunctionsRestBean();
			target.setParameters(parameters);
		}
		return parameters.createParameter(type);
	}
}
