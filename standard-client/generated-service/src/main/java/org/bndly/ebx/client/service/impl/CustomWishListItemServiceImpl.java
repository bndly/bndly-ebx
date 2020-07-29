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

import org.bndly.ebx.model.WishListItem;
import org.bndly.ebx.model.impl.WishListItemImpl;
import org.bndly.ebx.client.service.api.CustomWishListItemService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.WishListItemService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by ben on 18/06/15.
 */
public class CustomWishListItemServiceImpl implements ProxyAware<WishListItemService>, CustomWishListItemService {

    private WishListItemService thisProxy;

    @Override
    public void setThisProxy(WishListItemService serviceProxy) {
        this.thisProxy = serviceProxy;
    }

    @Override
    public WishListItem findById(Long id) throws ClientException {
        WishListItemImpl item = new WishListItemImpl();
        item.setId(id);
		try {
			return thisProxy.find(item);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

}
