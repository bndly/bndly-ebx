package org.bndly.ebx.client.servicepool.impl;

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

import org.bndly.common.service.model.api.ReferableResource;
import org.bndly.common.service.shared.api.GenericResourceService;
import org.bndly.common.service.shared.api.RegistrableService;
import org.bndly.common.service.shared.api.ServicePool;
import java.util.HashMap;
import java.util.Map;

public class ShopServicePoolImpl implements ServicePool {

	private final Map<String, RegistrableService> servicePoolMap = new HashMap<>();
	private final Map<Class<?>, RegistrableService> servicePoolMapByType = new HashMap<>();
	private final Map<Class<?>, GenericResourceService> servicePoolMapByModelType = new HashMap<>();

	@Override
	public void register(String registrableServiceName, RegistrableService service) {
		if (registrableServiceName != null && getService(registrableServiceName) == null) {
			servicePoolMap.put(registrableServiceName, service);
			register(service, service.getClass());
		}
	}

	@Override
	public void register(RegistrableService service) {
		register(service.getDefaultServiceName(), service);
	}

	private void register(RegistrableService service, Class<?> type) {
		if (RegistrableService.class.isAssignableFrom(type) && !RegistrableService.class.equals(type)) {
			servicePoolMapByType.put(type, service);
		}

		if (GenericResourceService.class.isAssignableFrom(type)) {
			Class<?> modelClass = ((GenericResourceService) service).getModelClass();
			servicePoolMapByModelType.put(modelClass, (GenericResourceService) service);
		}

		Class<?> sc = type.getSuperclass();
		if (sc != null) {
			register(service, sc);
		}
		Class<?>[] interfaces = type.getInterfaces();
		if (interfaces != null) {
			for (Class<?> class1 : interfaces) {
				register(service, class1);
			}
		}
	}

	@Override
	public <DOMAINMODEL extends ReferableResource> void register(Class<DOMAINMODEL> modelType, GenericResourceService service) {
		if (getService(modelType) == null) {
			servicePoolMapByModelType.put(modelType, service);
		}
	}

	@Override
	public <SERVICE extends GenericResourceService, DOMAINMODEL> SERVICE getService(Class<DOMAINMODEL> model) {
		SERVICE service = (SERVICE) servicePoolMapByModelType.get(model);
		return service;
	}

	@Override
	public <SERVICE extends GenericResourceService, DOMAINMODEL extends ReferableResource> SERVICE getService(Class<DOMAINMODEL> model, Class isAssignableFrom) {
		SERVICE service = getService(model);
		if (service != null && isAssignableFrom.isAssignableFrom(service.getClass())) {
			return service;
		} else {
			return null;
		}
	}

	@Override
	public < SERVICE extends RegistrableService> SERVICE getService(String registratedServiceClassName) {
		if (registratedServiceClassName != null) {
			return (SERVICE) servicePoolMap.get(registratedServiceClassName);
		}

		return null;
	}

	@Override
	public <SERVICE extends RegistrableService> SERVICE getServiceByType(Class<SERVICE> serviceType) {
		return (SERVICE) servicePoolMapByType.get(serviceType);
	}

}
