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
define(["cy/ui/container/ViewContainer", "cy/ui/input/Button", "cy/FieldSet", "cy/FormBinder", "cy/form/AddressRestBean", "cy/RestBeans", "cy/ui/container/Form", "cy/ui/container/InputViewContainer", "cy/ui/IFrame"], function(ViewContainer, Button, FieldSet, FormBinder, AddressRestBean, RestBeans, Form, InputViewContainer, IFrame) {
    var PurchaseOrderDocumentsView = ViewContainer.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            var buttonsDisabled = true;
            if (config.parent.hasLink("self")) {
                buttonsDisabled = false;
            }
            config.donwloadInvoiceButton = new Button({
                label: "download invoice",
                disabled: buttonsDisabled,
                listeners: {
                    clicked: {
                        fn: this.downloadInvoice,
                        scope: this
                    }
                }
            });
            config.downloadShipmentButton = new Button({
                label: "download shipment",
                disabled: buttonsDisabled,
                listeners: {
                    clicked: {
                        fn: this.downloadShipment,
                        scope: this
                    }
                }
            });
            config.items = [
                config.donwloadInvoiceButton,
                config.downloadShipmentButton
            ];
            this.callSuper(arguments, config);
            this.listen(config.parent, "persisted", this.orderPersisted, this);
        },
        destroy: function() {
            this.invokeSuper(arguments);
        },
        orderPersisted: function(order) {
            this.donwloadInvoiceButton.enable();
            this.downloadShipmentButton.enable();
        },
        renderTo: function(renderTarget) {
            this.invokeSuper(arguments);
        },
        downloadShipment: function(button) {
            this.parent.follow({
                rel: "shipment",
                cb: function(shipment) {
                    this.openPDF(shipment);
                },
                scope: this
            });
        },
        downloadInvoice: function(button) {
            this.parent.follow({
                rel: "invoice",
                cb: function(invoice) {
                    this.openPDF(invoice);
                },
                scope: this
            });
        },
        openPDF: function(entity) {
            if (entity) {
                var link = entity.hasLink("print");
                if (link) {
                    this.items.add(new IFrame({
                        url: link.getHref(),
                        hidden: true
                    }));
                }
            } else {
                console.warn("no entity for printing was provided");
            }
        }
    });
    return PurchaseOrderDocumentsView;

});
