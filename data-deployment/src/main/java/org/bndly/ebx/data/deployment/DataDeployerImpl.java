package org.bndly.ebx.data.deployment;

/*-
 * #%L
 * org.bndly.ebx.data-deployment
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

import org.bndly.common.data.api.Data;
import org.bndly.common.data.api.DataStore;
import org.bndly.common.data.api.DataStoreListener;
import org.bndly.common.data.api.FileExtensionContentTypeMapper;
import org.bndly.common.data.api.NoOpDataStoreListener;
import org.bndly.common.data.io.ReplayableInputStream;
import org.bndly.common.data.api.SimpleData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DataDeployer.class, immediate = true)
@Designate(ocd = DataDeployerImpl.Configuration.class)
public class DataDeployerImpl implements DataDeployer {

	@ObjectClassDefinition
	public @interface Configuration {
		@AttributeDefinition(
				name = "Folder to scan",
				description = "Folder that will be scanned for files that will be deployed to the binary data entity."
		)
		String folderToScan() default "";
	}
	
	@Reference(target = "(service.pid=org.bndly.common.data.api.DataStore.ebx)")
	private DataStore dataStore;

	@Reference(target = "(service.pid=org.bndly.common.data.api.FileExtensionContentTypeMapper)")
	private FileExtensionContentTypeMapper fileExtensionContentTypeMapper;

	private String folderToScan;
	
	private final List<DataDeploymentListener> listeners = new ArrayList<>();
	private final ReadWriteLock listenersLock = new ReentrantReadWriteLock();

	private static final Logger LOG = LoggerFactory.getLogger(DataDeployer.class);
	private boolean didDeploy;
	private DataStoreListener installListener;

	@Reference(
			bind = "addListener",
			unbind = "removeListener",
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC,
			service = DataDeploymentListener.class
	)
	@Override
	public void addListener(DataDeploymentListener listener) {
		if (listener != null) {
			listenersLock.writeLock().lock();
			try {
				listeners.add(listener);
				if (didDeploy) {
					listener.dataDeployed(this);
				}
			} finally {
				listenersLock.writeLock().unlock();
			}
		}
	}
	
	@Override
	public void removeListener(DataDeploymentListener listener) {
		if (listener != null) {
			listenersLock.writeLock().lock();
			try {
				Iterator<DataDeploymentListener> iterator = listeners.iterator();
				while (iterator.hasNext()) {
					DataDeploymentListener next = iterator.next();
					if (next == listener) {
						iterator.remove();
					}
				}
			} finally {
				listenersLock.writeLock().unlock();
			}
		}
	}

	@Activate
	public void activate(ComponentContext componentContext) {
		listenersLock.writeLock().lock();
		try {
			Dictionary<String, Object> props = componentContext.getProperties();
			if (props != null) {
				folderToScan = (String) props.get("folderToScan");
			}
			installListener = new NoOpDataStoreListener() {

				@Override
				public void dataStoreIsReady(DataStore dataStore) {
					insertData();
				}

			};
			if(dataStore.isReady()) {
				insertData();
			} else {
				dataStore.addListener(installListener);
			}
		} finally {
			listenersLock.writeLock().unlock();
		}
	}
	
	@Deactivate
	public void deactivate() {
		listenersLock.writeLock().lock();
		try {
			dataStore.removeListener(installListener);
		} finally {
			listenersLock.writeLock().unlock();
		}
	}

	public void insertData() {
		if (folderToScan == null || "".equals(folderToScan)) {
			LOG.warn("no folder to scan is configured");
			return;
		}
		File folder = new File(folderToScan);
		if (!folder.exists()) {
			LOG.warn("folder {} to scan does not exist", folderToScan);
			return;
		}
		listenersLock.writeLock().lock();
		try {
			LOG.info("deploying data of {}", folderToScan);
			insertDataOfFolder(folder, null);
			LOG.info("finished deploying data of {}", folderToScan);
			didDeploy = true;
		} finally {
			listenersLock.writeLock().unlock();
		}
		listenersLock.readLock().lock();
		try {
			for (DataDeploymentListener dataDeploymentListener : listeners) {
				dataDeploymentListener.dataDeployed(this);
			}
		} finally {
			listenersLock.readLock().unlock();
		}
	}

	private void insertDataOfFolder(File folder, String prefix) {
		if (folder == null) {
			return;
		}

		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			for (final File file : files) {
				if (file.isHidden()) {
					continue;
				}
				if (file.isDirectory()) {
					String newPrefix = prefix == null ? file.getName() + "/" : prefix + file.getName() + "/";
					insertDataOfFolder(file, newPrefix);
					continue;
				}
				String dataName = file.getName();
				if (prefix != null) {
					dataName = prefix + dataName;
				}
				Data d = dataStore.findByName(dataName);
				long lastModifiedMillis = file.lastModified();
				long lastDataModifiedMillis = -1;
				if (d != null) {
					Date date = d.getUpdatedOn() != null ? d.getUpdatedOn() : d.getCreatedOn();
					if (date != null) {
						lastDataModifiedMillis = date.getTime();
					}
				}
				if (d == null || lastDataModifiedMillis < lastModifiedMillis) {
					try {
						final ReplayableInputStream is = ReplayableInputStream.newInstance(new FileInputStream(file));
						SimpleData cd;
						if (d == null || !SimpleData.class.isInstance(d)) {
							cd = new SimpleData(new SimpleData.LazyLoader() {

								@Override
								public ReplayableInputStream getBytes() {
									return is;
								}
							});
						} else {
							cd = (SimpleData) d;
							cd.setInputStream(is);
						}
						cd.setName(dataName);
						String contentType = null;
						int extensionStart = dataName.lastIndexOf(".");
						String extension = extensionStart < 0 ? null : dataName.substring(extensionStart + 1);

						if (extension != null) {
							contentType = fileExtensionContentTypeMapper.mapExtensionToContentType(extension);
						}
						if (contentType == null) {
							if (dataName.endsWith(".xml")) {
								contentType = "application/xml";

							} else if (dataName.endsWith(".css")) {
								contentType = "text/css";

							} else if (dataName.endsWith(".jpg")) {
								contentType = "image/jpeg";

							} else if (dataName.endsWith(".gif")) {
								contentType = "image/gif";

							} else if (dataName.endsWith(".png")) {
								contentType = "image/png";

							} else if (dataName.endsWith(".vm")) {
								contentType = "text/x-velocity";

							} else if (dataName.endsWith(".pdf")) {
								contentType = "application/pdf";

							} else if (dataName.endsWith(".json")) {
								contentType = "application/json";
							}

						}
						cd.setContentType(contentType);
						cd.setCreatedOn(d == null ? new Date() : d.getCreatedOn());
						Date updatedOn = new Date();
						updatedOn.setTime(lastModifiedMillis);
						cd.setUpdatedOn(d == null ? null : updatedOn);
						if (d == null) {
							dataStore.create(cd);
						} else {
							dataStore.update(cd);
						}
					} catch (IOException ex) {
						LOG.error("failed to deploy data for {}", dataName, ex);
					}
				}
			}
		}
	}

	public void setFolderToScan(String folderToScan) {
		this.folderToScan = folderToScan;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public void setSchemaDeploymentListeners(List schemaDeploymentListeners) {
		schemaDeploymentListeners.add(this);
	}

	@Override
	public boolean didDeploy() {
		return didDeploy;
	}

}
