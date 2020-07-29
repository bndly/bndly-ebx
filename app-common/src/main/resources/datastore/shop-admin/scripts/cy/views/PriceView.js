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
define(["cy/FieldSet", "cy/RestBeans", "cy/LabelFunctions", "cy/ui/input/Button", "cy/ui/container/Form", "cy/ui/input/TextInput", "cy/ui/input/SelectInput", "cy/Collection", "cy/FieldSet", "cy/EntityCollection", "cy/ui/input/DateInput"], function(FieldSet, RestBeans, LabelFunctions, Button, Form, TextInput, SelectInput, Collection, FieldSet, EntityCollection, DateInput) {
	var PriceView = FieldSet.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

                        if(!config.price) {
                            throw new Error("missing price object");
                        }
                        var constraints = config.price.getConstraints();
                        if(!constraints) {
                            constraints = new RestBeans["PriceConstraintListRestBean"]();
                            config.price.setConstraints(constraints);
                        }
                        var constraintItems = constraints.getItems();
                        if(!constraintItems) {
                            constraintItems = new Collection();
                            constraints.setItems(constraintItems);
                        }
                        if(constraintItems.size() <= 0) {
                            config.constraint = new RestBeans["PriceConstraintRestBean"]();
                            constraintItems.add(config.constraint);
                        } else {
                            config.constraint = constraintItems.getItemAt(0);
                        }
                        
                        if(!config.taxModels) {
                            config.taxModels = new EntityCollection({
                                proto: new RestBeans["ValueAddedTaxRestBean"]()
                            });
                            config.taxModels.load();
                        }
                            if(!config.currencies) {
                            config.currencies = new EntityCollection({
                                proto: new RestBeans["CurrencyRestBean"]()
                            });
                            config.currencies.load();
                        }
                        
                        var taxModelInput = new SelectInput({
				name: "taxModel",
				label: "Tax Model",
				allowEmpty: true,	
				entryValueFn: function(entry) {
					var l = entry.hasLink("self");
					return l ? l.getHref() : "";
				},
				entryLabelFn: LabelFunctions,
				entity: config.price,
				member: "taxModel",
				items: config.taxModels
			});
                        var currencyInput = new SelectInput({
				name: "currency",
				label: "Currency",
                                allowEmpty: true,
				entryValueFn: function(entry) {
					var l = entry.hasLink("self");
					return l ? l.getHref() : "";
				},
				entryLabelFn: LabelFunctions,
				entity: config.price,
				member: "currency",
				items: config.currencies
			});
                        if(!config.items) {
                            config.items = [];
                        }
                        var sharedItems = [
                            new DateInput({
				name: "validFrom",
				label: "Valid From",
				entity: config.constraint,
				member: "startDate"
                            }),
                            new DateInput({
				name: "validTo",
				label: "Valid To",
				entity: config.constraint,
				member: "endDate"
                            }),
                            currencyInput,
                            taxModelInput,
                            new Button({
                                label: "remove",
                                listeners: {
                                    clicked: {
                                        fn: this.removeButtonClicked,
                                        scope: this
                                    }
                                }
                            })
                        ];
                        for(var i in sharedItems) {
                            if(config.items instanceof Collection) {
                                config.items.add(sharedItems[i]);                                
                            } else {
                                config.items.push(sharedItems[i]);
                            }
                        }
			this.callSuper(arguments, config);
		},
                destroy: function() {
                    this.invokeSuper(arguments);
		},
		renderTo: function(renderTarget) {
			this.invokeSuper(arguments);
		},
                removeButtonClicked: function(button) {
                    this.fireEvent("removed", this);
                    return false;
                }
	});
	return PriceView;

});
