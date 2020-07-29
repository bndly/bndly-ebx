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
define(["cy/views/PriceView", "cy/RestBeans", "cy/LabelFunctions", "cy/ui/input/Button", "cy/ui/container/Form", "cy/ui/input/TextInput", "cy/ui/input/SelectInput", "cy/Collection", "cy/FieldSet", "cy/EntityCollection", "cy/ui/input/DateInput"], function(PriceView, RestBeans, LabelFunctions, Button, Form, TextInput, SelectInput, Collection, FieldSet, EntityCollection, DateInput) {
	var SimplePriceView = PriceView.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

                        if(!config.legend) {
                            config.legend = "Simple Price";
                        }
                        if(!config.price) {
                            config.price = new RestBeans["SimplePriceRestBean"]();
                        }
                        config.items = [
                            new TextInput({
				name: "netValue",
				label: "Net Value",
				entity: config.price,
                                type: "number",
				member: "netValue"
                            })
                        ];
			this.callSuper(arguments, config);
		},
                destroy: function() {
                    this.invokeSuper(arguments);
		}
	});
	return SimplePriceView;

});
