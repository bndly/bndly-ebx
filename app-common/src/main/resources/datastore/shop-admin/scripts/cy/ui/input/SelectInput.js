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
define(["cy/ui/input/InputViewComponent", "cy/Collection", "cy/LabelFunctions", "cy/ui/input/InputTypeRegistry", "cy/ui/Text"], function (InputViewComponent, Collection, LabelFunctions, InputTypeRegistry, Text) {
	var SelectInput = InputViewComponent.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			if (!config.items) {
				config.items = new Collection();
			}

			if (!config.entryValueFn) {
				config.entryValueFn = this._defaultEntryValueFn;
			}
			if (!config.entryLabelFn) {
				config.entryLabelFn = this._defaultEntryLabelFn;
			}
			config.emptyEntry = {
			};

			config.entryObjectConfigs = {};
			this.callSuper(arguments, config);

			this.listen(config.items, "inserted", this.renderSelectItem, this);
			this.listen(config.items, "removed", this.removeSelectItem, this);
		},
		_defaultEntryValueFn: function (entry) {
			for (var i in entry) {
				return i;
			}
			return undefined;
		},
		_defaultEntryLabelFn: function (entry) {
			var label = LabelFunctions(entry);
			if (label) {
				return label;
			}
			for (var i in entry) {
				return entry[i];
			}
			return undefined;
		},
		destroy: function () {
			arguments.callee.$parent.destroy.call(this);
		},
		renderTo: function (renderTarget) {
			this.input = $(renderTarget).append("<select></select>").children().last();
			var _this = this;
			$(this.input).change(function () {
				var rawValue = $(_this.input).val();
				//console.log("setting raw value: "+rawValue);
				_this._setValue(rawValue);
				_this.inputChanged();
			});
			if (this.name) {
				$(this.input).attr("name", this.name);
			}
			if (this.id) {
				$(this.input).attr("id", this.id);
			}
			if (this.allowEmpty) {
				this.renderSelectItem(this.emptyEntry);
			}
			this.items.each(function (entry) {
				this.renderSelectItem(entry);
			}, this);
		},
		renderSelectItem: function (entry) {
			if (!this.allowEmpty && !this.getBoundValue()) {
				this.setBoundValue(entry);
			}

			var entryValue, entryLabel;
			if (entry === this.emptyEntry) {
				entryValue = null;
				entryLabel = "";
			} else {
				entryValue = this.entryValueFn(entry);
				entryLabel = this.entryLabelFn(entry);
			}
			var shouldPreselect = false;
			var bound = this.getBoundValue();
			if (bound) {
				if (this.entryValueFn === this._defaultEntryValueFn) {
					shouldPreselect = bound === entryValue;
				} else {
					shouldPreselect = this.entryValueFn(bound) === entryValue;
				}
			}

			var config = {};
			this.entryObjectConfigs[entryValue] = config;
			config.entry = entry;
			config.option = new Text({value: entryLabel, tag: "option"});
			config.option.renderTo(this.input);
			$(config.option.element).attr("value", entryValue);
			if (shouldPreselect) {
				$(this.input).children("option[selected]").removeAttr("selected");
				$(config.option.element).attr("selected", "");
			}
		},
		removeSelectItem: function (entry) {
			var entryValue = this.entryValueFn(entry);
			var config = this.entryObjectConfigs[entryValue];
			config.option.destroy();
		},
		getValue: function () {
			if (!this.rawValue) {
				this._setValue($(this.input).val());
			}

			// map raw value to actual value
			if (this.entryValueFn === this._defaultEntryValueFn) {
				// console.log("returning raw: " + this.rawValue);
				return this.rawValue;
			} else {
				var entry = this.entryObjectConfigs[this.rawValue].entry;
				if (entry === this.emptyEntry) {
					return null;
				}
				return entry;
			}
		},
		setValue: function (value) {
			var rawValue = value;
			if (this.entryValueFn !== this._defaultEntryValueFn) {
				rawValue = this.entryValueFn(value);
			}
			this._setValue(rawValue);
		},
		_setValue: function (rawValue) {
			this.rawValue = rawValue;
			var config = this.entryObjectConfigs[rawValue];
			$(this.input).val(rawValue);
		}
	});
	InputTypeRegistry.register("SelectInput", SelectInput);
	return SelectInput;
});
