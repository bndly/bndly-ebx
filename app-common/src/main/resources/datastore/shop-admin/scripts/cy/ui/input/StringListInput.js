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
define(["cy/ui/input/TextInput", "cy/Collection", "cy/ui/input/InputTypeRegistry", "cy/ui/Text"], function(TextInput, Collection, InputTypeRegistry, Text) {
	var StringListInput = TextInput.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
			if(!config.values) {
				config.values = new Collection();
			}
			this.callSuper(arguments, config);
			this.listen(this.values, "inserted", this.stringValueAdded, this);
		},
		destroy: function() {
			arguments.callee.$parent.destroy.call(this);
		},
		renderTo: function(renderTarget) {
			this.valuesElement = $(renderTarget).append("<div></div>").children().last();
			this.invokeSuper(arguments);
			var _this = this;
			$(this.input).keyup(function(event) {
				_this.keyup(event);
			});

			this.renderStringValues();
		},
		keyup: function(event) {
			// Enter key = keycode 13
			if (event.which === 13) {
				var currentValue = $(this.input).val();
				if(currentValue) {
					currentValue = currentValue.trim();
					if(currentValue !== "") {
						// new value entered
						this.values.add(currentValue);
						$(this.input).val("");
						this.inputChanged();
					}
				}
			}
			return false;
		},
		getValue: function() {
			if(this.values instanceof Collection) {
				return this.values;
			}
		},
		setValue: function(value) {
			if(value instanceof Collection) {
				this.values = value;
			}
		},
		renderStringValues: function() {
			this.values.each(function(string) {
				this.renderStringValue(string);
			}, this);
		},
		stringValueAdded: function(string) {
			if(this.valuesElement) {
				this.renderStringValue(string);
			}
		},
		renderStringValue: function(string) {
			var stringText = new Text({value: string, cls: "label label-info"});
			stringText.renderTo($(this.valuesElement));
			$(stringText.element).css({
				margin: "2px"
			});
			var removeIcon = $(stringText.element).append("<i class=\"icon-remove\"></i>").children().last();
			stringText.element.attr("value", string);
			var _this = this;
			$(removeIcon).click(function() {
				stringText.destroy();
				_this.values.remove(string);
			});
		}
	});
        InputTypeRegistry.register("StringListInput", StringListInput);
	return StringListInput;
});
