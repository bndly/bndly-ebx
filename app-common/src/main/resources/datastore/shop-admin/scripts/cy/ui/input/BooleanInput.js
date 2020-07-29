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
    var BooleanInput = InputViewComponent.extend({
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
                }
            }
            this.callSuper(arguments, config);

        },
        destroy: function() {
            arguments.callee.$parent.destroy.call(this);
        },
        renderTo: function(renderTarget) {
            this.input = $(renderTarget).append("<input type=\"checkbox\" />").children().last();
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
            if (this.name) {
                $(this.input).attr("name", this.name);
            }
            if (this.getBoundValue()) {
                this.setValue(this.getBoundValue());
            }

            var _this = this;
            $(this.input).change(function() {
                _this.inputChanged();
            });
        },
        getValue: function() {
            return $(this.input).prop("checked");
        },
        setValue: function(value) {
            if (!value) {
                value = false;
            }
            return $(this.input).prop("checked", value);
        }
    });
    InputTypeRegistry.register("BooleanInput", BooleanInput);
    return BooleanInput;
});
