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
define(["cy/ui/input/TextInput", "cy/ui/input/InputTypeRegistry"], function(TextInput, InputTypeRegistry) {
    var DateInput = TextInput.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.type = "date";
            if(!config.placeholder) {
                config.placeholder = "yyyy-mm-dd";
            }
            this.callSuper(arguments, config);
        },
        destroy: function() {
            arguments.callee.$parent.destroy.call(this);
        },
        renderTo: function(renderTarget) {
            this.callSuper(arguments, renderTarget);
            var _this = this;
            $(this.input).blur(function(){
                var v = _this.getValue();
                if(v === undefined) {
                    _this.setValue(null);
                }
            });
        },
        getValue: function() {
            var raw = $(this.input).val();
//            console.log("getting date value raw: "+raw);
            if(raw === "") {
                // invalid input
                return undefined;
            }
            if(!raw) {
                raw = null;
            } else {
                var s = raw.split("-");
                var d = new Date(s[0], s[1]-1, s[2], 0, 0, 0, 0);
//                console.log("getting date value: "+d);
                raw = d;
            }
            this._value = raw;
            return raw;
        },
        setValue: function(value) {
            if(value === undefined) {
                return false;
            }
//            console.log("setting date value: "+value);
            if (!value) {
                this._value = null;
                this.callSuper(arguments, value);
            } else {
                if (!(value instanceof Date)) {
                    value = null;
                }
                this._value = value;
                if(value) {
                    var tmp = [];
                    tmp[0] = 1900+value.getYear();
                    tmp[1] = value.getMonth()+1;
                    if(tmp[1] < 10) {
                        tmp[1] = "0"+tmp[1];
                    }
                    tmp[2] = value.getDate();
                    if(tmp[2] < 10) {
                        tmp[2] = "0"+tmp[2];
                    }
                    value = tmp.join("-");
                }
//                console.log("setting date value raw: "+value);
                this.callSuper(arguments, value);
            }

        }
    });
    InputTypeRegistry.register("DateInput", DateInput);
    return DateInput;
});
