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
define(["cy/StringUtil", "cy/Expression"],function(StringUtil, Expression) {
    var labelFn = function(entity) {
        if (!entity) {
            return undefined;
        }
        var fn = labelFn[entity.clazzName()];
        if (fn) {
            return fn(entity);
        }
        return undefined;
    };

    labelFn.ProductRestBean = function(entity) {
        var a = [];
        if (entity.getSku()) {
            a.push(entity.getSku());
        }
        if (entity.getName()) {
            a.push(entity.getName());
        }
        return a.join(" ");
    };
    labelFn.PropertyRestBean = function(entity) {
        if(!entity.getName() && !entity.getActualValue()) {
            return "";
        }
        var v = entity.getName()+"="+entity.getActualValue();
        if(entity.getLocale()) {
            v += " ("+entity.getLocale()+")";
        }
        return v;
    };
    labelFn.ValueAddedTaxRestBean = function(entity) {
        return entity.getName();
    };
    labelFn.UserAttributeRestBean = function(entity) {
        return entity.getName();
    };
    labelFn.QuantityUnitRestBean = function(entity) {
        return entity.getDescription();
    };
    labelFn.LineItemRestBean = function(entity) {
        return entity.getSku() + " - " + entity.getProductName();
    };
    labelFn.WishListItemRestBean = function(entity) {
        var r = entity.getDesiredAmount() + "x " + entity.getSku();;
        if(entity.getCoremediaProductName()) {
            r+= " - " + entity.getCoremediaProductName();
        }
        return r ;
    };
    labelFn.WishListItemPriorityRestBean = function(entity) {
        return entity.getName();
    };
    labelFn.CartItemRestBean = function(entity) {
        return entity.getQuantity() + "x " + entity.getSku();
    };
    labelFn.ManufacturerRestBean = function(entity) {
        return entity.getName();
    };
    labelFn.PurchaseOrderRestBean = function(entity) {
        var a = entity.getAddress();
        var d = StringUtil.formatDate(entity.getOrderDate());
        if(a) {
            var ad = ": "+a.getFirstName()+" "+a.getLastName();
            return entity.getOrderNumber()+ad+" "+d;
        } else {
            return entity.getOrderNumber()+": "+d;
        }
    };
    labelFn.DynamicPropertyRestBean = function(entity) {
        return entity.getName();
    };
    labelFn.SalutationRestBean = function(entity) {
        return entity.getInternalName();
    };
    labelFn.UserAttributeValueRestBean = function(entity) {
        var att = entity.getAttribute();
        if(att) {
            return labelFn(att) + ": " + entity.getStringValue();
        } else {
            return entity.getStringValue();
        }
    };
    labelFn.PersonTitleRestBean = function(entity) {
        return entity.getInternalName();
    };
    labelFn.PersonRestBean = function(entity) {
        var address = entity.getAddress();
        if(address) {
            return address.getFirstName()+" "+address.getLastName();
        } else {
            return entity.getExternalUserId();
        }
    };
    labelFn.CountryRestBean = function(entity) {
        return entity.getInternalName();
    };
    labelFn.ProductFamilyRestBean = function(entity) {
        return entity.getName();
    };
    labelFn.ProductAttributeRestBean = function(entity) {
        if (entity.getLabel()) {
            return entity.getLabel();
        }
        return entity.getName();
    };
    labelFn.ShipmentItemRestBean = function(entity) {
        return entity.get("articleId")+": "+entity.get("articleName");
    };
    labelFn.ProductFamilyAttributeRestBean = function(entity) {
        var att = labelFn(entity.getAttribute());
        var fam = labelFn(entity.getFamily());
        if(att && fam) {
            return fam+": "+att;
        } else {
            if(att) {
                return att;
            }
        }
        return undefined;
    };
    labelFn.CurrencyRestBean = function(entity) {
        if (entity.getSymbol()) {
            return entity.getSymbol();
        }
        return entity.getCode();
    };
    labelFn.ProductAttributeValueRestBean = function(entity) {
        if (entity.getLabel()) {
            return entity.getLabel();
        }
        return entity.getStringValue();
    };
    labelFn.ProductAttributeRestBean = function(entity) {
        if (entity.getLabel()) {
            return entity.getLabel();
        }
        return entity.getName();
    };
    labelFn.WishListPrivacyRestBean = function(entity) {
        if (entity.getLabel()) {
            return entity.getLabel();
        }
        return entity.getName();

    };
    labelFn.LanguageRestBean = function(entity) {
        return entity.getName();

    };
    labelFn.TranslationRestBean = function(entity) {
        var sv = entity.getStringValue();
        if(!sv) {
            sv = "";
        }
        var lang = new Expression({root: entity, path: "language.name"}).resolve();
        if(!lang) {
            lang = "";
        }
        if(!sv && !lang) {
            return undefined;
        }
        return lang+" : "+sv;
    };
    labelFn.PathConfigurationRestBean = function(entity) {
        var 
		path = entity.getCmsPath(),
		name = entity.getName(),
		contentLanguage = entity.getContentLanguage();
		if(!contentLanguage) {
			contentLanguage = "";
		} else {
			contentLanguage = " ("+contentLanguage+")";
		}
		if(!name) {
			name = "";
		}
        return name+contentLanguage+" : "+path;
    };

    return labelFn;
});
