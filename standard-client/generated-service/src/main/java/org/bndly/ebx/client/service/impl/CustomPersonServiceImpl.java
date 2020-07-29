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

import org.bndly.common.event.api.EventBus;
import org.bndly.common.event.api.ListenerIterator;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.impl.PersonImpl;
import org.bndly.ebx.client.service.api.CustomPersonService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.PersonService;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import javax.annotation.PostConstruct;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomPersonServiceImpl implements CustomPersonService, ProxyAware<PersonService> {

    private UserIDProvider userIDProvider;
    private EventBus eventBus;
    private PersonService thisProxy;

    @Override
    public Person readById(long id) throws ClientException {
        PersonImpl p =new PersonImpl();
        p.setId(id);
		try {
			return thisProxy.find(p);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public Person readByExternalUserId(String externalUserId) throws ClientException {
        Person p =new PersonImpl();
        p.setExternalUserId(externalUserId);
		try {
			return thisProxy.find(p);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public Person assertCurrentUserExistsAsPerson() throws ClientException {
        Person p = readCurrentUserAsPerson();
        if(p == null) {
            String userId = userIDProvider.getCurrentUserID();
            final Person prePersisted = new PersonImpl();
            prePersisted.setExternalUserId(userId);
            eventBus.fireEvent(PersonService.PersonPrePersistListener.class, new ListenerIterator<PersonPrePersistListener>(){
                @Override
                public void fireEvent(PersonPrePersistListener listener) {
                    listener.personPrePresist(prePersisted);
                }
            });
            p = thisProxy.create(prePersisted);
        }
        return p;
    }

    @PostConstruct
    public void registerEvent(){
        eventBus.registerEvent(PersonService.PersonPrePersistListener.class);
    }

    @Override
    public Person readCurrentUserAsPerson() throws ClientException {
        String userId = userIDProvider.getCurrentUserID();
        return readByExternalUserId(userId);
    }

    public void setUserIDProvider(UserIDProvider userIDProvider) {
        this.userIDProvider = userIDProvider;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void setThisProxy(PersonService serviceProxy) {
        thisProxy = serviceProxy;
    }
	
	
}
