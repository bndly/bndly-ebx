/*-
 * #%L
 * org.bndly.ebx.app-common
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
define(["cy/ui/container/ViewContainer", "cy/CopyUtil", "cy/ui/ViewComponent", "cy/Collection", "cy/ui/input/BooleanInput", "cy/ui/input/TextInput", "cy/RestBeans", "cy/ui/Text"], function (ViewContainer, CopyUtil, ViewComponent, Collection, BooleanInput, TextInput, RestBeans, Text) {
	var ConstraintTable = ViewComponent.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			var defaultNumberCfg = {
				type: "number",
				step: 1,
				cssClass: "input-mini"
			};
			config.constraints = new Collection();
			config.constraints.add({
				handler: this.createNotEmpty,
				label: "not empty",
				expression: "*and->not->empty->value",
				input: BooleanInput
			});
			config.constraints.add({
				handler: this.createMinLength,
				label: "min length",
				expression: "*and->not->maxSize->value[name=value],value[name=size]",
				inputExpression: "*and->not->maxSize->value[name=size]",
				allowOn: {
					String: true,
					Collection: true
				},
				input: TextInput,
				inputConfig: CopyUtil.copyConfig(defaultNumberCfg, {
					min: 0
				})
			});
			config.constraints.add({
				handler: this.createMaxLength,
				label: "max length",
				expression: "*and->maxSize->value[name=value],value[name=size]",
				inputExpression: "*and->not->maxSize->value[name=size]",
				allowOn: {
					String: true,
					Collection: true
				},
				input: TextInput,
				inputConfig: CopyUtil.copyConfig(defaultNumberCfg, {
					min: 0
				})
			});
			config.constraints.add({
				handler: this.createGreaterThan,
				label: "greater than",
				expression: "*and->intervall->value[name=value],value[name=left]",
				inputExpression: "*and->intervall->value[name=left]",
				allowOn: {
					Number: true
				},
				input: TextInput,
				inputConfig: CopyUtil.copyConfig(defaultNumberCfg, {
				})
			});
			config.constraints.add({
				handler: this.createLowerThan,
				label: "lower than",
				expression: "*and->intervall->value[name=value],value[name=right]",
				inputExpression: "*and->intervall->value[name=right]",
				allowOn: {
					Number: true
				},
				input: TextInput,
				inputConfig: CopyUtil.copyConfig(defaultNumberCfg, {
				})
			});
			config.constraints.add({
				handler: this.createPositive,
				label: "positive",
				expression: "*and->intervall->value[name=value],value[name=left,numeric=0]",
				allowOn: {
					Number: true
				},
				input: BooleanInput
			});
			config.constraints.add({
				handler: this.createBeforeNow,
				label: "before now",
				expression: "*and->intervall->value[name=value],value[name=right,dateOfNow=true]",
				allowOn: {
					Date: true
				},
				input: BooleanInput
			});
			config.constraints.add({
				handler: this.createAfterNow,
				label: "after now",
				expression: "*and->intervall->value[name=value],value[name=left,dateOfNow=true]",
				allowOn: {
					Date: true
				},
				input: BooleanInput
			});
			config.constraints.add({
				handler: this.createPrecision,
				label: "precision",
				expression: "*and->precision->value[name=value],value[name=decimal]",
				allowOn: {
					Number: true
				},
				input: TextInput,
				inputConfig: CopyUtil.copyConfig(defaultNumberCfg, {
					min: 0
				})
			});
			if (!config.attributes) {
				config.attributes = new Collection();
			} else {
				if (!(config instanceof Collection)) {
					config.attributes = new Collection(config.attributes);
				}
			}
			this.callSuper(arguments, config);
			this.listen(this.attributes, "inserted", this.renderAttributeRow, this);
			this.listen(this.attributes, "removed", this.removeAttributeRow, this);
		},
		renderTo: function (renderTarget) {
			this.table = $(renderTarget).append("<table class=\"table table-condensed\"></table>").children().last();
			this.tableHead = $(this.table).append("<thead></thead>").children().last();
			this.tableBody = $(this.table).append("<tbody></tbody>").children().last();
			this.tableHeadRow = $(this.tableHead).append("<tr></tr>").children().last();
			this.tableAttributeHeaderColumn = this.renderHeaderColumn("Attribute");
			this.attributeBindings = {};
			this.constraints.each(function (constraint) {
				constraint.td = this.renderHeaderColumn(constraint.label);
			}, this);
			this.renderAttributes();
			this.callSuper(arguments, renderTarget);
		},
		renderHeaderColumn: function (label) {
			return this.renderColumn(label, this.tableHeadRow, "th");
		},
		renderColumn: function (label, target, tag) {
			if (!tag) {
				tag = "td";
			}
			var td = new Text({tag:tag, value: label});
			td.renderTo($(target));
			return td;
		},
		renderAttributes: function () {
			if (this.attributes) {
				this.attributes.each(function (attribute) {
					this.renderAttributeRow(attribute);
				}, this);
			}
		},
		removeAttributeRow: function (attribute) {
			var binding = this.attributeBindings[attribute.getName()];
			$(binding.row).remove();
			this.attributeBindings[attribute.getName()] = undefined;
		},
		renderAttributeRow: function (attribute) {
			var
					row = $(this.tableBody).append("<tr></tr>").children().last(),
					binding = {
						row: row,
						inputs: []
					};
			this.attributeBindings[attribute.getName()] = binding;
			this.renderColumn(attribute.getName(), row);
			this.constraints.each(function (constraint) {
				var col = this.renderColumn(undefined, row);
				var inputElement = new constraint.input(CopyUtil.copyConfig(constraint.inputConfig, {
					disabled: !this.constraintAppliesForAttribute(constraint, attribute),
					listeners: {
						changed: {
							fn: function (input, value) {
								constraint.handler.call(this, attribute, input, value);
							},
							scope: this
						}
					}
				}));
				binding.inputs.push({
					constraint: constraint,
					input: inputElement
				});
				inputElement.renderTo(col);
			}, this);
		},
		constraintAppliesForAttribute: function (constraint, attribute) {
			if (!constraint.allowOn) {
				return true;
			} else if (attribute instanceof RestBeans["StringAttributeBean"]) {
				return constraint.allowOn["String"];
			} else if (attribute instanceof RestBeans["NamedAttributeHolderAttributeBean"]) {
				return constraint.allowOn["Object"];
			} else if (attribute instanceof RestBeans["InverseAttributeBean"]) {
				return constraint.allowOn["Collection"];
			} else if (attribute instanceof RestBeans["DecimalAttributeBean"]) {
				return constraint.allowOn["Number"];
			} else if (attribute instanceof RestBeans["DateAttributeBean"]) {
				return constraint.allowOn["Date"];
			} else if (attribute instanceof RestBeans["BooleanAttributeBean"]) {
				return constraint.allowOn["Boolean"];
			} else if (attribute instanceof RestBeans["JSONAttributeBean"]) {
				return constraint.allowOn["Object"];
			}
		},
		createNotEmpty: function (attribute, input, value) {
			console.log("not empty " + attribute.getName());
		},
		createMinLength: function (attribute, input, value) {
			console.log("min length " + attribute.getName());
		},
		createMaxLength: function (attribute, input, value) {
			console.log("max length " + attribute.getName());
		},
		createGreaterThan: function (attribute, input, value) {
			console.log("greater than " + attribute.getName());
		},
		createLowerThan: function (attribute, input, value) {
			console.log("lower than " + attribute.getName());
		},
		createPositive: function (attribute, input, value) {
			console.log("positive " + attribute.getName());
		},
		createBeforeNow: function (attribute, input, value) {
			console.log("before now " + attribute.getName());
		},
		createAfterNow: function (attribute, input, value) {
			console.log("after now " + attribute.getName());
		},
		createPrecision: function (attribute, input, value) {
			console.log("precision " + attribute.getName());
		},
		applyRules: function (collection) {
			this.rules = collection;
			collection.each(function (ruleFn) {
				this.applyRule(ruleFn);
			}, this);
		},
		applyRule: function (ruleFn) {
			// iterate over all constraints and check if the constraint expression applies to the rule function
			// if the constraint and rule function match,
			// bind the constraint input to the function parameters

			this.constraints.each(function (constraint) {
				var expression = constraint.expression;
				if (this.expressionApplies(expression, ruleFn)) {
					var binding = this.attributeBindings[ruleFn.getField()];
					if (binding) {
						var input;
						for (var i in binding.inputs) {
							var inputConstraintBinding = binding.inputs[i];
							if (inputConstraintBinding.constraint === constraint) {
								input = inputConstraintBinding.input;
								break;
							}
						}
						if (input) {
							this.bindConstraintToInput(constraint, input, ruleFn);
						} else {
							console.warn("no input found");
						}
					} else {
						console.warn("unknown attribute: " + ruleFn.getField());
					}
				}
			}, this);
		},
		expressionApplies: function (expression, fn, anyChild, childOf) {
			if (!fn || !expression) {
				return false;
			}
			var
					i = expression.indexOf("->"),
					isLast = i < 0;

			var
					fragment = isLast ? expression : expression.substr(0, i),
					furtherExpression = isLast ? undefined : expression.substr(i + "->".length);
			if ('*' === fragment.charAt(0)) {
				return this.expressionApplies(furtherExpression, fn, true, fragment.substr(1));
			} else {
				var fnForFragment = this.getParameterByExpressionFragmentMatchWithAny(fragment, fn, anyChild, childOf);
				if (!fnForFragment) {
					return false;
				} else {
					if (isLast) {
						return true;
					} else {
						return this.expressionApplies(furtherExpression, fnForFragment, anyChild, childOf);
					}
				}
			}
		},
		getParameterByExpression: function (expression, fn, anyChild, childOf) {
			if (!fn || !expression) {
				return undefined;
			}
			var i = expression.indexOf("->"),
				isLast = i < 0;

			var fragment = isLast ? expression : expression.substr(0, i),
				furtherExpression = isLast ? undefined : expression.substr(i + "->".length);
			if ('*' === fragment.charAt(0)) {
				return this.getParameterByExpression(furtherExpression, fn, true, fragment.substr(1));
			} else {
				var fnForFragment = this.getParameterByExpressionFragmentMatchWithAny(fragment, fn, anyChild, childOf);
				if (!fnForFragment) {
					return fnForFragment;
				} else {
					if (isLast) {
						return fnForFragment;
					} else {
						return this.getParameterByExpression(furtherExpression, fnForFragment, false, undefined);
					}
				}
			}
		},
		getParameterByExpressionFragmentMatchWithAny: function (fragment, fn, anyChild, childOf) {
			var r = this.getParameterByExpressionFragment(fragment, fn);
			if (!r && anyChild) {
				var p = fn.getParameters();
				if (p) {
					p.getParameters().each(function (parameterFn) {
						if (!r) {
							if (this.functionTypeMatches(childOf, parameterFn)) {
								r = this.getParameterByExpressionFragmentMatchWithAny(fragment, parameterFn, anyChild, childOf);
							}
						} else {
							return;
						}
					}, this);
				}
			}
			return r;
		},
		getParameterByExpressionFragment: function (fragment, fn) {
			var p = fn.getParameters();
			if (!p) {
				return undefined;
			}

			// split the fragment when there is a colon
			var inAttribute = false;
			var elements = [];
			var inAttributeValue;
			var tmpAtt;
			var tmpAttValue;
			var tmp = {
				el: "",
				atts: {}
			};
			for (var i = 0; i < fragment.length; i++) {
				var char = fragment.charAt(i), isLast = (i === (fragment.length - 1));
				if (inAttribute) {
					// ignore
					if (']' === char) {
						inAttribute = false;
						tmp.atts[tmpAtt] = tmpAttValue;
					} else {
						if ('=' === char) {
							inAttributeValue = true;
						} else {
							if (!inAttributeValue) {
								tmpAtt += char;
							} else {
								if (',' === char) {
									tmp.atts[tmpAtt] = tmpAttValue;
									tmpAtt = "";
									tmpAttValue = "";
								} else {
									tmpAttValue += char;
								}
							}
						}
					}
				} else {
					if ('[' === char) {
						inAttribute = true;
						tmpAtt = "";
						tmpAttValue = "";
						inAttributeValue = false;
					} else if (',' === char) {
						elements.push(tmp);
						tmp = {
							el: "",
							atts: {}
						};
					} else {
						tmp.el += char;
						if (isLast) {
							elements.push(tmp);
						}
					}
				}
			}

			var result = [];
			for (var i = 0; i < elements.length; i++) {
				var element = elements[i];
				var tmp2;
				p.getParameters().each(function (parameterFn) {
					if (tmp2) {
						return;
					}
					tmp2 = this.functionTypeMatches(element.el, parameterFn);
					if (tmp2) {
						for (var att in element.atts) {
							var v = tmp2.get(att);
							if (v !== undefined && v !== null) {
								if (v != element.atts[att]) {
									tmp2 = undefined;
								}
							} else {
								tmp2 = undefined;
							}
						}
					}
				}, this);
				result.push(tmp2);
				if (!tmp2) {
					return undefined;
				}
			}

			if (result.length === 1) {
				return result[0];
			}
			return result;
		},
		functionTypeMatches: function (fragment, fn) {
			if ("not" === fragment && (fn instanceof RestBeans["NotFunctionRestBean"])) {
				return fn;
			} else if ("empty" === fragment && (fn instanceof RestBeans["EmptyFunctionRestBean"])) {
				return fn;
			} else if ("intervall" === fragment && (fn instanceof RestBeans["IntervallFunctionRestBean"])) {
				return fn;
			} else if ("precision" === fragment && (fn instanceof RestBeans["PrecisionFunctionRestBean"])) {
				return fn;
			} else if ("value" === fragment && (fn instanceof RestBeans["ValueFunctionRestBean"])) {
				return fn;
			} else if ("and" === fragment && (fn instanceof RestBeans["ANDFunctionRestBean"])) {
				return fn;
			} else if ("or" === fragment && (fn instanceof RestBeans["ORFunctionRestBean"])) {
				return fn;
			} else if ("mul" === fragment && (fn instanceof RestBeans["MultiplyFunctionRestBean"])) {
				return fn;
			} else if ("maxSize" === fragment && (fn instanceof RestBeans["MaxSizeFunctionRestBean"])) {
				return fn;
			} else if ("charset" === fragment && (fn instanceof RestBeans["CharsetFunctionRestBean"])) {
				return fn;
			} else if ("regex" === fragment && (fn instanceof RestBeans["RegExFunctionRestBean"])) {
				return fn;
			} else if ("equals" === fragment && (fn instanceof RestBeans["EqualsFunctionRestBean"])) {
				return fn;
			} else {
				return undefined;
			}
		},
		bindConstraintToInput: function (constraint, input, ruleFn) {
//            console.log("bind to input");
//            console.log(ruleFn.getField() + ": " + constraint.label);
			if (input instanceof BooleanInput) {
				if (ruleFn) {
					input.setValue(true);
				}
				this.listen(input, "changed", function (inp, value) {
					var i = this.rules.indexOf(ruleFn);
					if (value) {
						if (i < 0) {
							// append rule
							this.rules.add(ruleFn);
						} else {
							console.warn("rule already exists in rules collection");
						}
					} else {
						// remove rule
						if (i > -1) {
							this.rules.removeAtIndex(i);
						} else {
							console.warn("rule is not present in rules collection");
						}

					}
				}, this);
			} else if (input instanceof TextInput) {
				if (constraint.inputExpression) {
					var valueFn = this.getParameterByExpression(constraint.inputExpression, ruleFn);
					if (valueFn) {
						console.log("applying value to valueFn");
						if (input.type === "number") {
							input.setValue(valueFn.get("numeric"));
						} else {
							input.setValue(valueFn.get("string"));
						}
						this.listen(input, "changed", function (inp, value) {
							if (input.type === "number") {
								valueFn.set("numeric", value);
							} else {
								valueFn.set("string", value);
							}
						}, this);
					} else {
						console.warn("could not find value of for constraint with text input");
					}
				} else {
					console.warn("missing inputExpression on constraint");
				}
			} else {
				console.warn("unsupported input element for constraint");
			}
		}

	});

	var RuleBuilderView = ViewContainer.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			var cfg = CopyUtil.copyConfig(config.formConfig, {});
			cfg.ruleSet = config.parent;
			config.constraintTable = new ConstraintTable({
				attributes: []
			});
			config.attributesByType = {};
			RestBeans.root.follow({
				rel: "schema",
				cb: this.schemaLoaded,
				scope: this
			});
			config.items = [
				config.constraintTable
			];
			this.callSuper(arguments, config);

			if (!this.parent.getRules()) {
				this.parent.setRules(new RestBeans["RulesRestBean"]());
			}
			if (!this.parent.getRules().getItems()) {
				this.parent.getRules().setItems(new Collection());
			}

			this.ruleFunctions = this.parent.getRules().getItems();

			this.listen(this.parent, "changed", this.classChanged, this);
		},
		schemaLoaded: function (schema) {
			// schema is loaded
			// index all attributes for each type
			schema.getMixins().each(function (mixin) {
				var c = {};
				this.attributesByType[mixin.getName()] = c;
				mixin.getAttributes().getItems().each(function (att) {
					c[att.getName()] = att;
				}, this);
			}, this);

			schema.getTypes().each(function (type) {
				var c = {};
				this.attributesByType[type.getName()] = c;
				type.getAttributes().getItems().each(function (att) {
					c[att.getName()] = att;
				}, this);

				var tmpMixins = type.getMixins();
				if (tmpMixins) {
					tmpMixins.each(function (mixin) {
						CopyUtil.copyConfig(this.attributesByType[mixin], c);
					}, this);
				}
			}, this);

			this.applyAttributesForClassName(this.constraintTable, this.parent.get("name"));
		},
		applyAttributesForClassName: function (constraintTable, newClassName) {
			constraintTable.attributes.clear();
			if (newClassName) {
				var cls = newClassName.substr(0, newClassName.indexOf("RestBean"));
				var atts = this.attributesByType[cls];
				if (atts) {
					for (var attributeName in atts) {
						constraintTable.attributes.add(atts[attributeName]);
					}
				}
				constraintTable.applyRules(this.ruleFunctions);
			}
		},
		classChanged: function (rs, member, newClassName, oldClassName) {
			if ("name" === member) {
				this.applyAttributesForClassName(this.constraintTable, newClassName);
			}
		}
	});
	return RuleBuilderView;
});
