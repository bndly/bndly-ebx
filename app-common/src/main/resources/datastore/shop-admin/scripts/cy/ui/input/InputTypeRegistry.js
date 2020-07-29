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
define([
], function(
        ) {
    var InputTypeRegistry = {};
    InputTypeRegistry.inputTypes = {};
    InputTypeRegistry.construct = function(key, config){
        var constructor = InputTypeRegistry.getInputConstructor(key);
        return new constructor(config);
    };
    InputTypeRegistry.getInputConstructor = function(key){
		// if(typeof(key) === "function") {
		// 	return key
		// }
        if(!InputTypeRegistry.inputTypes[key]) {
            console.warn("no input type for key "+key);
            return undefined;
        }
        return InputTypeRegistry.inputTypes[key];
    };
    InputTypeRegistry.register = function(key, constructor){
        if(!key) {
            console.error("missing key while registering constructor");
            return false;
        }
        if(!constructor) {
            console.error("missing constructor while registering constructor for key "+key);
            return false;
        }
        if(InputTypeRegistry.inputTypes[key]) {
            console.warn("overwriting input type for key "+key);
        }
        InputTypeRegistry.inputTypes[key] = constructor;
        return true;
    };
    return InputTypeRegistry;
});
