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
define(["./AddressRestBean"], function(AddressRestBean) {
    return [
        {
            items: [{
                    member: "number",
                    labelText: "Number"
                },
                {
                    member: "date",
                    labelText: "Date"
                },
                {
                    member: "orderNumber",
                    labelText: "Order number"
                },
                {
                    member: "orderDate",
                    labelText: "Order date",
                    generated: true
                }, {
                    member: "address",
                    labelText: "Recipient address",
                    config: {
                        sections: AddressRestBean
                    },
                    embedded: true
                }, {
                    member: "senderAddress",
                    labelText: "Sender address",
                    config: {
                        sections: AddressRestBean
                    },
                    embedded: true
                }, {
                    member: "shipmentItems",
                    labelText: "Items",
                    config: {
                        sections: [
                            {
                                items: [
                                    {
                                        member: "items",
                                        labelText: "Items"
                                    }
                                ]
                            }
                        ]
                    },
                    embedded: true
                }]
        }
    ];

});
