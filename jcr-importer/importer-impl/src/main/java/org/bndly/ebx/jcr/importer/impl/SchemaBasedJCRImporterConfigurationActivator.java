package org.bndly.ebx.jcr.importer.impl;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-impl
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

import org.bndly.common.osgi.util.ServiceRegistrationBuilder;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.model.ImporterConfiguration;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.listener.DeleteListener;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = SchemaBasedJCRImporterConfigurationActivator.class, immediate = true)
public class SchemaBasedJCRImporterConfigurationActivator implements PersistListener, MergeListener, DeleteListener {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaBasedJCRImporterConfigurationActivator.class);
	
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private final Map<Long, ServiceRegistration<JCRImporterConfiguration>> regsById = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private ComponentContext componentContext;
	
	@Activate
	public void activate(final ComponentContext componentContext) {
		this.componentContext = componentContext;
		LOG.info("activating schema based JCR Importer configuration");
		Iterator<Record> res = schemaBeanFactory.getEngine().getAccessor().query("PICK " + ImporterConfiguration.class.getSimpleName() + " i IF i.active=?", true);
		while (res.hasNext()) {
			LOG.info("found an active importer configuration");
			ImporterConfiguration importerConfiguration = schemaBeanFactory.getSchemaBean(ImporterConfiguration.class, res.next());
			registerConfig(importerConfiguration, componentContext);
		}
		schemaBeanFactory.getEngine().addListener(this);
	}

	@Deactivate
	public void deactivate(ComponentContext componentContext) {
		lock.writeLock().lock();
		try {
			for (ServiceRegistration<JCRImporterConfiguration> reg : regsById.values()) {
				reg.unregister();
			}
			regsById.clear();
		} finally {
			lock.writeLock().unlock();
		}
		schemaBeanFactory.getEngine().removeListener(this);
	}

	private void registerConfig(ImporterConfiguration importerConfiguration, ComponentContext componentContext) {
		SchemaBasedJCRImporterConfiguration cfg = new SchemaBasedJCRImporterConfiguration(importerConfiguration);
		ServiceRegistration<JCRImporterConfiguration> reg = ServiceRegistrationBuilder
				.newInstance(JCRImporterConfiguration.class, cfg)
				.pid(JCRImporterConfiguration.class.getName() + "." + cfg.getName())
				.property("name", cfg.getName())
				.register(componentContext.getBundleContext());
		lock.writeLock().lock();
		try {
			regsById.put(((ActiveRecord) importerConfiguration).getId(), reg);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void onPersist(Record record) {
		if (isRelevantRecord(record)) {
			ImporterConfiguration importerConfiguration = schemaBeanFactory.getSchemaBean(ImporterConfiguration.class, record);
			if (importerConfiguration.getActive() != null && importerConfiguration.getActive()) {
				registerConfig(importerConfiguration, componentContext);
			}
		}
	}

	@Override
	public void onMerge(Record record) {
		if (isRelevantRecord(record)) {
			lock.writeLock().lock();
			try {
				ServiceRegistration<JCRImporterConfiguration> reg = regsById.get(record.getId());
				if (reg != null) {
					JCRImporterConfiguration service = componentContext.getBundleContext().getService(reg.getReference());
					if (SchemaBasedJCRImporterConfiguration.class.isInstance(service)) {
						SchemaBasedJCRImporterConfiguration schemaConfig = (SchemaBasedJCRImporterConfiguration) service;
						schemaConfig.reloadConfig();
						if (schemaConfig.getImporterConfiguration().getActive() != null && !schemaConfig.getImporterConfiguration().getActive()) {
							// remove it, because it is no longer active
							reg.unregister();
							regsById.remove(record.getId());
						}
					}
				}
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	@Override
	public void onDelete(Record record) {
		if (isRelevantRecord(record)) {
			lock.writeLock().lock();
			try {
				ServiceRegistration<JCRImporterConfiguration> reg = regsById.remove(record.getId());
				if (reg != null) {
					reg.unregister();
				}
			} finally {
				lock.writeLock().unlock();
			}
		}
	}
	
	private boolean isRelevantRecord(Record record) {
		return record.getType().getName().equals(ImporterConfiguration.class.getSimpleName());
	}
}
