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
define({
    PurchaseOrderRestBean: [{
            path: "orderNumber",
            labelText: "Number"
        }, {
            path: "address.firstName",
            labelText: "First name"
        }, {
            path: "address.lastName",
            labelText: "Last name"
        }, {
            path: "orderDate",
            labelText: "Date"
        }, {
            path: "totalGross",
            labelText: "Total gross"
        }, {
            path: "hasPaymentFulfillments",
            labelText: "Paid"
        }, {
            path: "hasBillingFailures",
            labelText: "Billing failed"
        }, {
            path: "hasCancelations",
            labelText: "Cancelled"
        }],
    BusinessProcessInstance: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "processName",
            labelText: "Process name"
        }, {
            path: "startTime",
            labelText: "Start",
            asDateTime: true
        }, {
            path: "endTime",
            labelText: "End",
            asDateTime: true
        }],
    RuleSetRestBean: [{
            path: "name",
            labelText: "Name"
        }],
    VariantRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "sku",
            labelText: "SKU"
        }, {
            path: "name",
            labelText: "Name"
        }, {
            path: "product.family.name",
            labelText: "Product family"
        }, {
            path: "product.name",
            labelText: "Product name"
        }],
    ProductRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "sku",
            labelText: "SKU"
        }, {
            path: "name",
            labelText: "Name"
        }, {
            path: "manufacturer.name",
            labelText: "Manufacturer"
        }],
    TranslatedObjectRestBean: [{
            path: "translationKey",
            labelText: "Key"
        }],
    ProductAttributeRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "name",
            labelText: "Technical Key"
        }, {
            path: "label",
            labelText: "Natural Name"
        }],
    ValueAddedTaxRestBean: [{
            path: "name",
            labelText: "Name"
        }, {
            path: "description",
            labelText: "Description"
        }, {
            path: "value",
            labelText: "Percent"
        }],
    QuantityUnitRestBean: [{
            path: "description",
            labelText: "Name"
        }, {
            path: "abbrevation",
            labelText: "Abbrevation"
        }, {
            path: "quantity",
            labelText: "Quantity"
        }],
    StockItemRestBean: [{
            path: "sku",
            labelText: "SKU"
        }, {
            path: "stock",
            labelText: "Stock"
        }],
    ManufacturerRestBean: [{
            path: "name",
            labelText: "Name"
        }, {
            path: "description",
            labelText: "Description"
        }, {
            path: "homepage",
            labelText: "Homepage"
        }],
    CurrencyRestBean: [{
            path: "code",
            labelText: "Code"
        }, {
            path: "symbol",
            labelText: "Symbol"
        }, {
            path: "decimalPlaces",
            labelText: "Decimal places"
        }],
    ShipmentRestBean: [{
            path: "number",
            labelText: "Number"
        }, {
            path: "date",
            labelText: "Date"
        }, {
            path: "orderNumber",
            labelText: "Order number"
        }, {
            path: "orderDate",
            labelText: "Order date"
        }],
    ShipmentModeRestBean: [{
            path: "name",
            labelText: "Name"
        }, {
            path: "label",
            labelText: "Label"
        }],
    SalutationRestBean: [{
            path: "internalName",
            labelText: "Internal name"
        }, {
            path: "label",
            labelText: "Label"
        }],
    PersonTitleRestBean: [{
            path: "internalName",
            labelText: "Internal name"
        }, {
            path: "label",
            labelText: "Label"
        }],
    LanguageRestBean: [{
            path: "name",
            labelText: "Name"
        }],
    PropertySetRestBean: [{
            path: "name",
            labelText: "Name"
        }],
    CreditCardBrandRestBean: [{
            path: "name",
            labelText: "Name"
        }, {
            path: "label",
            labelText: "Label"
        }],
    ProductFamilyRestBean: [{
            path: "name",
            labelText: "Name"
        }],
    ProductAttributeValueRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "productAttribute.name",
            labelText: "Attribute"
        }, {
            path: "stringValue",
            labelText: "Technical Key"
        }, {
            path: "label",
            labelText: "Natural Name"
        }],
    VariantDetailRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "productAttributeValue.productAttribute.name",
            labelText: "Attribute"
        }, {
            path: "productAttributeValue.stringValue",
            labelText: "Value"
        }],
    PersonRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "email",
            labelText: "Email"
        }, {
            path: "externalUserId",
            labelText: "User ID"
        }],
    WishListRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "name",
            labelText: "Name"
        }, {
            path: "privacy",
            labelText: "Privacy"
        }],
    CartRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "userIdentifier",
            labelText: "User ID"
        }, {
            path: "merchandiseValueGross",
            labelText: "Value"
        }, {
            path: "lastInteraction",
            labelText: "Last Interaction"
        }],
    UserRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "identifier",
            labelText: "User ID"
        }],
    Data: [{
            path: "name",
            labelText: "Name"
        }, {
            path: "contentType",
            labelText: "Content type"
        }, {
            path: "createdOn",
            labelText: "Created on"
        }, {
            path: "updatedOn",
            labelText: "Updated on"
        }],
    BusinessProcessDefinition: [{
            path: "name",
            labelText: "Name"
        }, {
            path: "key",
            labelText: "Key"
        }, {
            path: "id",
            labelText: "ID"
        }, {
            path: "version",
            labelText: "Version"
        }, {
            path: "deploymentId",
            labelText: "Deployment ID"
        }],
    CheckoutRequestRestBean: [{
            path: "id",
            labelText: "ID"
        }, {
            path: "createdOn",
            labelText: "Created on"
        }, {
            path: "processId",
            labelText: "Process ID"
        }, {
            path: "paymentResult",
            labelText: "Payment result"
        }, {
            path: "order.orderNumber",
            labelText: "Order No."
        }, {
            path: "order.address.firstName",
            labelText: "First name"
        }, {
            path: "order.address.lastName",
            labelText: "Last name"
        }]
});
