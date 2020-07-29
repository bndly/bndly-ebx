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

import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.client.service.api.AddressAssignmentService;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.model.Address;
import org.bndly.ebx.model.AddressAssignment;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.impl.AddressAssignmentImpl;
import org.bndly.ebx.model.impl.AddressImpl;
import org.bndly.ebx.model.impl.PersonImpl;
import org.bndly.ebx.client.service.api.CustomAddressAssignmentService;
import org.bndly.ebx.client.service.api.PersonService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexp on 17.06.15.
 */
public class CustomAddressAssignmentServiceImpl implements CustomAddressAssignmentService, ProxyAware<AddressAssignmentService> {

	private UserIDProvider userIDProvider;
	private PersonService personService;
	private AddressAssignmentService thisProxy;

	@Override
	public void removeAddressOfCurrentUser(long addressId) throws ClientException {
		AddressAssignment tmp = getCurrentUserAssignmentPrototype();
		AddressImpl a = new AddressImpl();
		a.setId(addressId);
		tmp.setAddress(a);
		try {
			tmp = thisProxy.find(tmp);
			thisProxy.delete(tmp);
		} catch (UnknownResourceClientException e) {
			// if the address does not exist, it is deleted anyway
		}
	}

	@Override
	public Address addAddressToCurrentUser(Address address) throws ClientException {
		AddressAssignment assignment = new AddressAssignmentImpl();
		try {
			assignment.setPerson(thisProxy.modelAsReferableResource(personService.assertCurrentUserExistsAsPerson()).buildReference());
		} catch (ReferenceBuildingException e) {
			return address;
		}
		if (getId(address) == null) {
			assignment.setAddress(address);
			assignment = thisProxy.create(assignment);
		} else {
			try {
				assignment.setAddress(thisProxy.modelAsReferableResource(address).buildReference());
			} catch (ReferenceBuildingException e) {
				return address;
			}
			assignment = thisProxy.find(assignment);
			assignment.setAddress(address);
			assignment = thisProxy.update(assignment);
		}
		return assignment.getAddress();
	}

	@Override
	public List<Address> getAddressesOfCurrentUser() throws ClientException {
		AddressAssignment tmp = getCurrentUserAssignmentPrototype();
		List<Address> addresses = new ArrayList<>();
		Collection<AddressAssignment> l = thisProxy.findAllLike(tmp);
		if (l != null) {
			for (AddressAssignment addressAssignment : l) {
				addresses.add(addressAssignment.getAddress());
			}
		}
		return addresses;
	}

	private AddressAssignment getCurrentUserAssignmentPrototype() {
		AddressAssignment tmp = new AddressAssignmentImpl();
		tmp.setPerson(getCurrentUserPrototype());
		return tmp;
	}

	private Person getCurrentUserPrototype() {
		Person p = new PersonImpl();
		p.setExternalUserId(userIDProvider.getCurrentUserID());
		return p;
	}

	private Long getId(Address model) {
		if (AddressImpl.class.isInstance(model)) {
			return ((AddressImpl) model).getId();
		}
		return null;
	}

	public void setUserIDProvider(UserIDProvider userIDProvider) {
		this.userIDProvider = userIDProvider;
	}

	@ServiceReference
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	@Override
	public void setThisProxy(AddressAssignmentService serviceProxy) {
		thisProxy = serviceProxy;
	}
}
