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
define(["cy/EntityRelationViewComponent", "cy/RestBeans", "cy/LabelFunctions", "cy/ui/input/DropDownButton", "cy/ui/container/Form", "cy/ui/input/TextInput", "cy/ui/input/SelectInput", "cy/Collection", "cy/FieldSet", "cy/views/SimplePriceView", "cy/views/StaggeredPriceView","cy/EntityCollection"], function(EntityRelationViewComponent, RestBeans, LabelFunctions, DropDownButton, Form, TextInput, SelectInput, Collection, FieldSet, SimplePriceView, StaggeredPriceView, EntityCollection) {
	var PriceModelView = EntityRelationViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			var newButton = new DropDownButton({
				label: "new",
                                items: [
                                    {
                                        name: "SimplePrice",
                                        label: "Simple Price"
                                    },
                                    {
                                        name: "StaggeredPrice",
                                        label: "Staggered Price"
                                    }
                                ],
                                listeners:{
                                    clicked: {
                                        fn: this.newPriceButtonClicked,
                                        scope: this
                                    }
                                }
			});
                        if(!config.taxModels) {
                            config.taxModels = new EntityCollection({
                                proto: new RestBeans["ValueAddedTaxRestBean"]()
                            });
                        }
                        
                        if(!config.currencies) {
                            config.currencies = new EntityCollection({
                                proto: new RestBeans["CurrencyRestBean"]()
                            });
                        }
                        
			config.form = new Form();
			config.items = [
				newButton,
				config.form
			];
			this.callSuper(arguments, config);
                        this.taxModels.load();
                        this.currencies.load();
		}, 
		renderTo: function(renderTarget) {
                    this.invokeSuper(arguments);
                    this.renderPricesOfParent();
		},
                newPriceButtonClicked: function(button, item){
                    var type = RestBeans[item.name+"RestBean"];
                    if(!type) {
                        console.log("unsupported price type: "+item.name);
                    } else {
                        var price = new type();
                        this.priceItems.add(price);
                        this.renderPrice(price);
                    }
                },
                renderPricesOfParent: function() {
                    var pm = this.parent.getPriceModel();
                    if(!pm) {
                        pm = new RestBeans["PriceModelRestBean"]();
                        this.parent.setPriceModel(pm);
                    }
                    var prices = pm.getPrices();
                    if(!prices) {
                        prices = new RestBeans["PriceListRestBean"]();
                        pm.setPrices(prices);
                    }
                    this.priceItems = prices.getItems();
                    if(!this.priceItems) {
                        this.priceItems = new Collection();
                        prices.setItems(this.priceItems);
                    }
                    this.priceItems.each(function(price){
                        this.renderPrice(price);
                    }, this);
                },
                renderPrice: function(price) {
                    var viewType;
                    if(price instanceof RestBeans["SimplePriceRestBean"]) {
                        viewType = SimplePriceView;
                    } else if(price instanceof RestBeans["StaggeredPriceRestBean"]) {
                        viewType = StaggeredPriceView;
                    } else if(price instanceof RestBeans["ParameterizedPriceRestBean"]) {
                        console.log("rendering parameterized price");
                    }
                    
                    if(viewType) {
                        var view = new viewType({
                            price: price,
                            taxModels: this.taxModels,
                            currencies: this.currencies,
                            listeners: {
                                removed: {
                                    fn: this.priceRemoved,
                                    scope: this
                                }
                            }
                        });
                        this.form.items.add(view);
                    }
                },
                priceRemoved: function(priceView) {
                    var price = priceView.price;
                    this.priceItems.remove(price);
                    this.form.items.remove(priceView);
                }
	});
	return PriceModelView;

});
