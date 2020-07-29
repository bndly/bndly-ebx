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

import org.bndly.common.service.cache.api.CacheLevel;
import org.bndly.common.service.cache.api.Cached;
import org.bndly.ebx.model.WishListItemPriority;
import org.bndly.ebx.model.impl.WishListItemPriorityImpl;
import org.bndly.ebx.client.service.api.CustomWishListItemPriorityService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.WishListItemPriorityService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by ben on 18/06/15.
 */
public class CustomWishListItemPriorityServiceImpl implements ProxyAware<WishListItemPriorityService>, CustomWishListItemPriorityService {

    WishListItemPriorityService thisProxy;

    @Override
    public void setThisProxy(WishListItemPriorityService serviceProxy) {
        thisProxy = serviceProxy;
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION}, localized = true)
    public WishListItemPriority getDefault() throws ClientException {
        WishListItemPriority priority = new WishListItemPriorityImpl();
        priority.setName("MAY");
		try {
			return thisProxy.find(priority);
		} catch (UnknownResourceClientException e) {
			return thisProxy.create(priority);
		}
    }


}
