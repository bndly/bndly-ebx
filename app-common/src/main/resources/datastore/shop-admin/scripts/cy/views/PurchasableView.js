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
define(["cy/views/TwoColumnView", "cy/Navigation", "cy/views/CMSImporterView"], function(TwoColumnView, Navigation, CMSImporterView) {
    var PurchasableView = TwoColumnView.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.nav = new Navigation({
                sections: [
                    {
                        name: "stuff to buy",
                        items: [
                            {
                                labelText: "Products",
                                entityType: "ProductRestBean"
                            },
                            {
                                labelText: "Variants",
                                entityType: "VariantRestBean"
                            }
                        ]
                    },
                    {
                        name: "variant configuration",
                        items: [
                            {
                                labelText: "Dynamic Properties",
                                entityType: "DynamicProductPropertyRestBean"
                            },
                            {
                                labelText: "Product Attributes",
                                entityType: "ProductAttributeRestBean"
                            },
                            {
                                labelText: "Product families",
                                entityType: "ProductFamilyRestBean"
                            }
                        ]
                    },
                    {
                        name: "CMS Importer",
                        items: [
                            {
                                labelText: "Status",
                                view: new CMSImporterView()
                            }
                        ]
                    },
                    {
                        name: "Other",
                        items: [
                            {
                                labelText: "Taxes",
                                entityType: "ValueAddedTaxRestBean"
                            },
                            {
                                labelText: "Quantity units",
                                entityType: "QuantityUnitRestBean"
                            },
                            {
                                labelText: "Stock",
                                entityType: "StockItemRestBean"
                            },
                            {
                                labelText: "Manufacturers",
                                entityType: "ManufacturerRestBean"
                            },
                            {
                                labelText: "Currencies",
                                entityType: "CurrencyRestBean"
                            }
                        ]
                    }
                ]
            });

            this.callSuper(arguments, config);
        },
        renderTo: function(renderTarget) {
            this.invokeSuper(arguments);

            this.nav.contentRenderTarget = this.rightColumn;
            this.nav.renderTo(this.leftColumn);
        }
    });
    return PurchasableView;
});
