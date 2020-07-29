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
import org.bndly.common.service.model.api.ContentID;
import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.ebx.model.ExternalObject;
import org.bndly.ebx.model.ExternalObjectCentricExternalObjectList;
import org.bndly.ebx.model.ExternalObjectListType;
import org.bndly.ebx.model.impl.ExternalObjectCentricExternalObjectListImpl;
import org.bndly.ebx.model.impl.ExternalObjectImpl;
import org.bndly.ebx.model.impl.ExternalObjectListTypeImpl;
import org.bndly.rest.beans.ebx.ExternalObjectCentricExternalObjectListRestBean;
import org.bndly.rest.beans.ebx.misc.ExternalObjectTrackingRestBean;
import org.bndly.ebx.client.service.api.CustomExternalObjectCentricExternalObjectListService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.ExternalObjectCentricExternalObjectListService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomExternalObjectCentricExternalObjectListServiceImpl
        implements CustomExternalObjectCentricExternalObjectListService, ProxyAware<ExternalObjectCentricExternalObjectListService> {

    private ExternalObjectCentricExternalObjectListService thisProxy;

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObjectCentricExternalObjectList readByContentIDAndListTypeName(@CacheKeyParameter("contentId") ContentID contentId, @CacheKeyParameter("listTypeName") String listTypeName) throws ClientException {
        if(contentId == null || listTypeName == null) {
            return null;
        }
        ExternalObjectCentricExternalObjectList proto = buildPrototype(contentId, listTypeName);
		try {
			return thisProxy.find(proto);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public ExternalObjectCentricExternalObjectList assertExistsByContentIDAndListTypeName(@CacheKeyParameter("contentId") ContentID contentId, @CacheKeyParameter("listTypeName") String listTypeName) throws ClientException {
        ExternalObjectCentricExternalObjectList found = readByContentIDAndListTypeName(contentId, listTypeName);
        if(found == null) {
            ExternalObjectCentricExternalObjectList proto = buildPrototype(contentId, listTypeName);
			try {
				proto.setExternalObject(thisProxy.modelAsReferableResource(proto.getExternalObject()).buildReference());
				proto.setType(thisProxy.modelAsReferableResource(proto.getType()).buildReference());
			} catch (ReferenceBuildingException e) {
				throw new IllegalStateException("could not create reference while creating external object centric list", e);
			}
            found = thisProxy.create(proto);
        }
        return found;
    }

    @Override
    public ExternalObjectCentricExternalObjectList trackFast(String rootExternalObjectIdentifier, String listTypeName, String externalObjectIdentifier) throws ClientException {
        ExternalObjectTrackingRestBean payload = new ExternalObjectTrackingRestBean();
        payload.setExternalObjectIdentifier(externalObjectIdentifier);
        payload.setListTypeName(listTypeName);
        payload.setRootExternalObjectIdentifier(rootExternalObjectIdentifier);
        ExternalObjectCentricExternalObjectListRestBean location = (ExternalObjectCentricExternalObjectListRestBean) thisProxy.getPrimaryResourceClient().follow("trackFast").preventRedirect().execute(payload, ExternalObjectCentricExternalObjectListRestBean.class);
        return (ExternalObjectCentricExternalObjectList) thisProxy.toDomainModel(location);
    }

    private ExternalObjectCentricExternalObjectList buildPrototype(ContentID contentId, String listTypeName) {
        ExternalObject o = new ExternalObjectImpl();
        o.setIdentifier(contentId.getValue());
        ExternalObjectListType type = new ExternalObjectListTypeImpl();
        type.setName(listTypeName);
        ExternalObjectCentricExternalObjectList proto = new ExternalObjectCentricExternalObjectListImpl();
        proto.setType(type);
        proto.setExternalObject(o);
        return proto;
    }

    @Override
    public void setThisProxy(ExternalObjectCentricExternalObjectListService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
