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

import org.bndly.ebx.model.Manufacturer;
import org.bndly.rest.client.exception.UnknownResourceClientException;
import org.bndly.ebx.client.service.api.CustomManufacturerService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.ManufacturerService;
import org.bndly.ebx.model.Address;
import org.bndly.ebx.model.impl.AddressImpl;
import org.bndly.ebx.model.impl.ManufacturerImpl;
import org.bndly.rest.client.exception.ClientException;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomManufacturerServiceImpl implements CustomManufacturerService, ProxyAware<ManufacturerService> {

	private ManufacturerService thisProxy;

	@Override
	public Manufacturer readByName(String name) throws ClientException {
		Address addressProto = new AddressImpl();
		addressProto.setName(name);
		Manufacturer manuProto = new ManufacturerImpl();
		manuProto.setAddress(addressProto);
		manuProto.setName(name);
		try {
			return thisProxy.find(manuProto);
		} catch (UnknownResourceClientException e) {
			return null;
		}
	}

	@Override
	public void setThisProxy(ManufacturerService serviceProxy) {
		thisProxy = serviceProxy;
	}
}
