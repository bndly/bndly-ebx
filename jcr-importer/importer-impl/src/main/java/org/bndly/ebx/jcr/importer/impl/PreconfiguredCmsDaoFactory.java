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
import org.bndly.ebx.jcr.importer.api.CmsDao;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.jcr.importer.api.PreconfiguredCmsDao;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = PreconfiguredCmsDaoFactory.class, immediate = true)
public class PreconfiguredCmsDaoFactory {

	private ServiceTracker<JCRImporterConfiguration, JCRImporterConfiguration> tracker;
	private final Map<Integer, ServiceRegistration> regs = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	@Reference
	private CmsDao cmsDao;

	@Activate
	public void activate(ComponentContext componentContext) {
		tracker = new ServiceTracker<JCRImporterConfiguration, JCRImporterConfiguration>(componentContext.getBundleContext(), JCRImporterConfiguration.class, null) {
			@Override
			public JCRImporterConfiguration addingService(ServiceReference<JCRImporterConfiguration> reference) {
				JCRImporterConfiguration r = super.addingService(reference);
				PreconfiguredCmsDao preconfigured = create(r);
				ServiceRegistration<PreconfiguredCmsDao> reg = ServiceRegistrationBuilder
						.newInstance(PreconfiguredCmsDao.class, preconfigured)
						.pid(PreconfiguredCmsDao.class.getName() + "." + preconfigured.getConfiguration().getName())
						.property("name", preconfigured.getConfiguration().getName())
						.register(context);
				lock.writeLock().lock();
				try {
					regs.put(System.identityHashCode(r), reg);
				} finally {
					lock.writeLock().unlock();
				}
				return r;
			}

			@Override
			public void removedService(ServiceReference<JCRImporterConfiguration> reference, JCRImporterConfiguration service) {
				lock.writeLock().lock();
				try {
					ServiceRegistration reg = regs.remove(System.identityHashCode(service));
					if (reg != null) {
						reg.unregister();
					}
				} finally {
					lock.writeLock().unlock();
				}
				super.removedService(reference, service);
			}

		};
		tracker.open();
	}

	@Deactivate
	public void deactivate(ComponentContext componentContext) {
		lock.writeLock().lock();
		try {
			for (ServiceRegistration value : regs.values()) {
				value.unregister();
			}
			regs.clear();
		} finally {
			lock.writeLock().unlock();
		}
		tracker.close();
	}

	private PreconfiguredCmsDao create(final JCRImporterConfiguration importerConfiguration) {
		return new PreconfiguredCmsDao() {
			@Override
			public <E> E run(CmsDao.JCRSessionCallback<E> callback) {
				return cmsDao.run(callback, importerConfiguration);
			}

			@Override
			public JCRImporterConfiguration getConfiguration() {
				return importerConfiguration;
			}
		};
	}
}
