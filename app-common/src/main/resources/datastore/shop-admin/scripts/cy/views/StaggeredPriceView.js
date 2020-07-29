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
define(["cy/views/PriceView", "cy/RestBeans", "cy/LabelFunctions", "cy/ui/input/Button", "cy/ui/container/Form", "cy/ui/input/TextInput", "cy/ui/input/SelectInput", "cy/Collection", "cy/FieldSet", "cy/EntityCollection", "cy/ui/input/DateInput", "cy/FieldSet", "cy/ui/container/InputViewContainer"], function (PriceView, RestBeans, LabelFunctions, Button, Form, TextInput, SelectInput, Collection, FieldSet, EntityCollection, DateInput, FieldSet, InputViewContainer) {
	var StaggeredPriceView = PriceView.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}

			if (!config.legend) {
				config.legend = "Staggered Price";
			}
			if (!config.price) {
				config.price = new RestBeans["StaggeredPriceRestBean"]();
			}
			var items = config.price.getItems();
			if (!items) {
				items = new RestBeans["StaggeredPriceItemListRestBean"]();
				config.price.setItems(items);
			}
			this.rangeItems = items.getItems();
			if (!this.rangeItems) {
				this.rangeItems = new Collection();
				items.setItems(this.rangeItems);
			}
			config.addRangeButton = new Button({
				label: "add range",
				listeners: {
					clicked: {
						fn: this.addRange,
						scope: this
					}
				}
			});
			config.rangesContainer = new FieldSet({
				legend: "Ranges"
			});
			config.items = [
				config.addRangeButton,
				config.rangesContainer
			];
			this.callSuper(arguments, config);
		},
		destroy: function () {
			this.invokeSuper(arguments);
		},
		renderTo: function (renderTarget) {
			this.invokeSuper(arguments);
			this.rangeItems.each(function (range) {
				this.renderRange(range);
			}, this);
		},
		addRange: function () {
			var range = new RestBeans["StaggeredPriceItemRestBean"]();
			this.rangeItems.add(range);
			this.renderRange(range);
		},
		renderRange: function (range) {
			var ivc = new InputViewContainer({
				range: range,
				items: [
					new TextInput({
						name: "netValue",
						label: "Net Value",
						entity: range,
						type: "number",
						member: "netValue"
					}),
					new TextInput({
						name: "minQuantity",
						label: "Min",
						entity: range,
						step: 1,
						type: "number",
						member: "minQuantity"
					}),
					new TextInput({
						name: "maxQuantity",
						label: "Max",
						entity: range,
						step: 1,
						type: "number",
						member: "maxQuantity"
					})
				]
			});
			this.rangesContainer.items.add(ivc);
		}
	});
	return StaggeredPriceView;

});
