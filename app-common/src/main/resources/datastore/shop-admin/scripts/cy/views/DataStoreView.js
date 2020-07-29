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
	"cy/RestBeans",
	"cy/ui/input/Button", 
	"cy/ui/container/ViewContainer", 
	"cy/ui/ViewComponent", 
	"cy/ui/Text",
	"cy/ui/container/TabView",
	"cy/ui/container/Tab",
	"cy/ui/input/DropZoneInput",
	"cy/TableBinder",
	"cy/TableDataProvider"
], function(
		RestBeans, 
		Button, 
		ViewContainer, 
		ViewComponent, 
		Text,
		TabView,
		Tab,
		DropZoneInput,
		TableBinder,
		TableDataProvider
) {
    var DataStoreView = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.titleText = new Text({value: "Data Stores", tag: "h1"});
            config.container = new ViewContainer({
                items: [
                ]
            });
            this.callSuper(arguments, config);
			this.tabView = new TabView();
			this.container.items.add(this.tabView);
            RestBeans.root.follow({
                rel: "dataStores",
                cb: this.dataStoresLoaded,
                scope: this
            });
        },
        destroy: function() {
            this.invokeSuper(arguments);
			this.titleText.destroy();
        },
        renderTo: function(renderTarget) {
            this.renderTitle(renderTarget);
            this.container.renderTo(renderTarget);
            this.invokeSuper(arguments);
        },
        renderTitle: function(renderTarget) {
            if (this.titleText) {
                this.titleText.renderTo(renderTarget);
            }
        },
        dataStoresLoaded: function(dataStores) {
            this.dataStores = dataStores;
			this.dataStores.getItems().each(function(item, index){
				this.tabView.items.add(new Tab({
					labelText: item.getName(),
					items: [
						new DropZoneInput({
							uploadLocation: "/bndly/data/"+item.getName()+"/bin/"
						}),
						new TableBinder({
							columns:[
								{
									labelText: "Name",
									path: "name"
								},
								{
									labelText: "Content-Type",
									path: "contentType"
								},
								{
									labelText: "Created",
									path: "createdOn"
								},
								{
									labelText: "Updated",
									path: "updatedOn"
								}
							],
							dataProvider: new TableDataProvider({
								pageOffset: 0,
								pageSize: 1,
								loadPage: function(){
									var _this = this;
									item.follow({rel:"items", cb: function(dataStoreItems){
										_this.setPageSize(dataStoreItems.getItems().size());
										_this.fireEvent("load", dataStoreItems.getItems());
									}});
								}
							})
						})
					]
				}));
			}, this);
        }
    });
    return DataStoreView;

});
