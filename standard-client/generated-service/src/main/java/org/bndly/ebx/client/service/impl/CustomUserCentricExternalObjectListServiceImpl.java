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
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.model.ExternalObjectListAssocation;
import org.bndly.ebx.model.ExternalObjectListType;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.UserCentricExternalObjectList;
import org.bndly.ebx.model.impl.ExternalObjectListAssocationImpl;
import org.bndly.ebx.model.impl.ExternalObjectListTypeImpl;
import org.bndly.ebx.model.impl.UserCentricExternalObjectListImpl;
import org.bndly.ebx.model.impl.UserImpl;
import org.bndly.rest.beans.ebx.misc.UserTrackingRestBean;
import org.bndly.ebx.client.service.api.CustomUserCentricExternalObjectListService;
import org.bndly.ebx.client.service.api.ExternalObjectListAssocationService;
import org.bndly.ebx.client.service.api.ExternalObjectListTypeService;
import org.bndly.ebx.client.service.api.UserCentricExternalObjectListService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomUserCentricExternalObjectListServiceImpl implements ProxyAware<UserCentricExternalObjectListService>, CustomUserCentricExternalObjectListService {

	private UserIDProvider userIDProvider;
	private ExternalObjectListTypeService externalObjectListTypeService;
	private ExternalObjectListAssocationService externalObjectListAssociationService;
	private UserCentricExternalObjectListService thisProxy;

	@Override
	public void setThisProxy(UserCentricExternalObjectListService serviceProxy) {
		thisProxy = serviceProxy;
	}

	@Override
	public UserCentricExternalObjectList readByListTypeNameAndCurrentUser(String listTypeName) throws ClientException {
		UserCentricExternalObjectList proto = buildPrototype(listTypeName, userIDProvider.getCurrentUserID());
		try {
			return thisProxy.find(proto);
		} catch (UnknownResourceClientException e) {
			return null;
		}
	}

	@Override
	public UserCentricExternalObjectList assertExistsByListTypeNameAndUser(String listTypeName, User user) throws ClientException {
		try {
			ExternalObjectListType listType = thisProxy.modelAsReferableResource(externalObjectListTypeService.assertExists(listTypeName)).buildReference();
			user = thisProxy.modelAsReferableResource(user).buildReference();
			UserCentricExternalObjectList proto = buildPrototype(listType, user);
			try {
				return thisProxy.find(proto);
			} catch (UnknownResourceClientException e) {
				proto.setUser(user);
				proto.setType(listType);
				return thisProxy.create(proto);
			}
		} catch (ReferenceBuildingException e) {
			throw new IllegalStateException("could not create reference while looking up a list", e);
		}
	}

	@Override
	public void addExternalObjects(User user, String listTypeName, List<ExternalObjectListAssocation> externalObjects) throws ClientException {
		UserCentricExternalObjectList list = assertExistsByListTypeNameAndUser(listTypeName, user);
		List<ExternalObjectListAssocation> updateExternalObjectListAssociations = new ArrayList<>();
		List<ExternalObjectListAssocation> createExternalObjectListAssociations = new ArrayList<>();

		HashMap<String, ExternalObjectListAssocation> currentUserCentricExternalObjectAssociations = new HashMap<>(list.getAssociations().size());
		for (ExternalObjectListAssocation externalObjectListAssociation : list.getAssociations()) {
			currentUserCentricExternalObjectAssociations.put(externalObjectListAssociation.getExternalObject().getIdentifier(), externalObjectListAssociation);
		}

		for (ExternalObjectListAssocation externalObjectListAssociation : externalObjects) {

			ExternalObjectListAssocation addOrUpdateExternalObjectListAssociation = null;

			String identifier = externalObjectListAssociation.getExternalObject().getIdentifier();
			ExternalObjectListAssocation findToBeAddedOrUpdatedExternalObjectListAssociation = currentUserCentricExternalObjectAssociations.get(identifier.toString());

			// if externalObjectAssociation for externalObject aren't new, increase quantity and set modifcationDate with new Date()
			if (findToBeAddedOrUpdatedExternalObjectListAssociation != null
					&& findToBeAddedOrUpdatedExternalObjectListAssociation.getExternalObject() != null
					&& findToBeAddedOrUpdatedExternalObjectListAssociation.getExternalObject().getIdentifier() != null
					&& identifier.equals(findToBeAddedOrUpdatedExternalObjectListAssociation.getExternalObject().getIdentifier())) {
				addOrUpdateExternalObjectListAssociation = findToBeAddedOrUpdatedExternalObjectListAssociation;
			}

			if (addOrUpdateExternalObjectListAssociation != null) {
				BigDecimal currentQuantity = addOrUpdateExternalObjectListAssociation.getQuantity();
				currentQuantity = currentQuantity.add(externalObjectListAssociation.getQuantity());
				addOrUpdateExternalObjectListAssociation.setQuantity(currentQuantity);
				updateExternalObjectListAssociations.add(addOrUpdateExternalObjectListAssociation);
			} else {
				addOrUpdateExternalObjectListAssociation = new ExternalObjectListAssocationImpl();
				addOrUpdateExternalObjectListAssociation.setExternalObject(externalObjectListAssociation.getExternalObject());
				addOrUpdateExternalObjectListAssociation.setList(list);
				addOrUpdateExternalObjectListAssociation.setQuantity(externalObjectListAssociation.getQuantity());
				createExternalObjectListAssociations.add(addOrUpdateExternalObjectListAssociation);
			}
		}

		externalObjectListAssociationService.batchUpdate(updateExternalObjectListAssociations);
		externalObjectListAssociationService.batchCreate(createExternalObjectListAssociations);

	}

	@Override
	public UserCentricExternalObjectList trackFast(String userId, String listTypeName, String externalObjectIdentifier) throws ClientException {
		UserTrackingRestBean payload = new UserTrackingRestBean();
		payload.setExternalObjectIdentifier(externalObjectIdentifier);
		payload.setListTypeName(listTypeName);
		payload.setUserIdentifier(userId);
		String location = thisProxy.getPrimaryResourceClient().follow("trackFast").preventRedirect().execute(payload, String.class);
		UserCentricExternalObjectListImpl userCentricExternalObjectListImpl = new UserCentricExternalObjectListImpl();
		if (location != null) {
			userCentricExternalObjectListImpl.addLink("self", location, "GET");
			try {
				return userCentricExternalObjectListImpl.buildReference();
			} catch (ReferenceBuildingException ex) {
				return null;
			}
		} else {
			return null;
		}
	}

	private UserCentricExternalObjectList buildPrototype(String listTypeName, String uid) {
		User u = new UserImpl();
		u.setIdentifier(uid);
		return buildPrototype(listTypeName, u);
	}

	private UserCentricExternalObjectList buildPrototype(String listTypeName, User u) {
		ExternalObjectListType type = new ExternalObjectListTypeImpl();
		type.setName(listTypeName);
		return buildPrototype(type, u);
	}

	private UserCentricExternalObjectList buildPrototype(ExternalObjectListType listType, User u) {
		UserCentricExternalObjectList proto = new UserCentricExternalObjectListImpl();
		proto.setType(listType);
		proto.setUser(u);
		return proto;
	}

	public void setUserIDProvider(UserIDProvider userIDProvider) {
		this.userIDProvider = userIDProvider;
	}

	@ServiceReference
	public void setExternalObjectListTypeService(ExternalObjectListTypeService externalObjectListTypeService) {
		this.externalObjectListTypeService = externalObjectListTypeService;
	}

	@ServiceReference
	public void setExternalObjectListAssociationService(ExternalObjectListAssocationService externalObjectListAssociationService) {
		this.externalObjectListAssociationService = externalObjectListAssociationService;
	}

}
