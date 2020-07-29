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

import org.bndly.ebx.model.ExternalObjectListAssocation;
import org.bndly.rest.client.exception.ClientException;

import java.util.Collection;

/**
 * Created by alexp on 17.06.15.
 */
public interface CustomExternalObjectListAssocationService {

    ExternalObjectListAssocation readById(long id) throws ClientException;

    void batchCreate(Collection<ExternalObjectListAssocation> batch) throws ClientException;

    void batchDelete(Collection<ExternalObjectListAssocation> batch) throws ClientException;

    void batchUpdate(Collection<ExternalObjectListAssocation> batch) throws ClientException;
}
