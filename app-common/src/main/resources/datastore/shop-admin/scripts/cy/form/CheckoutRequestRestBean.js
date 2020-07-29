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
define([],function() {
    return [
        {
            items: [{
                    member: "processId",
                    labelText: "Process ID",
                    generated: true
                },
                {
                    member: "paymentResult",
                    labelText: "Payment result",
                    generated: true
                },
                {
                    member: "serviceLink",
                    labelText: "Service Link",
                    generated: true
                },
                {
                    member: "order",
                    labelText: "Order",
                    mandatory: true,
                    typeahead: true
                },
                {
                    member: "paymentConfiguration",
                    labelText: "Payment configuration",
                    typeahead: true,
                    generated: true
                },
                {
                    member: "createdOn",
                    labelText: "Created on"
                },
                {
                    member: "updatedOn",
                    labelText: "Updated on"
                }]
        }
    ];

});
