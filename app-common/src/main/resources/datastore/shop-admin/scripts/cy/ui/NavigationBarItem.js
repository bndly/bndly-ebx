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
define(["cy/ui/ViewComponent", "cy/Value", "cy/ui/Text"], function(ViewComponent, Value, Text) {
	var NavigationBarItem = ViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
			if(!config.label instanceof Value) {
				config.label = new Value({value: config.label});
			}
			this.labelText = new Text({value: config.label});
			this.callSuper(arguments, config);
		},
		destroy: function() {
			this.invokeSuper(arguments);
			this.labelText.destroy();
		},
		setActive: function(active) {
			if(this.wrapper) {
				if(active) {
					this.wrapper.addClass("active");
				} else {
					this.wrapper.removeClass("active");
				}	
			}
		},
		renderTo: function(renderTarget) {
			this.wrapper = $(renderTarget).append("<li></li>").children().last();
			this.element = $(this.wrapper).append("<a href=\"#\"></a>").children().last();
			this.labelText.renderTo(this.element);
			var _this = this;
			$(this.element).click(function(){
				_this.clicked();
				return false;
			});
		},
		clicked: function() {
			this.fireEvent("clicked", this);
		}
	});
	return NavigationBarItem;
});
