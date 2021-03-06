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
import org.bndly.ebx.model.PropertySet;
import org.bndly.ebx.model.impl.PropertySetImpl;
import org.bndly.ebx.client.service.api.CustomPropertySetService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.PropertySetService;
import org.bndly.ebx.client.service.property.PropertyToObjectMappingUtil;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomPropertySetServiceImpl implements ProxyAware<PropertySetService>, CustomPropertySetService {

    PropertySetService thisProxy;

    @Override
    public void setThisProxy(PropertySetService serviceProxy) {
        thisProxy = serviceProxy;
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public PropertySet findByName(@CacheKeyParameter("name") String name) throws ClientException {
        PropertySet proto = new PropertySetImpl();
        proto.setName(name);
		try {
			return thisProxy.find(proto);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public void applyPropertySetValuesTo(PropertySet propertySet, Object target) {
        PropertyToObjectMappingUtil.transferPropertiesToTargetDomainModel(propertySet, target);
    }
}
