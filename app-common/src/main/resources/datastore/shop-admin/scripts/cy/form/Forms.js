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
define([
    "./ProductRestBean", 
    "./VariantRestBean", 
    "./PurchaseOrderRestBean", 
    "./LineItemRestBean", 
    "./CartRestBean", 
    "./CartItemRestBean",
    "./UserAttributeValueRestBean",
    "./UserRestBean",
    "./WishListRestBean",
    "./WishListItemRestBean",
    "./PersonRestBean",
    "./CheckoutRequestRestBean",
    "./Data",
    "./TranslatedObjectRestBean",
    "./TranslationRestBean",
    "./ProductFamilyRestBean",
    "./ProductFamilyAttributeRestBean",
    "./ValueAddedTaxRestBean",
    "./QuantityUnitRestBean",
    "./StockItemRestBean",
    "./ManufacturerRestBean",
    "./CurrencyRestBean",
    "./ShipmentRestBean",
    "./ShipmentItemRestBean",
    "./SalutationRestBean",
    "./PersonTitleRestBean",
    "./CreditCardBrandRestBean",
    "./ShipmentModeRestBean",
    "./PropertySetRestBean",
    "./PropertyRestBean",
    "./RuleSetRestBean",
    "./BusinessProcessDefinition",
    "./BusinessProcessInstance",
    "./Language",
	"./CountryRestBean",
	"./ImporterConfigurationRestBean",
	"./PathConfigurationRestBean"
], function(
        ProductRestBean, 
        VariantRestBean, 
        PurchaseOrderRestBean, 
        LineItemRestBean, 
        CartRestBean, 
        CartItemRestBean,
        UserAttributeValueRestBean,
        UserRestBean,
        WishListRestBean,
        WishListItemRestBean,
        PersonRestBean,
        CheckoutRequestRestBean,
        Data,
        TranslatedObjectRestBean,
        TranslationRestBean,
        ProductFamilyRestBean,
        ProductFamilyAttributeRestBean,
        ValueAddedTaxRestBean,
        QuantityUnitRestBean,
        StockItemRestBean,
        ManufacturerRestBean,
        CurrencyRestBean,
        ShipmentRestBean,
        ShipmentItemRestBean,
        SalutationRestBean,
        PersonTitleRestBean,
        CreditCardBrandRestBean,
        ShipmentModeRestBean,
        PropertySetRestBean,
        PropertyRestBean,
        RuleSetRestBean,
        BusinessProcessDefinition,
        BusinessProcessInstance,
        LanguageRestBean,
		CountryRestBean,
		ImporterConfigurationRestBean,
		PathConfigurationRestBean
) {
	return {
				ProductRestBean: ProductRestBean,
				VariantRestBean: VariantRestBean,
                PurchaseOrderRestBean: PurchaseOrderRestBean,
                LineItemRestBean: LineItemRestBean,
                CartRestBean: CartRestBean,
                CartItemRestBean: CartItemRestBean,
                UserAttributeValueRestBean: UserAttributeValueRestBean,
                UserRestBean: UserRestBean,
                WishListRestBean: WishListRestBean,
                WishListItemRestBean: WishListItemRestBean,
                PersonRestBean: PersonRestBean,
                CheckoutRequestRestBean: CheckoutRequestRestBean,
                Data: Data,
                TranslatedObjectRestBean: TranslatedObjectRestBean,
                TranslationRestBean: TranslationRestBean,
                BusinessProcessDefinition: BusinessProcessDefinition,
                ProductFamilyRestBean: ProductFamilyRestBean,
                ValueAddedTaxRestBean: ValueAddedTaxRestBean,
                QuantityUnitRestBean: QuantityUnitRestBean,
                StockItemRestBean: StockItemRestBean,
                ManufacturerRestBean: ManufacturerRestBean,
                CurrencyRestBean: CurrencyRestBean,
                ShipmentRestBean: ShipmentRestBean,
                ShipmentItemRestBean: ShipmentItemRestBean,
                SalutationRestBean: SalutationRestBean,
                PersonTitleRestBean: PersonTitleRestBean,
                CreditCardBrandRestBean: CreditCardBrandRestBean,
                ShipmentModeRestBean: ShipmentModeRestBean,
                PropertySetRestBean: PropertySetRestBean,
                PropertyRestBean: PropertyRestBean,
                RuleSetRestBean: RuleSetRestBean,
                ProductFamilyAttributeRestBean: ProductFamilyAttributeRestBean,
                BusinessProcessInstance: BusinessProcessInstance,
                LanguageRestBean: LanguageRestBean,
				CountryRestBean: CountryRestBean,
				ImporterConfigurationRestBean: ImporterConfigurationRestBean,
				PathConfigurationRestBean: PathConfigurationRestBean
	};
});
