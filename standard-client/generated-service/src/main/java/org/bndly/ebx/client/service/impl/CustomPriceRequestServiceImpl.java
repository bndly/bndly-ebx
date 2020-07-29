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

import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.impl.PriceRequestImpl;
import org.bndly.ebx.client.service.api.CustomPriceRequestService;
import org.bndly.ebx.client.service.api.PriceRequestService;
import org.bndly.ebx.client.service.api.UserService;
import org.bndly.rest.client.exception.ClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomPriceRequestServiceImpl implements ProxyAware<PriceRequestService>, CustomPriceRequestService {

    private UserService userService;
    private PriceRequestService thisProxy;

    @Override
    public void setThisProxy(PriceRequestService serviceProxy) {
        thisProxy = serviceProxy;
    }

    @Override
    public PriceRequest getPriceForCurrentUserForSKU(String sku) throws ClientException {
        return getPriceForCurrentUserForSKUAndQuantity(sku, 1);
    }

    @Override
    public PriceRequest getPriceForCurrentUserForSKUAndQuantity(String sku, long quantity) throws ClientException {
        PriceRequest p = new PriceRequestImpl();
        p.setSku(sku);
        p.setUser(userService.assertCurrentUserExistsAsReference());
        p.setQuantity(quantity);
        return thisProxy.create(p);
    }

	@ServiceReference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
