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

import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.impl.PersonImpl;
import org.bndly.ebx.model.impl.PurchaseOrderImpl;
import org.bndly.ebx.client.service.api.CustomPurchaseOrderService;
import org.bndly.ebx.client.service.api.PurchaseOrderService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexp on 16.06.15.
 */
public class CustomPurchaseOrderServiceImpl implements ProxyAware<PurchaseOrderService>, CustomPurchaseOrderService {

	private UserIDProvider userIDProvider;
	private PurchaseOrderService thisProxy;

	@Override
	public void setThisProxy(PurchaseOrderService serviceserviceProxy) {
		thisProxy = serviceserviceProxy;
	}

	@Override
	public PurchaseOrder readById(long id) throws ClientException {
		PurchaseOrderImpl o = new PurchaseOrderImpl();
		o.setId(id);
		try {
			return thisProxy.find(o);
		} catch (UnknownResourceClientException e) {
			return null;
		}
	}

	@Override
	public PurchaseOrder readByNumber(String orderNumber) throws ClientException {
		PurchaseOrder o = new PurchaseOrderImpl();
		o.setOrderNumber(orderNumber);
		try {
			return thisProxy.find(o);
		} catch (UnknownResourceClientException e) {
			return null;
		}
	}

	@Override
	public List<PurchaseOrder> findAllOfPerson(Person person) throws ClientException {
		PersonImpl p = new PersonImpl();
		if (PersonImpl.class.isInstance(person)) {
			p.setId(((PersonImpl) person).getId());
		}
		p.setExternalUserId(person.getExternalUserId());
		PurchaseOrder o = new PurchaseOrderImpl();
		o.setOrderer(p);
		return thisProxy.findAllLikeEagerly(o, ArrayList.class);
	}

	@Override
	public void printToOutStream(PurchaseOrder o, OutputStream outputStream) throws ClientException {
		thisProxy.print(o, outputStream);
	}

	@Override
	public List<PurchaseOrder> findAllOfCurrentUser() throws ClientException {
		String uid = userIDProvider.getCurrentUserID();
		PurchaseOrder o = new PurchaseOrderImpl();
		Person p = new PersonImpl();
		p.setExternalUserId(uid);
		o.setOrderer(p);
		return thisProxy.findAllLikeEagerly(o, ArrayList.class);
	}

	public void setUserIDProvider(UserIDProvider userIDProvider) {
		this.userIDProvider = userIDProvider;
	}
}
