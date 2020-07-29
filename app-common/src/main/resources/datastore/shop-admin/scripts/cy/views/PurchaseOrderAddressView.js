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
define(["cy/ui/container/ViewContainer", "cy/ui/input/Button", "cy/FieldSet", "cy/FormBinder", "cy/form/AddressRestBean", "cy/RestBeans", "cy/ui/container/Form", "cy/ui/container/InputViewContainer"], function(ViewContainer, Button, FieldSet, FormBinder, AddressRestBean, RestBeans, Form, InputViewContainer) {
    var PurchaseOrderAddressView = ViewContainer.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            var deliveryAddressHidden,billingAddressHidden;
            config.order = config.parent;
            config.address = config.order.getAddress();
            if (!config.address) {
                config.address = new RestBeans["AddressRestBean"]();
                config.order.setAddress(config.address);
            }
            config.billingAddress = config.order.getBillingAddress();
            if (!config.billingAddress) {
                billingAddressHidden = true;
                config.billingAddress = new RestBeans["AddressRestBean"]();
            } else {
                billingAddressHidden = config.billingAddress.hasLink("self") != null;
            }
            config.deliveryAddress = config.order.getDeliveryAddress();
            if (!config.deliveryAddress) {
                deliveryAddressHidden = true;
                config.deliveryAddress = new RestBeans["AddressRestBean"]();
            } else {
                deliveryAddressHidden = config.deliveryAddress.hasLink("self") != null;
            }
            config.items = [
                this.buildAddressForm("address", "Address", config.address, false),
                this.buildAddressForm("billingAddress", "Billing Address", config.billingAddress, true, billingAddressHidden),
                this.buildAddressForm("deliveryAddress", "Delivery Address", config.deliveryAddress, true, deliveryAddressHidden)
            ];
            this.callSuper(arguments, config);
        },
        buildAddressForm: function(member, title, address, optional, hidden) {
            var rawItems = [], ivc = new InputViewContainer({
                items: rawItems,
                hidden: hidden
            }), fs = new FieldSet({
                legend: title,
                items: [ivc]
            });
            
            var items = [fs];

            new FormBinder({
                entity: address,
                sections: AddressRestBean
            }).renderMembersToForm(address, rawItems, {});
            
            if (optional) {
                fs.items.add(new Button({
                    label: "edit",
                    listeners: {
                        clicked: {
                            fn: function(button) {
                                ivc.show();
                                this.editAddress(button, member);
                            },
                            scope: this
                        }
                    }
                }));
                fs.items.add(new Button({
                    label: "delete",
                    listeners: {
                        clicked: {
                            fn: function(button) {
                                ivc.hide();
                                this.removeAddress(button, member);
                            },
                            scope: this
                        }
                    }
                }));
            }
            
            return new Form({
                items: items
            });
        },
        destroy: function() {
            this.invokeSuper(arguments);
        },
        renderTo: function(renderTarget) {
            this.invokeSuper(arguments);
        },
        removeAddress: function(button, member) {
            this.order.set(member, null);
        },
        editAddress: function(button, member) {
            this.order.set(member, this[member]);
        }
    });
    return PurchaseOrderAddressView;

});
