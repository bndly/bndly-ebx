/*
 * Copyright (c) 2011, cyber:con GmbH, Bonn.
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
package org.bndly.rest.resources.jcr;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-resource
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

import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.beans.jcr.JCRImporterStatusRestBean;
import org.bndly.rest.controller.api.CacheControl;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bndly.ebx.jcr.importer.api.JCRImporter;
import org.bndly.rest.common.beans.Services;
import org.bndly.rest.controller.api.QueryParam;
import org.bndly.rest.entity.resources.EntityResource;
import org.bndly.rest.entity.resources.EntityResourceDeploymentListener;
import org.bndly.rest.entity.resources.SchemaAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Path("jcr-importer")
@Component(service = {ImporterResourceImpl.class, EntityResourceDeploymentListener.class}, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = ImporterResourceImpl.Configuration.class)
public class ImporterResourceImpl implements EntityResourceDeploymentListener {

	@ObjectClassDefinition
	public @interface Configuration {
		@AttributeDefinition(name = "JCR Importer", description = "A filter expression to retrieve the JCR Importer to hook up into the resource.")
		String jcrImporter_target();
	}
	
	public static Logger LOG = LoggerFactory.getLogger(ImporterResourceImpl.class);

	@Reference(name = "jcrImporter")
	private JCRImporter jcrImporter;
	
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;
	
	private final Map<Integer, TypeSpecificImporterResourceImpl> subResources = new HashMap<>();
	private final ReadWriteLock subResourcesLock = new ReentrantReadWriteLock();

	public JCRImporter getJcrImporter() {
		return jcrImporter;
	}

	public SchemaBeanFactory getSchemaBeanFactory() {
		return schemaBeanFactory;
	}

	public ControllerResourceRegistry getControllerResourceRegistry() {
		return controllerResourceRegistry;
	}

	@Activate
	public void activate() {
		controllerResourceRegistry.deploy(this);
		subResourcesLock.readLock().lock();
		try {
			for (TypeSpecificImporterResourceImpl value : subResources.values()) {
				value.activate();
			}
		} finally {
			subResourcesLock.readLock().unlock();
		}
	}

	@Deactivate
	public void deactivate() {
		subResourcesLock.writeLock().lock();
		try {
			for (TypeSpecificImporterResourceImpl value : subResources.values()) {
				value.deactivate();
			}
			subResources.clear();
		} finally {
			subResourcesLock.writeLock().unlock();
		}
		controllerResourceRegistry.undeploy(this);
	}

	@AtomLink(rel = "jcrimporter", target = Services.class)
	@GET
	@CacheControl(preventCaching = true)
	public Response getStatus(@QueryParam("testConnection") Integer testConnectionRetries) {
		boolean connected = jcrImporter.isConnected();
		if (testConnectionRetries != null) {
			testConnectionRetries = testConnectionRetries > 10 ? 10 : testConnectionRetries;
			connected = jcrImporter.establishTestConnection(testConnectionRetries);
		}
		JCRImporterStatusRestBean status = new JCRImporterStatusRestBean();
		status.setEnabled(jcrImporter.isEnabled());
		status.setRunning(jcrImporter.isRunning());
		status.setDone(jcrImporter.getDone());
		status.setConnected(connected);
		status.setRepositoryUrl(jcrImporter.getConfig().getUrl());
		return Response.ok(status);
	}
	
	@AtomLink(rel = "trigger", target = JCRImporterStatusRestBean.class)
	@GET
	@Path("trigger")
	@CacheControl(preventCaching = true)
	public synchronized Response trigger() {
		LOG.info("Triggered REST trigger()");
		if (jcrImporter.isEnabled()) {
			jcrImporter.requestImport();
			return Response.NO_CONTENT;
		} else {
			return Response.status(500).entity("Importer is disabled in configuration.\n");
		}
	}

	@AtomLink(rel = "fullSync", target = JCRImporterStatusRestBean.class)
	@GET
	@Path("fullSync")
	@CacheControl(preventCaching = true)
	public synchronized Response fullSync() {
		if (jcrImporter.isEnabled()) {
			jcrImporter.requestFullsynchronization();
			return Response.NO_CONTENT;
		} else {
			return Response.status(500).entity("Importer is disabled in configuration.\n");
		}
	}
	
	public void setJCRImporter(JCRImporter jcrImporter) {
		this.jcrImporter = jcrImporter;
	}

	@Override
	public void deployed(SchemaAdapter sa, EntityResource er) {
		TypeSpecificImporterResourceImpl typeSpecificImporterResourceImpl = new TypeSpecificImporterResourceImpl(er, this);
		subResourcesLock.writeLock().lock();
		try {
			TypeSpecificImporterResourceImpl previous = subResources.put(System.identityHashCode(er), typeSpecificImporterResourceImpl);
			if (previous != null) {
				previous.deactivate();
			}
			typeSpecificImporterResourceImpl.activate();
		} finally {
			subResourcesLock.writeLock().unlock();
		}
	}

	@Override
	public void undeployed(SchemaAdapter sa, EntityResource er) {
		subResourcesLock.writeLock().lock();
		try {
			TypeSpecificImporterResourceImpl typeSpecificImporterResourceImpl = subResources.remove(System.identityHashCode(er));
			if (typeSpecificImporterResourceImpl != null) {
				typeSpecificImporterResourceImpl.deactivate();
			}
		} finally {
			subResourcesLock.writeLock().unlock();
		}
	}

}
