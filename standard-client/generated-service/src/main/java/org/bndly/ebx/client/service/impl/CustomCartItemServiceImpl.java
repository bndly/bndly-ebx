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

import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.client.service.api.ContentService;
import org.bndly.common.reflection.BeanPropertyAccessor;
import org.bndly.common.reflection.GetterBeanPropertyAccessor;
import org.bndly.common.service.model.api.CollectionIndexer;
import org.bndly.common.service.model.api.ContentID;
import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.CartItemService;
import org.bndly.ebx.model.Cart;
import org.bndly.ebx.model.CartItem;
import org.bndly.ebx.model.impl.CartItemImpl;
import org.bndly.ebx.client.service.api.CartService;
import org.bndly.ebx.client.service.api.CustomCartItemService;
import org.bndly.ebx.client.service.api.CustomCartItemService.CartItemKey;
import org.bndly.ebx.client.service.api.CustomCartItemService.CartItemKeyIndexer;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.util.Map;

/**
 * Created by alexp on 17.06.15.
 */
public class CustomCartItemServiceImpl implements CustomCartItemService, ProxyAware<CartItemService> {

	private ContentService contentService;
	private final BeanPropertyAccessor accessor = new GetterBeanPropertyAccessor();
	private CartService cartService;

	private CartItemService thisProxy;

	@Override
	public CartItem updateCartItemQuantity(long id, long quantity) throws ClientException {
		CartItem ci = readById(id);
		if (ci != null) {
			ci.setQuantity(quantity);
			thisProxy.update(ci);
		}
		return ci;
	}

	@Override
	public boolean deleteById(long id) throws ClientException {
		CartItem ci = readById(id);
		if (ci != null) {
			return thisProxy.delete(ci);
		}
		return false;
	}

	@Override
	public CartItem readById(long id) throws ClientException {
		CartItemImpl proto = new CartItemImpl();
		proto.setId(id);
		try {
			return thisProxy.find(proto);
		} catch (UnknownResourceClientException e) {
			return null;
		}
	}

	@Override
	public CartItem addCartItemForCurrentUser(ContentID contentID, String sku) throws ClientException {
		return addCartItemForCurrentUser(contentID, sku, 1);
	}

	@Override
	public CartItem addCartItemForCurrentUser(ContentID contentID, String sku, long quantity) throws ClientException {
		return addCartItemForCurrentUser(contentID, sku, quantity, null);
	}

	@Override
	public CartItem addCartItemForCurrentUser(ContentID contentID, String sku, long quantity, String wishListSecurityToken) throws ClientException {
		Object contentBean = contentService.getContentBeanById(contentID);
		if (contentBean != null) {
			Cart c = cartService.assertCartOfCurrentUserExists();
			if (sku == null) {
				sku = (String) accessor.get("productNumber", contentBean);
			}
			if (sku != null) {
				CartItemKey key = new CartItemKey(sku, wishListSecurityToken);
				Map<CartItemKey, CartItem> index = new CollectionIndexer<CartItemKey, CartItem>().index(c.getCartItems(), new CartItemKeyIndexer());
				if (index.containsKey(key)) {
					// update
					CartItem ci = index.get(key);
					long q = ci.getQuantity() + quantity;
					if (q > 99) {
						q = 99;
					}
					if (q < 0) {
						ci.setQuantity(0L);
						deleteById(getId(ci));
						return ci;
					} else {
						ci.setQuantity(q);
						return thisProxy.update(ci);
					}
				} else {
					// create
					CartItem ci = new CartItemImpl();
					ci.setContentId(contentID == null ? null : contentID.getValue());
					ci.setSku(sku);
					ci.setWishListSecurityToken(wishListSecurityToken);
					ci.setQuantity(quantity);
					try {
						ci.setCart(thisProxy.modelAsReferableResource(c).buildReference());
					} catch (ReferenceBuildingException e) {
						return null;
					}
					// remaining information is added in the service layer automatically by a content merge bean iterator listener
					ci = thisProxy.create(ci);
					return ci;
				}
			} else {
				// product number can not be found in content object
				return null;
			}
		} else {
			// content object is not found
			return null;
		}
	}

	private Long getId(CartItem model) {
		if (CartItemImpl.class.isInstance(model)) {
			return ((CartItemImpl) model).getId();
		}
		return null;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@ServiceReference
	public void setCartService(CartService cartService) {
		this.cartService = cartService;
	}

	@Override
	public void setThisProxy(CartItemService serviceProxy) {
		thisProxy = serviceProxy;
	}
}
