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
import org.bndly.common.service.cache.api.Cached;
import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.UserAttribute;
import org.bndly.ebx.model.UserAttributeValue;
import org.bndly.ebx.model.impl.UserAttributeValueImpl;
import org.bndly.ebx.model.impl.UserImpl;
import org.bndly.ebx.client.service.api.CustomUserService;
import org.bndly.ebx.client.service.api.UserAttributeService;
import org.bndly.ebx.client.service.api.UserService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomUserServiceImpl implements ProxyAware<UserService>, CustomUserService {

	private UserIDProvider userIDProvider;
	private UserAttributeService userAttributeService;
	private UserService thisProxy;

	@Override
	public void setThisProxy(UserService serviceProxy) {
		thisProxy = serviceProxy;
	}

	@Override
	public User createUserWithUserAttributeValues(User user) throws ClientException, ReferenceBuildingException {
		User createUser = assertUserExists(user.getIdentifier());

		List<UserAttributeValue> userAttributeValues = new ArrayList<>(user.getUserAttributeValues().size());
		for (UserAttributeValue userAttributeValue : user.getUserAttributeValues()) {
			if (userAttributeValue.getAttribute() != null && userAttributeValue.getAttribute().getName() != null) {
				UserAttribute userAttribute = userAttributeService.readOrCreateUserAttribute(userAttributeValue.getAttribute().getName());

				if (UserAttributeValueImpl.class.isInstance(userAttributeValue)) {
					((UserAttributeValueImpl) userAttributeValue).setId(null);
				}
				try {
					userAttributeValue.setAttribute(thisProxy.modelAsReferableResource(userAttribute).buildReference());
				} catch (ReferenceBuildingException e) {
					userAttributeValue.setAttribute(userAttribute);
				}
				try {
					userAttributeValue.setUser(thisProxy.modelAsReferableResource(createUser).buildReference());
				} catch (ReferenceBuildingException e) {
					userAttributeValue.setUser(createUser);
				}

				userAttributeValues.add(userAttributeValue);
			}
		}
		createUser.setUserAttributeValues(userAttributeValues);
		User response = thisProxy.update(createUser);

		return response;
	}

	@Override
	public User readByExternalIdentifier(String externalIdentifier) throws ClientException {
		User u = new UserImpl();
		u.setIdentifier(externalIdentifier);
		try {
			return thisProxy.find(u);
		} catch (UnknownResourceClientException e) {
			return null;
		}
	}

	@Override
	public User readById(long id) throws ClientException {
		UserImpl u = new UserImpl();
		u.setId(id);
		try {
			return thisProxy.find(u);
		} catch (UnknownResourceClientException e) {
			return null;
		}
	}

	@Override
	public User readCurrentUser() throws ClientException {
		return readByExternalIdentifier(userIDProvider.getCurrentUserID());
	}

	@Override
	@Cached
	public User assertCurrentUserExists() throws ClientException, ReferenceBuildingException {
		String userId = userIDProvider.getCurrentUserID();
		return assertUserExists(userId);
	}

	@Override
	@Cached
	public User assertUserExists(@CacheKeyParameter("userIdentifier") String userIdentifier) throws ClientException, ReferenceBuildingException {
		User user = new UserImpl();
		user.setIdentifier(userIdentifier);
		return thisProxy.assertExists(user);
	}

	@Override
	@Cached
	public User assertCurrentUserExistsAsReference() throws ClientException {
		String userId = userIDProvider.getCurrentUserID();
		return assertUserExistsAsReference(userId);
	}

	@Override
	public User assertUserExistsAsReference(String userIdentifier) throws ClientException {
		User u = new UserImpl();
		u.setIdentifier(userIdentifier);
		return thisProxy.assertExistsAsReference(u);
	}

	public void setUserIDProvider(UserIDProvider userIDProvider) {
		this.userIDProvider = userIDProvider;
	}

	@ServiceReference
	public void setUserAttributeService(UserAttributeService userAttributeService) {
		this.userAttributeService = userAttributeService;
	}
}
