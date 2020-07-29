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
define(["cy/ui/Text", "cy/Value"], function(Text, Value) {
    var Badge = Text.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if(!config.value || typeof(config.value) === "string") {
                config.value = new Value({
					value: config.value
				});
			}
			config.tag = "span";
			config.cls = config.cls ? "badge "+config.cls : "badge";
            this.callSuper(arguments, config);
        },
        renderTo: function(renderTarget) {
            this.invokeSuper(arguments);
        },
        destroy: function() {
            this.invokeSuper(arguments);
        }
    });
    return Badge;
});
