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
define(["cy/ui/ViewComponent", "cy/Collection", "cy/ui/container/ViewContainer", "cy/ui/NavigationBarItem", "cy/ui/Text"], function(ViewComponent, Collection, ViewContainer, NavigationBarItem, Text) {
	var NavigationBar = ViewContainer.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
			config.navigationContent = new Collection();

			if(config.items) {
				for(var i in config.items) {
					var item = config.items[i];
					if(item) {
						if (!(item instanceof NavigationBarItem)) {
							config.items[i] = new NavigationBarItem(item);
						}
					}
				}
			}
			
			this.callSuper(arguments, config);
		},
		renderTo: function(renderTarget) {
			this.navigationContent.clear();
			this.navBarOuter = $(renderTarget).append("<div class=\"navbar navbar-static-top\" />").children().last();
			this.navBarInner = $(this.navBarOuter).append("<div class=\"navbar-inner\" />").children().last();
			this.logo = new Text({value: this.brand, cls: "brand", tag: "a"});
			this.logo.renderTo($(this.navBarInner));
			this.navBarItems = $(this.navBarInner).append("<ul class=\"nav mainMenu\" />").children().last();
			this.contentHolder = $(renderTarget).append("<div/>").children().last();
			this.welcome = $(this.contentHolder).append("<div class=\"hero-unit\" style=\"background-color: transparent;\"></div>").children().last();
			this.welcomeTitle = new Text({value: "Hello.", tag: "h1"});
			this.welcomeTitle.renderTo(this.welcome);
			this.welcomeText = new Text({value: "Click on the navigation bar above in order to manage your eBX application.", tag: "p"});
			this.welcomeText.renderTo(this.welcome);
			this.callSuper(arguments, this.navBarItems);
		},
		renderItem: function(component) {
			var wrapper = $(this.contentHolder).append("<div />").children().last();
			$(wrapper).hide();
			this.callSuper(arguments, component);
			this.listen(component, "clicked", this.navigationItemClicked, this);
			if(component.view) {
				var viewInstance = new component.view();
				viewInstance.renderTo(wrapper);
			}

			this.navigationContent.add({
				navigationItem: component,
				wrapper: wrapper
			});
		},
		navigationItemClicked: function(navigationItem) {
			if(this.welcome) {
				this.welcomeText.destroy();
				this.welcomeTitle.destroy();
				$(this.welcome).remove();
				this.welcome = null;
			}
			this.navigationContent.each(function(config){
				var isSelected = config.navigationItem === navigationItem;
				config.navigationItem.setActive(isSelected);
				if(isSelected) {
					$(config.wrapper).show();
				} else {
					$(config.wrapper).hide();
				}
			}, this);
		}
	});
	return NavigationBar;
});
