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
define(["cy/ui/ViewComponent", "cy/Collection"], function(ViewComponent, Collection) {
	var ViewContainer = ViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
			if(!config.items) {
				config.items = new Collection();
			} else if(!(config.items instanceof Collection)) {
				config.items = new Collection(config.items);
			}
			
			this.callSuper(arguments, config);

			this.listen(config.items, "inserted", this.renderItemOnAdd, this);
			this.listen(config.items, "removed", this.removeItem, this);
		},
		renderTo: function(renderTarget) {
			if(!this.renderTarget) {
				this.renderTarget = renderTarget;	
			}
			this.invokeSuper(arguments);
			this.renderItems();
		},
		renderItems: function() {
			if(this.rendered) {
				this.items.each(function(entry) {
					this.renderItem(entry);
				}, this);	
			}
		},
		renderItemOnAdd: function(component) {
			this.renderItem(component);
		},
		renderItem: function(component, renderTarget) {
			renderTarget = renderTarget ? renderTarget : this.renderTarget;
			if(this.rendered) {
				var isViewComponent = component instanceof ViewComponent;
				if(isViewComponent) {
					component.renderTo(renderTarget);
				}	
			}
		},
		removeItem: function(component) {
			component.destroy();
		},
		destroy: function() {
			this.items.each(function(entry) {
				entry.destroy();
			}, this);

			this.invokeSuper(arguments);
		}
	});
	return ViewContainer;
});
