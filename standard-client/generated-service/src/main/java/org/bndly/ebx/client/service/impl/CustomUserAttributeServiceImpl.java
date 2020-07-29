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

import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.ebx.model.UserAttribute;
import org.bndly.ebx.model.impl.UserAttributeImpl;
import org.bndly.ebx.client.service.api.CustomUserAttributeService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.UserAttributeService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomUserAttributeServiceImpl implements ProxyAware<UserAttributeService>, CustomUserAttributeService {

    UserAttributeService thisProxy;

    @Override
    public void setThisProxy(UserAttributeService serviceProxy) {
        thisProxy = serviceProxy;
    }

    @Override
    public UserAttribute readOrCreateUserAttribute(String name) throws ClientException, ReferenceBuildingException {
        UserAttribute userAttribute = new UserAttributeImpl();
        userAttribute.setName(name);
        return thisProxy.assertExists(userAttribute);
    }

    @Override
    public UserAttribute readByName(String name) throws ClientException {
        UserAttribute ua = new UserAttributeImpl();
        ua.setName(name);
		try {
			return thisProxy.find(ua);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public UserAttribute readById(long id) throws ClientException {
        UserAttributeImpl ua = new UserAttributeImpl();
        ua.setId(id);
		try {
			return thisProxy.find(ua);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }
}
