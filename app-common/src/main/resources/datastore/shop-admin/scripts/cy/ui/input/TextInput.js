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
define(["cy/ui/input/InputViewComponent", "cy/ui/input/InputTypeRegistry"], function(InputViewComponent, InputTypeRegistry) {
    var TextInput = InputViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if (!config.type) {
                config.type = "text";
            }
            if (config.type === "number") {
                if (!config.step) {
                    config.step = "0.01";
                    if (config.entity && config.member) {
                    }
                }
            }
            this.callSuper(arguments, config);

        },
        destroy: function() {
            arguments.callee.$parent.destroy.call(this);
        },
        renderTo: function(renderTarget) {
            this.input = $(renderTarget).append("<input />").children().last();
			$(this.input).attr("type", this.type);
            if (this.placeholder) {
                $(this.input).attr("placeholder", this.placeholder);
            }
            if (this.disabled) {
                $(this.input).attr("disabled", "");
            }
            if (this.id) {
                $(this.input).attr("id", this.id);
            }
            if (this.step) {
                $(this.input).attr("step", this.step);
            }
            if (this.min !== undefined && this.min !== null) {
                $(this.input).attr("min", this.min);
            }
            if (this.max !== undefined && this.max !== null) {
                $(this.input).attr("max", this.max);
            }
            if (this.name) {
                $(this.input).attr("name", this.name);
            }
            if (this.cssClass) {
                $(this.input).addClass(this.cssClass);
            }
            var bv = this.getBoundValue();
            if (bv !== undefined && bv !== null) {
                this.setValue(bv);
            }

            var _this = this;
            $(this.input).change(function() {
                _this.inputChanged();
            });
        },
        getValue: function() {
            return $(this.input).val();
        },
        setValue: function(value) {
            if (value === undefined || value === null) {
                value = "";
            }
            $(this.input).val(value);
        },
        enable: function() {
            this.disabled = false;
            if (this.input) {
                $(this.input).removeAttr("disabled");
            }
        },
        disable: function() {
            this.disabled = true;
            if (this.input) {
                $(this.input).attr("disabled", "");
            }
        }
    });
    InputTypeRegistry.register("TextInput", TextInput);
    return TextInput;
});
