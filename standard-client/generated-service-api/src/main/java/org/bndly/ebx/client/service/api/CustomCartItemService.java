package org.bndly.ebx.client.service.api;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service-api
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

import org.bndly.common.service.model.api.ContentID;
import org.bndly.common.service.model.api.IDFieldIndexerFunction;
import org.bndly.common.service.model.api.IndexerFunction;
import org.bndly.ebx.model.CartItem;
import org.bndly.rest.client.exception.ClientException;

import java.io.Serializable;

/**
 * Created by alexp on 17.06.15.
 */

public interface CustomCartItemService {

    static class CartItemKey implements Serializable {

        private static final long serialVersionUID = -8033421361039202377L;

        private String productNumber;
        private String wishListId;

        public CartItemKey(String productNumber) {
            this.productNumber = productNumber;
        }

        public CartItemKey(String productNumber, String wishListId) {
            this.productNumber = productNumber;
            if (null != wishListId && "".compareTo(wishListId) != 0) {
                this.wishListId = wishListId;
            }
        }

        public String getProductNumber() {
            return productNumber;
        }

        public void setProductNumber(String productNumber) {
            this.productNumber = productNumber;
        }

        public String getWishListId() {
            return wishListId;
        }

        public void setWishListId(String wishListId) {
            this.wishListId = wishListId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CartItemKey that = (CartItemKey) o;

            if (productNumber != null ? !productNumber.equals(that.productNumber) : that.productNumber != null) {
                return false;
            }
            if (wishListId != null ? !wishListId.equals(that.wishListId) : that.wishListId != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = productNumber != null ? productNumber.hashCode() : 0;
            result = 31 * result + (wishListId != null ? wishListId.hashCode() : 0);
            return result;
        }

        public String getIdentifier() {
            return "" + hashCode();
        }
    }

    static class CartItemIdIndexer extends IDFieldIndexerFunction<CartItem> implements IndexerFunction<Long, CartItem> {
    }

    static class CartItemKeyIndexer implements IndexerFunction<CartItemKey, CartItem> {

        @Override
        public CartItemKey index(CartItem o) {
            CartItemKey k = new CartItemKey(o.getSku(), o.getWishListSecurityToken());
            return k;
        }

    }

    CartItem updateCartItemQuantity(long id, long quantity) throws ClientException;

    boolean deleteById(long id) throws ClientException;

    CartItem readById(long id) throws ClientException;

    CartItem addCartItemForCurrentUser(ContentID contentID, String sku) throws ClientException;

    CartItem addCartItemForCurrentUser(ContentID contentID, String sku, long quantity) throws ClientException;

    CartItem addCartItemForCurrentUser(ContentID contentID, String sku, long quantity, String wishListSecurityToken) throws ClientException;

}

