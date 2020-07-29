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
define(function() {
    return [{
        items: [{
                member: "salutation",
                labelText: "Salutation",
                mandatory: true
            }, {
                member: "title",
                labelText: "Title"
            }, {
                member: "firstName",
                labelText: "First name",
                mandatory: true
            }, {
                member: "lastName",
                labelText: "Last name",
                mandatory: true
            }, {
                member: "street",
                labelText: "Street",
                mandatory: true
            }, {
                member: "houseNumber",
                labelText: "House No."
            }, {
                member: "additionalInfo",
                labelText: "Additional info"
            }, {
                member: "postCode",
                labelText: "Postcode",
                mandatory: true
            }, {
                member: "city",
                labelText: "City",
                mandatory: true
            }, {
                member: "country",
                labelText: "Country",
                mandatory: true
            }]
    }];
});
