package org.bndly.ebx.client.service.graph;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service
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

import org.bndly.ebx.client.service.api.CurrencyService;
import org.bndly.ebx.model.CartItem;
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.LineItem;
import org.bndly.ebx.model.PriceAlarmRegistration;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.ShipmentOffer;
import org.bndly.ebx.model.ShipmentOfferRequest;
import org.bndly.ebx.model.WishListItem;
import org.bndly.ebx.client.service.graph.DefaultCurrencyGraphListener.DefaultCurrencyContext;

public class DefaultCurrencyGraphListener extends NoOpGraphListener<DefaultCurrencyContext>{

    private CurrencyService currencyService;

	@Override
    public Class<DefaultCurrencyContext> getIterationContextType() {
        return DefaultCurrencyContext.class;
    }

    @Override
    public void onStart(Object bean, DefaultCurrencyContext context) {
        context.setCurrencyService(currencyService);
    }
    
    @Override
    public void onVisitReference(Object bean, DefaultCurrencyContext context) {
		if(ShipmentOfferRequest.class.isInstance(bean)) {
			if(((ShipmentOfferRequest)bean).getCurrency() == null) {
				((ShipmentOfferRequest)bean).setCurrency(context.getDefaultCurrency());
			}
		} else if(WishListItem.class.isInstance(bean)) {
			if(((WishListItem)bean).getCurrency() == null) {
				((WishListItem)bean).setCurrency(context.getDefaultCurrency());
			}
		} else if(CartItem.class.isInstance(bean)) {
			if(((CartItem)bean).getCurrency() == null) {
				((CartItem)bean).setCurrency(context.getDefaultCurrency());
			}
		} else if(LineItem.class.isInstance(bean)) {
			if(((LineItem)bean).getCurrency() == null) {
				((LineItem)bean).setCurrency(context.getDefaultCurrency());
			}
		} else if(ShipmentOffer.class.isInstance(bean)) {
			if(((ShipmentOffer)bean).getCurrency() == null) {
				((ShipmentOffer)bean).setCurrency(context.getDefaultCurrency());
			}
		} else if(PriceRequest.class.isInstance(bean)) {
			if(((PriceRequest)bean).getCurrency() == null) {
				((PriceRequest)bean).setCurrency(context.getDefaultCurrency());
			}
		} else if(PriceAlarmRegistration.class.isInstance(bean)) {
			if(((PriceAlarmRegistration)bean).getCurrency() == null) {
				((PriceAlarmRegistration)bean).setCurrency(context.getDefaultCurrency());
			}
		}
    }
    
    public static class DefaultCurrencyContext {
        private Currency defaultCurrency;
        private CurrencyService currencyService;

        public void setCurrencyService(CurrencyService currencyService) {
            this.currencyService = currencyService;
        }

        public Currency getDefaultCurrency() {
            if(defaultCurrency == null) {
//				TODO: All cuurent service interface classes in module service-api should be defined as Custom(Foo)Service
//                defaultCurrency = currencyService.readOrCreateCurrencyByCode("EUR");
            }
            return defaultCurrency;
        }
    }

    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }
    
}
