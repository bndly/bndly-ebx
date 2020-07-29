package org.bndly.ebx.client.impl;

/*-
 * #%L
 * org.bndly.ebx.client.spring-context
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

import org.bndly.ebx.client.service.api.UserIDProvider;

public class TestUserIDProviderImpl implements UserIDProvider {

    private String userId;
    private boolean registered;
    private boolean loggedIn;

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public void setCurrentUserID(String id) {
        setUserId(id);
    }

    @Override
    public String getCurrentUserID() {
        return getUserId();
    }

    @Override
    public Object getElasticSocialUser() {
        //we don't need that
        return null;
    }

    @Override
    public boolean isRegisteredUser() {
        return isRegistered();
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public String getDefaultServiceName() {
        return UserIDProvider.NAME;
    }
    
}
