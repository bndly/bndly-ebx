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

import org.bndly.common.service.cache.api.CacheKeyParameter;
import org.bndly.common.service.cache.api.CacheLevel;
import org.bndly.common.service.cache.api.Cached;
import org.bndly.ebx.model.ExternalObject;
import org.bndly.ebx.model.impl.ExternalObjectImpl;
import org.bndly.ebx.client.service.api.CustomExternalObjectService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.ExternalObjectService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomExternalObjectServiceImpl implements CustomExternalObjectService, ProxyAware<ExternalObjectService> {

    private ExternalObjectService thisProxy;

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObject assertExternalObjectExists(@CacheKeyParameter("externalIdentifier") String externalIdentifier) throws ClientException {
        ExternalObject eo = readByExternalIdentifier(externalIdentifier);
        if(eo == null) {
            eo = new ExternalObjectImpl();
            eo.setIdentifier(externalIdentifier);
            eo = thisProxy.create(eo);
        }
        return eo;
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObject readById(@CacheKeyParameter("id") long id) throws ClientException {
        ExternalObjectImpl o = new ExternalObjectImpl();
        o.setId(id);
		try {
			return thisProxy.find(o);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObject readByExternalIdentifier(@CacheKeyParameter("externalIdentifier") String identifier) throws ClientException {
        ExternalObject o = new ExternalObjectImpl();
        o.setIdentifier(identifier);
		try {
			return thisProxy.find(o);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public void setThisProxy(ExternalObjectService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
