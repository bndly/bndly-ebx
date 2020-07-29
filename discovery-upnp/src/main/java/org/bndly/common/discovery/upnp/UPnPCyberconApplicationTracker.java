package org.bndly.common.discovery.upnp;

/*-
 * #%L
 * org.bndly.ebx.discovery-upnp
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

import static org.bndly.common.discovery.upnp.Activator.STATUS_DEVICE_TYPE;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class UPnPCyberconApplicationTracker extends ServiceTracker<UPnPDevice, UPnPDevice> {

	private static final Logger LOG = LoggerFactory.getLogger(UPnPCyberconApplicationTracker.class);
	
	private final Map<Integer, UPnPCyberconApplication> applicationsByDevice = new HashMap<>();
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	
	public UPnPCyberconApplicationTracker(BundleContext context) throws InvalidSyntaxException {
		super(context, context.createFilter("(&"
				+ "(" + Constants.OBJECTCLASS + "=" + UPnPDevice.class.getName() + ")"
				+ "(" + UPnPDevice.TYPE + "=" + STATUS_DEVICE_TYPE + ")"
				+ "(!(" + UPnPDevice.UPNP_EXPORT + "=*))"
				+ ")"), null);
	}

	@Override
	public UPnPDevice addingService(ServiceReference<UPnPDevice> reference) {
		readWriteLock.writeLock().lock();
		try {
			LOG.info("found UPnP device for Cybercon Application");
			UPnPDevice device = super.addingService(reference);
			UPnPCyberconApplication bndlyApplication = new UPnPCyberconApplication(device);
			LOG.info("Cybercon Application @ URL={}", bndlyApplication.getURL());
			applicationsByDevice.put(System.identityHashCode(device), bndlyApplication);
			return device;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public void removedService(ServiceReference<UPnPDevice> reference, UPnPDevice device) {
		readWriteLock.writeLock().lock();
		try {
			UPnPCyberconApplication bndlyApplication = applicationsByDevice.remove(System.identityHashCode(device));
			if (bndlyApplication.getDevice() != device) {
				applicationsByDevice.put(System.identityHashCode(device), bndlyApplication);
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
	
}
