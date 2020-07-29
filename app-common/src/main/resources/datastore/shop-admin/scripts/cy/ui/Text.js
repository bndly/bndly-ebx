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
define(["cy/ui/ViewComponent", "cy/Value"], function(ViewComponent, Value) {
    var Text = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if(!config.value || typeof(config.value) === "string") {
                config.value = new Value({
					value: config.value
				});
			}
			if(!config.tag || typeof(config.tag) !== "string") {
				config.tag = "span";
			}
			config.value.addListener("changed", this.valueHasChanged, this);
            this.callSuper(arguments, config);
        },
        renderTo: function(renderTarget) {
            this.element = $(renderTarget).append("<"+this.tag+"></"+this.tag+">").children().last();
			if(this.cls && typeof(this.cls) === "string") {
				$(this.element).attr("class", this.cls);
			}
            this.rendered = true;
			this.valueHasChanged(this.value.get(), this.value.get(), this.value);
            if(this.hidden) {
                this.hide();
            }
        },
		setCls: function(cls) {
			this.cls = cls;
			if(this.cls && typeof(this.cls) === "string") {
				$(this.element).attr("class", this.cls);
			}
		},
		valueHasChanged: function(newValue) {
			if(newValue === null || newValue === undefined) {
				newValue = "";
			}
			$(this.element).text(newValue);
		},
        hide: function(){
            if(this.element) {
                $(this.element).hide();
            }
        },
        show: function(){
            if(this.element) {
                $(this.element).show();
            }
        },
        destroy: function() {
            this.invokeSuper(arguments);
			this.value.removeListener("changed", this.valueHasChanged, this);
            if (this.element) {
                $(this.element).remove();
            }
        }
    });
    return Text;
});
