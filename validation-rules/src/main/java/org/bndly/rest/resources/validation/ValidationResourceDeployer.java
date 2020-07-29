package org.bndly.rest.resources.validation;

/*-
 * #%L
 * org.bndly.ebx.validation-rules
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

import org.bndly.ebx.validation.RuleSetProvider;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.entity.resources.EntityResource;
import org.bndly.rest.entity.resources.EntityResourceDeploymentListener;
import org.bndly.rest.entity.resources.SchemaAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = {ValidationResourceDeployer.class, EntityResourceDeploymentListener.class}, immediate = true)
public class ValidationResourceDeployer implements EntityResourceDeploymentListener {

	@Reference
	private RuleSetProvider ruleSetProvider;
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;

	private final List<ValidationResource> deployedResources = new ArrayList<>();
	private final ReadWriteLock deployedResourcesLock = new ReentrantReadWriteLock();

	@Deactivate
	public void deactivate() {
		deployedResourcesLock.writeLock().lock();
		try {
			for (ValidationResource validationResource : deployedResources) {
				controllerResourceRegistry.undeploy(validationResource);
			}
			deployedResources.clear();
		} finally {
			deployedResourcesLock.writeLock().unlock();
		}
	}

	public void deploy(ValidationResource validationResource) {
		deployedResourcesLock.writeLock().lock();
		try {
			Iterator<ValidationResource> iterator = deployedResources.iterator();
			while (iterator.hasNext()) {
				ValidationResource next = iterator.next();
				if (next.getEntityResource() == validationResource.getEntityResource()) {
					// skip deployment
					return;
				}
			}
			controllerResourceRegistry.deploy(validationResource, validationResource.getSegment());
			deployedResources.add(validationResource);
		} finally {
			deployedResourcesLock.writeLock().unlock();
		}
	}

	public void undeploy(EntityResource entityResource) {
		deployedResourcesLock.writeLock().lock();
		try {
			Iterator<ValidationResource> iterator = deployedResources.iterator();
			while (iterator.hasNext()) {
				ValidationResource next = iterator.next();
				if (next.getEntityResource() == entityResource) {
					iterator.remove();
					controllerResourceRegistry.undeploy(next);
				}
			}
		} finally {
			deployedResourcesLock.writeLock().unlock();
		}
	}

	public void setRuleSetProvider(RuleSetProvider ruleSetProvider) {
		this.ruleSetProvider = ruleSetProvider;
	}

	@Override
	public void deployed(SchemaAdapter schemaAdapter, EntityResource entityResource) {
		ValidationResource r = new ValidationResource(entityResource);
		r.setRuleSetProvider(ruleSetProvider);
		deploy(r);
	}

	@Override
	public void undeployed(SchemaAdapter schemaAdapter, EntityResource entityResource) {
		undeploy(entityResource);
	}

}
