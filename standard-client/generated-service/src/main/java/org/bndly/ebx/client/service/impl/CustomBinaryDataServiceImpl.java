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

import org.bndly.common.service.model.api.Linkable;
import org.bndly.ebx.model.BinaryData;
import org.bndly.ebx.model.impl.BinaryDataImpl;
import org.bndly.rest.beans.ebx.BinaryDataRestBean;
import org.bndly.ebx.client.service.api.CustomBinaryDataService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.BinaryDataService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by alexp on 17.06.15.
 */
public class CustomBinaryDataServiceImpl implements CustomBinaryDataService, ProxyAware<BinaryDataService> {

    BinaryDataService thisProxy;

    @Override
    public BinaryData createAndUpload(BinaryData data) throws ClientException {
        BinaryData createdData = thisProxy.create(data);
        if (createdData != null) {
            String rel = "upload";
            Linkable linkableData = thisProxy.modelAsLinkable(data);
            Linkable linkableCreatedData = thisProxy.modelAsLinkable(createdData);
            String href = linkableCreatedData.follow(rel);
            String method = linkableCreatedData.followForMethod(rel);
            linkableData.addLink(rel, href, method);
            upload(data);
        }
        return createdData;
    }

    @Override
    public void upload(BinaryData data) throws ClientException {
        if (data.getBytes() != null) {
            BinaryDataRestBean bean = (BinaryDataRestBean) thisProxy.toRestModel(data);
            thisProxy.createClient(bean).follow("upload").execute(data.getBytes());
        }
    }

    @Override
    public BinaryData readByName(String name) throws ClientException {
        BinaryData d = new BinaryDataImpl();
        d.setName(name);

		try {
			BinaryData dataFromService = thisProxy.find(d);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			BinaryDataRestBean bean = (BinaryDataRestBean) thisProxy.toRestModel(dataFromService);
			thisProxy.createClient(bean).follow("download").execute(os);
			dataFromService.setBytes(new ByteArrayInputStream(os.toByteArray()));
			try {
				os.close();
			} catch (IOException ex) {
				// ignore
			}
			return dataFromService;
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public void setThisProxy(BinaryDataService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
