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

import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.common.service.model.api.ContentID;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.client.service.api.AvailabilityRegistrationService;
import org.bndly.ebx.client.service.api.CartItemService;
import org.bndly.ebx.model.AvailabilityRegistration;
import org.bndly.ebx.model.CartItem;
import org.bndly.ebx.model.impl.AvailabilityRegistrationImpl;
import org.bndly.ebx.client.service.api.CustomAvailabilityRegistrationService;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.impl.UserImpl;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alexp on 17.06.15.
 */
public class CustomAvailabilityRegistrationServiceImpl implements CustomAvailabilityRegistrationService, ProxyAware<AvailabilityRegistrationService> {
    private UserIDProvider userIDProvider;
    private CartItemService cartItemService;
    private AvailabilityRegistrationService thisProxy;

    @Override
    public AvailabilityRegistration readById(long id) throws ClientException {
        AvailabilityRegistrationImpl ar = new AvailabilityRegistrationImpl();
        ar.setId(id);
		try {
			return thisProxy.find(ar);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public List<AvailabilityRegistration> readByExternalIdentifier(String identifier) throws ClientException {
        AvailabilityRegistration avRestBean = new AvailabilityRegistrationImpl();
        User user = new UserImpl();
        user.setIdentifier(identifier);
        avRestBean.setUser(user);
		return thisProxy.findAllLike(avRestBean, ArrayList.class);
    }

    @Override
    public void deleteById(long availabilityId) throws ClientException {
        thisProxy.delete(readById(availabilityId));
    }

    @Override
    public List<AvailabilityRegistration> findAllOfCurrentUser() throws ClientException {
        List<AvailabilityRegistration> collection = readByExternalIdentifier(userIDProvider.getCurrentUserID());
        return collection == null ? Collections.EMPTY_LIST : collection;
    }

    @Override
    public void addRegistrationToCartOfCurrentUser(long id) throws ClientException {
        AvailabilityRegistration ar = readById(id);
        if(ar != null) {
            String uid = ar.getUser().getIdentifier();
            if(userIDProvider.getCurrentUserID().equals(uid)) {
                String contentIdRaw = ar.getExternalObject().getIdentifier();
                CartItem ci = cartItemService.addCartItemForCurrentUser(new ContentID(contentIdRaw), ar.getSku());
                if(ci != null) {
                    deleteById(id);
                }
            }
        }
    }

    public void setUserIDProvider(UserIDProvider userIDProvider) {
        this.userIDProvider = userIDProvider;
    }

	@ServiceReference
    public void setCartItemService(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @Override
    public void setThisProxy(AvailabilityRegistrationService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
