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
import org.bndly.ebx.model.ExternalObjectListType;
import org.bndly.ebx.model.impl.ExternalObjectListTypeImpl;
import org.bndly.ebx.client.service.api.CustomExternalObjectListTypeService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.ExternalObjectListTypeService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomExternalObjectListTypeServiceImpl
        implements CustomExternalObjectListTypeService, ProxyAware<ExternalObjectListTypeService> {

    private ExternalObjectListTypeService thisProxy;

    private ExternalObjectListType buildPrototype(String listTypeName) {
        ExternalObjectListType proto = new ExternalObjectListTypeImpl();
        proto.setName(listTypeName);
        return proto;
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObjectListType readById(@CacheKeyParameter("id") long id) throws ClientException {
        ExternalObjectListTypeImpl t = new ExternalObjectListTypeImpl();
        t.setId(id);
		try {
			return thisProxy.find(t);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObjectListType readByName(@CacheKeyParameter("name") String name) throws ClientException {
        ExternalObjectListType t = new ExternalObjectListTypeImpl();
        t.setName(name);
		try {
			return thisProxy.find(t);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObjectListType assertExists(@CacheKeyParameter("name") String name) throws ClientException {
        ExternalObjectListType proto = new ExternalObjectListTypeImpl();
        proto.setName(name);
		try {
			return thisProxy.find(proto);
		} catch (UnknownResourceClientException e) {
			return thisProxy.create(proto);
		}
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObjectListType findOrCreate(@CacheKeyParameter("name") String listTypeName) throws ClientException {
        ExternalObjectListType type = buildPrototype(listTypeName);
		try {
			return thisProxy.find(type);
		} catch (UnknownResourceClientException e) {
			type = buildPrototype(listTypeName);
			return thisProxy.create(type);
		}
    }

    @Override
    public void setThisProxy(ExternalObjectListTypeService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
