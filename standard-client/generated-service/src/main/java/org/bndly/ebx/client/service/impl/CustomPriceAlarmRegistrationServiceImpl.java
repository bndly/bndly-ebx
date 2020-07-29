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
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.model.PriceAlarmRegistration;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.impl.PriceAlarmRegistrationImpl;
import org.bndly.ebx.model.impl.UserImpl;
import org.bndly.ebx.client.service.api.CustomPriceAlarmRegistrationService;
import org.bndly.ebx.client.service.api.PriceAlarmRegistrationService;
import org.bndly.ebx.client.service.api.UserService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomPriceAlarmRegistrationServiceImpl implements ProxyAware<PriceAlarmRegistrationService>, CustomPriceAlarmRegistrationService {

    private UserIDProvider userIDProvider;
    private UserService userService;
    private PriceAlarmRegistrationService thisProxy;

    @Override
    public void setThisProxy(PriceAlarmRegistrationService serviceProxy) {
        thisProxy = serviceProxy;
    }

    @Override
    public PriceAlarmRegistration readById(long priceAlertId) throws ClientException {
        PriceAlarmRegistrationImpl proto = new PriceAlarmRegistrationImpl();
        proto.setId(priceAlertId);
		try {
			return thisProxy.find(proto);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public boolean delete(long priceAlertId) throws ClientException {
        PriceAlarmRegistration pa = readById(priceAlertId);
        if(pa != null) {
            return thisProxy.delete(pa);
        }
        return false;
    }

    @Override
    public List<PriceAlarmRegistration> findAllByCurrentUser() throws ClientException {
        PriceAlarmRegistration proto = new PriceAlarmRegistrationImpl();
        User u = new UserImpl();
        u.setIdentifier(userIDProvider.getCurrentUserID());
        proto.setUser(u);
        return thisProxy.findAllLike(proto, ArrayList.class);
    }

    @Override
    public List<PriceAlarmRegistration> findAllByExternalIdentifier(String identifier) throws ClientException {
        PriceAlarmRegistration proto = new PriceAlarmRegistrationImpl();
        User u = userService.readByExternalIdentifier(identifier);
        proto.setUser(u);
        return thisProxy.findAllLike(proto, ArrayList.class);
    }

    public void setUserIDProvider(UserIDProvider userIDProvider) {
        this.userIDProvider = userIDProvider;
    }

	@ServiceReference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
