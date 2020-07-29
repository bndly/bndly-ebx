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
define(["cy/ui/ViewComponent", "cy/ui/container/ViewContainer", "cy/FormBinder", "cy/Collection", "cy/ui/container/Tab", "cy/form/Forms", "cy/EntityTableDataProvider", "cy/TableConfigurations", "cy/TableBinder", "cy/ui/container/TabView", "cy/RestBeans", "cy/Relation", "cy/views/RelationView", "cy/views/SelfRelationView"], function(ViewComponent, ViewContainer, FormBinder, Collection, Tab, Forms, EntityTableDataProvider, TableConfigurations, TableBinder, TabView, RestBeans, Relation, RelationView, SelfRelationView) {

	var RelationFormBinder = ViewContainer.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			// wrap the relations into an observable collection
			var tabView = new TabView();
			config.relations = new Collection(config.relations);
			config.items = [
				tabView
			];
			config.keyedInputs = {};

			this.callSuper(arguments, config);
			
			config.relations.each(function(relationDescriptor) {
				var tabItem = this.buildTabItemFromRelation(relationDescriptor);
				if(tabItem) {
					var activeByDefault = tabItem instanceof SelfRelationView,
					tab = new Tab({
						name: relationDescriptor.name,
						labelText: relationDescriptor.labelText,
						activeByDefault: activeByDefault,
						items: [
							tabItem
						]
					});
					tabView.items.add(tab);	
				}
			}, this);
		},
		buildTabItemFromRelation: function(relationDescriptor) {
			if(relationDescriptor.link) {
				if (relationDescriptor.link === "self") {
					// new SelfRelationView
					return new SelfRelationView({
						entity: this.entity,
						keyedInputs: this.keyedInputs
					});
				} else {
					// new RelationView
					return new RelationView({
						entity: this.entity,
						keyedInputs: this.keyedInputs
					});
				}	
			} else if(relationDescriptor.viewType) {
				// custom view
				var cfg = relationDescriptor.viewConfig;
				if(!cfg) {
					cfg = {};
				}
				cfg.parent = this.entity;
				cfg.keyedInputs = this.keyedInputs
				var view = new relationDescriptor.viewType(cfg);
				if(view instanceof ViewComponent) {
					return view;
				} else {
					return undefined;
				}
			} else {
				console.warn("no link relation oder viewType for relation");
				return undefined;
			}
		}
	});
	return RelationFormBinder;
});
