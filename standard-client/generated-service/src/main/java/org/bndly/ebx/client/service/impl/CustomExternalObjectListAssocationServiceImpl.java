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

import org.bndly.common.graph.BeanGraphBuilder;
import org.bndly.ebx.model.ExternalObjectListAssocation;
import org.bndly.ebx.model.impl.ExternalObjectListAssocationImpl;
import org.bndly.rest.beans.ebx.ExternalObjectListAssocationListRestBean;
import org.bndly.rest.beans.ebx.ExternalObjectListAssocationRestBean;
import org.bndly.ebx.client.service.api.CustomExternalObjectListAssocationService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.ExternalObjectListAssocationService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.util.Collection;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomExternalObjectListAssocationServiceImpl
        implements CustomExternalObjectListAssocationService, ProxyAware<ExternalObjectListAssocationService> {

    private ExternalObjectListAssocationService thisProxy;

    @Override
    public ExternalObjectListAssocation readById(long id) throws ClientException {
        ExternalObjectListAssocationImpl a = new ExternalObjectListAssocationImpl();
        a.setId(id);
		try {
			return thisProxy.find(a);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public void batchCreate(Collection<ExternalObjectListAssocation> batch) throws ClientException {
        ExternalObjectListAssocationListRestBean listRestBean = new ExternalObjectListAssocationListRestBean();
        for (ExternalObjectListAssocation assoc : batch) {
            ExternalObjectListAssocationRestBean restBean = (ExternalObjectListAssocationRestBean) thisProxy.toRestModel(assoc);
            restBean = BeanGraphBuilder.breakCycles(restBean);
            listRestBean.add(restBean);
        }
        thisProxy.getPrimaryResourceClient().follow("batchCreate").execute(listRestBean);
    }

    @Override
    public void batchDelete(Collection<ExternalObjectListAssocation> batch) throws ClientException {
        ExternalObjectListAssocationListRestBean listRestBean = new ExternalObjectListAssocationListRestBean();
        for (ExternalObjectListAssocation assoc : batch) {
            ExternalObjectListAssocationRestBean restBean = (ExternalObjectListAssocationRestBean) thisProxy.toRestModel(assoc);
            restBean = BeanGraphBuilder.breakCycles(restBean);
            listRestBean.add(restBean);
        }
        thisProxy.getPrimaryResourceClient().follow("batchDelete").execute(listRestBean);
    }

    @Override
    public void batchUpdate(Collection<ExternalObjectListAssocation> batch) throws ClientException {
        ExternalObjectListAssocationListRestBean listRestBean = new ExternalObjectListAssocationListRestBean();
        for (ExternalObjectListAssocation assoc : batch) {
            ExternalObjectListAssocationRestBean restBean = (ExternalObjectListAssocationRestBean) thisProxy.toRestModel(assoc);
            restBean = BeanGraphBuilder.breakCycles(restBean);
            listRestBean.add(restBean);
        }
        thisProxy.getPrimaryResourceClient().follow("batchUpdate").execute(listRestBean);
    }

    @Override
    public void setThisProxy(ExternalObjectListAssocationService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
