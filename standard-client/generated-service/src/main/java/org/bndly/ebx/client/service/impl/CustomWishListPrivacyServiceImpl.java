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
import org.bndly.ebx.model.WishListPrivacy;
import org.bndly.ebx.model.impl.WishListPrivacyImpl;
import org.bndly.ebx.client.service.api.CustomWishListPrivacyService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.WishListPrivacyService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by ben on 19/06/15.
 */
public class CustomWishListPrivacyServiceImpl implements CustomWishListPrivacyService, ProxyAware<WishListPrivacyService> {

    private WishListPrivacyService thisProxy;

    @Override
    @Cached(levels = {CacheLevel.APPLICATION}, localized = true)
    public WishListPrivacy getDefault() throws ClientException {
        WishListPrivacy privacy = new WishListPrivacyImpl();
        privacy.setName("PRIVATE");
		try {
			return thisProxy.find(privacy);
		} catch (UnknownResourceClientException e) {
			privacy.setAppearsInSearchIndex(false);
			privacy.setUseNickNameAsWishListOwner(true);
			return thisProxy.create(privacy);
		}
    }

    @Override
    public void setThisProxy(WishListPrivacyService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
