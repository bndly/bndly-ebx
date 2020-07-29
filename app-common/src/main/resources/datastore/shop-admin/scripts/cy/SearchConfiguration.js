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
define(["cy/RestBeans"], function(RestBeans) {
    var fn = function(typeName) {
        if(!typeName) {
            return undefined;
        }
        if(fn[typeName]) {
            return fn[typeName];
        } else {
            var proto = new RestBeans[typeName]();
            var rt = proto.refType();
            if(!rt) {
                return undefined;
            } else {
                var refTypeName = new rt().clazzName();
                if(refTypeName === typeName) {
                    return undefined;
                } else {
                    return fn(refTypeName);
                }
                
            }
        }
    };
    fn.ProductReferenceRestBean = {
        listType: "ProductListRestBean",
        rootLink: "Product",
        fields: ["AbstractProduct_name"],
        sortBy: "name"
    };
    fn.PurchaseOrderReferenceRestBean = {
        listType: "PurchaseOrderListRestBean",
        rootLink: "PurchaseOrder",
        fields: ["PurchaseOrder_orderNumber"],
        sortBy: "orderNumber"
    };
    fn.WishListReferenceRestBean = {
        listType: "WishListListRestBean",
        rootLink: "WishList",
        sortBy: "name"
    };
    fn.StockItemReferenceRestBean = {
        listType: "StockItemListRestBean",
        rootLink: "StockItem",
        fields: ["StockItem_sku"],
        sortBy: "sku"
    };
    fn.TranslatedObjectReferenceRestBean = {
        listType: "TranslatedObjectListRestBean",
        rootLink: "TranslatedObject",
        fields: ["TranslatedObject_translationKey"],
        sortBy: "translationKey"
    };
    fn.PersonReferenceRestBean = {
        listType: "PersonListRestBean",
        rootLink: "Person",
        fields: ["Person_email"],
        sortBy: "email"
    };
    fn.ProductAttributeReferenceRestBean = {
        listType: "ProductAttributeListRestBean",
        rootLink: "ProductAttribute",
        fields: ["ProductAttribute_name"],
        sortBy: "name"
    };
    fn.ProductFamilyReferenceRestBean = {
        listType: "ProductFamilyListRestBean",
        rootLink: "ProductFamily",
        fields: ["ProductFamily_name"],
        sortBy: "name"
    };
    return fn;
});
