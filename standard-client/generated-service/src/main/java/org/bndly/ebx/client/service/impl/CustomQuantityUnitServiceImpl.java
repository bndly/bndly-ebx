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

import org.bndly.ebx.model.QuantityUnit;
import org.bndly.ebx.model.impl.QuantityUnitImpl;
import org.bndly.ebx.client.service.api.CustomQuantityUnitService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.QuantityUnitService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomQuantityUnitServiceImpl implements ProxyAware<QuantityUnitService>, CustomQuantityUnitService {

    QuantityUnitService thisProxy;

    @Override
    public void setThisProxy(QuantityUnitService serviceProxy) {
        thisProxy = serviceProxy;
    }

    @Override
    public QuantityUnit readById(long id) throws ClientException {
        QuantityUnitImpl qu = new QuantityUnitImpl();
        qu.setId(id);
		try {
			return thisProxy.find(qu);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public QuantityUnit getDefault() throws ClientException {
        QuantityUnit defaultQuantityUnit = new QuantityUnitImpl();
        defaultQuantityUnit.setQuantity(1L);
		try {
			return thisProxy.find(defaultQuantityUnit);
		} catch (UnknownResourceClientException e) {
			defaultQuantityUnit.setAbbrevation("St.");
			defaultQuantityUnit.setDescription("Default");
			return thisProxy.create(defaultQuantityUnit);
		}
    }
}
