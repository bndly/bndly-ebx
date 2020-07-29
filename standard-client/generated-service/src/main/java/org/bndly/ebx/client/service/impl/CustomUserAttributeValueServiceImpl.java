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

import org.bndly.ebx.model.UserAttributeValue;
import org.bndly.ebx.model.impl.UserAttributeValueImpl;
import org.bndly.ebx.client.service.api.CustomUserAttributeValueService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.UserAttributeValueService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomUserAttributeValueServiceImpl implements ProxyAware<UserAttributeValueService>, CustomUserAttributeValueService {

    UserAttributeValueService thisProxy;

    @Override
    public void setThisProxy(UserAttributeValueService serviceProxy) {
        thisProxy = serviceProxy;
    }

    @Override
    public UserAttributeValue readById(long id) throws ClientException {
        UserAttributeValueImpl uav = new UserAttributeValueImpl();
        uav.setId(id);
		try {
			return thisProxy.find(uav);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }
}
