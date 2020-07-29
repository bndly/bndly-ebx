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
define(["cy/views/TwoColumnView", "cy/Navigation"], function(TwoColumnView, Navigation) {
    var PurchasedView = TwoColumnView.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.nav = new Navigation({
                sections: [
                    {
                        name: "Orders",
                        items: [
                            {
                                labelText: "Orders",
                                entityType: "PurchaseOrderRestBean"
                            },
                            {
                                labelText: "Checkouts",
                                entityType: "CheckoutRequestRestBean"
                            }
                        ]
                    },
                    {
                        name: "Shipments",
                        items: [
                            {
                                labelText: "Shipments",
                                entityType: "ShipmentRestBean"
                            }
                        ]
                    },
                    {
                        name: "Checkout process",
                        items: [
                            {
                                labelText: "Shipment modes",
                                entityType: "ShipmentModeRestBean"
                            },
                            {
                                labelText: "Salutations",
                                entityType: "SalutationRestBean"
                            },
                            {
                                labelText: "Creditcard brands",
                                entityType: "CreditCardBrandRestBean"
                            },
                            {
                                labelText: "Person titles",
                                entityType: "PersonTitleRestBean"
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
    return PurchasedView;
});
