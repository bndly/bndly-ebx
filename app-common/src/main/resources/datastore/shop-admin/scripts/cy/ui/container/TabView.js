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
define(["cy/ui/container/ViewContainer", "cy/Collection", "cy/ui/container/Tab", "cy/ui/Text"], function(ViewContainer, Collection, Tab, Text) {

	var TabView = ViewContainer.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			this.callSuper(arguments, config);
		},
		renderTo: function(renderTarget) {
			this.tabNav = $(renderTarget).append("<ul class=\"nav nav-pills\"></ul>").children().last();
			this.content = $(renderTarget).append("<div></div>").children().last();		
			
			// this.invokeSuper(arguments);
			arguments.callee.$parent.renderTo.call(this, this.content);
		},
		renderItem: function(component) {
			if(this.rendered) {				
				var isTab = component instanceof Tab;
				if(isTab) {
					arguments.callee.$parent.renderItem.call(this, component);
					component.hide();
					this.renderTabButtonForTab(component);

					if(component.activeByDefault) {
						this.showTab(component);
					}
				}
			}
		},
		showTab: function(tab) {
			if (this.activeTab) {
				this.activeTab.hide();
			}
			tab.show();
			this.activeTab = tab;
		},
		renderTabButtonForTab: function(tab) {
			var _this = this;
			var label = tab.labelText;
			if (!label) {
				label = tab.name;
			}
			tab.navItem = $(this.tabNav).append("<li></li>").children().last();
			var labelText = new Text({value: label, tag: "a"});
			labelText.renderTo(tab.navItem);
			$(tab.navItem).click(function() {
				// console.log("should switch tabs.");
				_this.showTab(tab);
				return false;
			});
	
		}
	});
	return TabView;
});
