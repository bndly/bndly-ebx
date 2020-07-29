/*
 * Copyright (c) 2013, cyber:con GmbH, Bonn.
 *
 * All rights reserved. This source file is provided to you for
 * documentation purposes only. No part of this file may be
 * reproduced or copied in any form without the written
 * permission of cyber:con GmbH. No liability can be accepted
 * for errors in the program or in the documentation or for damages
 * which arise through using the program. If an error is discovered,
 * cyber:con GmbH will endeavour to correct it as quickly as possible.
 * The use of the program occurs exclusively under the conditions
 * of the licence contract with cyber:con GmbH.
 */

package org.bndly.ebx.client.service.api;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service-api
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
import org.bndly.ebx.model.User;
import org.bndly.rest.client.exception.ClientException;

public interface CustomUserService {

    User readByExternalIdentifier(String externalIdentifier) throws ClientException;

    User readById(long id) throws ClientException;
    
    User readCurrentUser() throws ClientException;

    User createUserWithUserAttributeValues(User user) throws ClientException, ReferenceBuildingException;
    
    User assertCurrentUserExists() throws ClientException, ReferenceBuildingException;
    
    User assertCurrentUserExistsAsReference() throws ClientException;
    
    User assertUserExists(String userIdentifier) throws ClientException, ReferenceBuildingException;
    
    User assertUserExistsAsReference(String userIdentifier) throws ClientException;

}
