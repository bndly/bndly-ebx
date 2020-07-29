package org.bndly.ebx.client.service.impl;

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

import org.bndly.ebx.client.service.api.ContentService;
import org.bndly.common.graph.BeanGraphBuilder;
import org.bndly.common.reflection.BeanPropertyAccessor;
import org.bndly.common.reflection.GetterBeanPropertyAccessor;
import org.bndly.common.service.model.api.CollectionIndexer;
import org.bndly.common.service.model.api.ContentID;
import org.bndly.common.service.model.api.IndexerFunction;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.client.service.api.CartItemService;
import org.bndly.ebx.client.service.api.CurrencyService;
import org.bndly.ebx.model.Cart;
import org.bndly.ebx.model.CartItem;
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.impl.CartImpl;
import org.bndly.ebx.model.impl.CartItemImpl;
import org.bndly.rest.beans.ebx.CartRestBean;
import org.bndly.rest.client.exception.UnknownResourceClientException;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.client.service.api.CartService;
import org.bndly.ebx.client.service.api.CustomCartService;
import org.bndly.ebx.client.service.api.UserService;
import org.bndly.rest.client.exception.ClientException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexp on 11.06.15.
 */
public class CustomCartServiceImpl implements CustomCartService, ProxyAware<CartService> {

    private UserIDProvider userIDProvider;
    private UserService userService;
    private CurrencyService currencyService;
    private CartItemService cartItemService;
    private ContentService contentService;

    private CartService thisProxy;

    @Override
    public Cart readCartByUserIdentifier(String userIdentifier) throws ClientException {
        Cart c = new CartImpl();
        c.setUserIdentifier(userIdentifier);
		try {
			Cart cart = thisProxy.find(c);
			injectEuroCurrencyIntoCartItemsOfCart(cart);
			return cart;
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public Cart refreshCart(Cart cartToBeRefreshed) throws ClientException {
        try {
            //if someone call refresh cart and no user existes with the userIdentifier than create it before refresh, because
            //the process engine needs user for recalculating prices
            userService.assertUserExistsAsReference(cartToBeRefreshed.getUserIdentifier());
            thisProxy.traverse(cartToBeRefreshed, thisProxy.getUpdateResourceServiceGraphListener());
            cartToBeRefreshed.setLastInteraction(new Date());
            CartRestBean cartRestBean = (CartRestBean) thisProxy.toRestModel(cartToBeRefreshed);
            cartRestBean = BeanGraphBuilder.breakCycles(cartRestBean);
            thisProxy.createClient(cartRestBean).follow("refresh").execute(cartRestBean);
            CartRestBean cartResponseRestBean = thisProxy.createClient(cartRestBean).read().execute();
            cartResponseRestBean = BeanGraphBuilder.rebuildCycles(cartResponseRestBean);
            Cart responsedCart = (Cart) thisProxy.toDomainModel(cartResponseRestBean);
            return responsedCart;
        } catch (UnknownResourceClientException e) {
            return null;
        } catch (Exception e) {
            return null;
        }

    }

    private void injectEuroCurrencyIntoCartItemsOfCart(Cart toBeRefreshedCart) throws ClientException {
        List<CartItem> items = toBeRefreshedCart.getCartItems();
        if (items != null) {
            Currency currency = currencyService.readOrCreateCurrencyByCode("EUR");
            for (CartItem cartItem : items) {
                if (cartItem.getCurrency() == null) {
                    cartItem.setCurrency(currency);
                }
            }
        }
    }

    @Override
    public boolean deleteCartByUserIdentifier(String userIdentifier) throws ClientException {
        Cart cart = readCartByUserIdentifier(userIdentifier);
        if (cart != null) {
            return thisProxy.delete(cart);
        } else {
            return false;
        }
    }

    @Override
    public Cart readCartOfCurrentUser() throws ClientException {
        return readCartByUserIdentifier(userIDProvider.getCurrentUserID());
    }

    @Override
    public Cart removeCartItemOfCart(Long cartItemIdToRemove, Cart cart) throws ClientException {
		cartItemService.deleteById(cartItemIdToRemove);
		try {
			return thisProxy.read(cart);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public Cart removeCartItemOfCurrentUser(long id) throws ClientException {
        Cart cart = readCartOfCurrentUser();
        CartItem itemToRemove = new CollectionIndexer<Long, CartItem>().index(cart.getCartItems(), new IndexerFunction<Long, CartItem>() {
            @Override
            public Long index(CartItem o) {
                if(CartItemImpl.class.isInstance(o)) {
                    return ((CartItemImpl)o).getId();
                }
                return null;
            }
        }).get(id);
        cart.getCartItems().remove(itemToRemove);
        return refreshCart(cart);
    }

    @Override
    public Cart assertCartOfCurrentUserExists() throws ClientException {
        String uid = userIDProvider.getCurrentUserID();
        return assertCartOfUserExists(uid);
    }

    @Override
    public Cart assertCartOfUserExists(String userIdentifier) throws ClientException {
        Cart c = readCartByUserIdentifier(userIdentifier);
        if(c == null) {
            c = new CartImpl();
            c.setUserIdentifier(userIdentifier);
            c.setLastInteraction(new Date());
            c = thisProxy.create(c);
        }
        return c;
    }

    @Override
    public Cart cleanUpNotPurchasableCartItems(Cart c) throws ClientException {
        List<CartItem> items = c.getCartItems();
        if(items != null) {
            Map<String, Object> contentObjects = new HashMap<>();
            BeanPropertyAccessor accessor = new GetterBeanPropertyAccessor();
            List<CartItem> defCopy = new ArrayList<>(items);
            List<CartItem> itemsToRemove = new ArrayList<>();
            for (CartItem cartItem : defCopy) {
                String cid = cartItem.getContentId();
                Object content;
                if(!contentObjects.containsKey(cid)) {
                    content = contentService.getContentBeanById(new ContentID(cid));
                    contentObjects.put(cid, content);
                } else {
                    content = contentObjects.get(cid);
                }
                if(content == null) {
                    // content does not exist
                } else {
                    Object extRef = accessor.get("externalReference", content);
                    if(extRef == null) {
                        itemsToRemove.add(cartItem);
                    }
                }
            }

            if(!itemsToRemove.isEmpty()) {
                items.removeAll(itemsToRemove);
                c = thisProxy.update(c);
            }

        }
        return c;
    }

    @Override
    public Cart cleanUpNotPurchasableCartItemsByUserIdentifier(String userIdentifier) throws ClientException {
        return cleanUpNotPurchasableCartItems(assertCartOfUserExists(userIdentifier));
    }

    @Override
    public Cart cleanUpNotPurchasableCartItemsOfCurrentUser() throws ClientException {
        return cleanUpNotPurchasableCartItemsByUserIdentifier(userIDProvider.getCurrentUserID());
    }

    @ServiceReference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @ServiceReference
    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public void setUserIDProvider(UserIDProvider userIDProvider) {
        this.userIDProvider = userIDProvider;
    }

    @ServiceReference
    public void setCartItemService(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public void setThisProxy(CartService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
