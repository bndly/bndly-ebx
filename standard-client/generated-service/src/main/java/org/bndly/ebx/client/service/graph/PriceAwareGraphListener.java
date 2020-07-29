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

import org.bndly.common.service.model.api.ReferableResource;
import org.bndly.ebx.client.service.api.PriceRequestService;
import org.bndly.ebx.model.CartItem;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.UserPrice;
import org.bndly.ebx.model.WishListItem;
import org.bndly.ebx.client.service.graph.PriceAwareGraphListener.PriceContext;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PriceAwareGraphListener extends NoOpGraphListener<PriceContext> {

    private PriceRequestService priceService;

    
    public static class PriceContext {
        private PriceRequestService priceService;
        Map<String, PriceRequest> priceRequests = new HashMap<>();

        public PriceRequest getPriceForSKU(String sku) {
            if(priceRequests.containsKey(sku)) {
                return priceRequests.get(sku);
            } else {
//				TODO: All cuurent service interface classes in module service-api should be defined as Custom(Foo)Service
//                PriceRequest price = priceService.getPriceForCurrentUserForSKU(sku);
//                priceRequests.put(sku, price);
                return null;
            }
        }
        
        private void setPriceService(PriceRequestService priceService) {
            this.priceService = priceService;
        }
        
    }

    @Override
    public void onStart(Object bean, PriceContext context) {
        context.setPriceService(priceService);
        assertPricesAreSet(bean, context);
    }
    
    
    private void assertPricesAreSet(Object value, PriceContext context) {
		boolean isReference = false;
		if(ReferableResource.class.isInstance(value)) {
			isReference = ReferableResource.class.cast(value).isResourceReference();
		}
		if(isReference) {
			return;
		}
		
		String sku;
		BigDecimal priceNow;
		BigDecimal priceOnAddition;
		BigDecimal priceGrossNow;
		BigDecimal priceGrossOnAddition;
		if(CartItem.class.isInstance(value)) {
			sku = ((CartItem)value).getSku();
			priceNow = ((CartItem)value).getPriceNow();
			priceOnAddition = ((CartItem)value).getPriceOnAddition();
			priceGrossNow = ((CartItem)value).getPriceGrossNow();
			priceGrossOnAddition = ((CartItem)value).getPriceGrossOnAddition();
		} else if(WishListItem.class.isInstance(value)) {
			sku = ((WishListItem)value).getSku();
			priceNow = ((WishListItem)value).getPriceOnAddition();
			priceOnAddition = ((WishListItem)value).getPriceOnAddition();
			priceGrossNow = ((WishListItem)value).getPriceGrossOnAddition();
			priceGrossOnAddition = ((WishListItem)value).getPriceGrossOnAddition();
		} else {
			return;
		}
		
		PriceRequest p = null;
		if(priceNow == null || priceOnAddition == null || priceGrossNow == null || priceGrossOnAddition == null) {
			if(sku != null) {
				p = assertPriceIsRequested(sku, p, context);
			}
		}
		if(p != null) {
			UserPrice up = p.getPrice();
			if(up != null) {
				priceNow = up.getDiscountedNetValue();
				priceGrossNow = up.getDiscountedGrossValue();
				priceOnAddition = up.getDiscountedNetValue();
				priceGrossOnAddition = up.getDiscountedGrossValue();
				if (CartItem.class.isInstance(value)) {
					CartItem v = (CartItem) value;
					if(v.getPriceNow() == null) {
						v.setPriceNow(priceNow);
					}
					if(v.getPriceOnAddition()== null) {
						v.setPriceOnAddition(priceOnAddition);
					}
					if(v.getPriceGrossNow()== null) {
						v.setPriceGrossNow(priceGrossNow);
					}
					if(v.getPriceGrossOnAddition()== null) {
						v.setPriceGrossOnAddition(priceGrossOnAddition);
					}
				} else if (WishListItem.class.isInstance(value)) {
					WishListItem v = (WishListItem) value;
					if(v.getPriceOnAddition()== null) {
						v.setPriceOnAddition(priceOnAddition);
					}
					if(v.getPriceGrossOnAddition()== null) {
						v.setPriceGrossOnAddition(priceGrossOnAddition);
					}
				}
			}
		}
    }

    @Override
    public void onVisitReference(Object bean, PriceContext context) {
        assertPricesAreSet(bean, context);
    }
    
    @Override
    public void beforeVisitReference(Object value, Field field, Object bean, PriceContext context) {
        assertPricesAreSet(value, context);
    }

    @Override
    public void beforeVisitReferenceInCollection(Object object, Collection c, Field field, Object bean, PriceContext context) {
        assertPricesAreSet(object, context);
    }
    
    private PriceRequest assertPriceIsRequested(String sku, PriceRequest p, PriceContext context) {
        if(p == null) {
            p = context.getPriceForSKU(sku);
        }
        return p;
    }
    
	@Override
    public Class<PriceContext> getIterationContextType() {
        return PriceContext.class;
    }

    public void setPriceService(PriceRequestService priceService) {
        this.priceService = priceService;
    }
    
}
