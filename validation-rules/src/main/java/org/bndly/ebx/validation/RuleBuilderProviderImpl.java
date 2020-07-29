package org.bndly.ebx.validation;

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

import org.bndly.common.data.api.ChangeableData;
import org.bndly.common.data.api.Data;
import org.bndly.common.data.api.DataStore;
import org.bndly.common.data.api.DataStoreListener;
import org.bndly.common.data.io.ReplayableInputStream;
import org.bndly.common.data.api.SimpleData;
import org.bndly.common.data.io.SmartBufferOutputStream;
import org.bndly.common.service.validation.RuleSetRestBean;
import org.bndly.common.service.validation.RulesRestBean;
import org.bndly.rest.api.ContentType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {RuleSetProvider.class, RuleBuilderProvider.class}, immediate = true)
public class RuleBuilderProviderImpl implements RuleBuilderProvider, RuleSetProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(RuleBuilderProviderImpl.class);

	private final List<DeployedDataStore> dataStores = new ArrayList<>();
	private final ReadWriteLock dataStoresLock = new ReentrantReadWriteLock();
	
	private final Map<String, RestBeanRelatedRuleBuilder> ruleBuilders = new HashMap<>();
	private final ReadWriteLock ruleBuildersLock = new ReentrantReadWriteLock();
	private final Set<String> _knownRuleSets = new HashSet<>();
	private JAXBContext ctx;
	private ServiceTracker<DataStore, DeployedDataStore> dataStoreTracker;
	private ServiceTracker<RestBeanRelatedRuleBuilder, RestBeanRelatedRuleBuilder> ruleBuilderTracker;

	@Activate
	public void activate(BundleContext bundleContext) {
		try {
			ctx = JAXBContext.newInstance(RuleSetRestBean.class);
		} catch (JAXBException ex) {
			LOG.error("tried to parse rule set data, but jaxb failed: "+ex.getMessage(), ex);
			return;
		}

		// start tracking data stores and rule builders
		dataStoreTracker = new ServiceTracker<DataStore, DeployedDataStore>(bundleContext, DataStore.class, null) {
			@Override
			public DeployedDataStore addingService(ServiceReference<DataStore> reference) {
				dataStoresLock.writeLock().lock();
				try {
					DataStore dataStore = bundleContext.getService(reference);
					DeployedDataStore deployedDataStore = new DeployedDataStore(dataStore);
					dataStores.add(deployedDataStore);
					deployedDataStore.init();
					return deployedDataStore;
				} finally {
					dataStoresLock.writeLock().unlock();
				}
			}

			@Override
			public void removedService(ServiceReference<DataStore> reference, DeployedDataStore service) {
				dataStoresLock.writeLock().lock();
				try {
					service.destruct();
					Iterator<DeployedDataStore> iterator = dataStores.iterator();
					while (iterator.hasNext()) {
						if (iterator.next() == service) {
							iterator.remove();
						}
					}
					super.removedService(reference, service);
				} finally {
					dataStoresLock.writeLock().unlock();
				}
			}
		};
		ruleBuilderTracker = new ServiceTracker<RestBeanRelatedRuleBuilder,RestBeanRelatedRuleBuilder>(bundleContext, RestBeanRelatedRuleBuilder.class, null) {
			@Override
			public RestBeanRelatedRuleBuilder addingService(ServiceReference<RestBeanRelatedRuleBuilder> reference) {
				RestBeanRelatedRuleBuilder ruleBuilder = super.addingService(reference);
				addRuleBuilder(ruleBuilder);
				return ruleBuilder;
			}

			@Override
			public void removedService(ServiceReference<RestBeanRelatedRuleBuilder> reference, RestBeanRelatedRuleBuilder ruleBuilder) {
				removeRuleBuilder(ruleBuilder);
				super.removedService(reference, ruleBuilder);
			}
		};
		dataStoreTracker.open();
		ruleBuilderTracker.open();

	}

	@Deactivate
	public void deactivate() {
		if (dataStoreTracker != null) {
			dataStoreTracker.close();
			dataStoreTracker = null;
		}
		if(ruleBuilderTracker != null) {
			ruleBuilderTracker.close();
			ruleBuilderTracker = null;
		}
	}

	private class DeployedDataStore implements DataStoreListener {
		private final DataStore dataStore;

		public DeployedDataStore(DataStore dataStore) {
			this.dataStore = dataStore;
		}

		public DataStore getDataStore() {
			return dataStore;
		}

		private void destruct() {
			dataStore.removeListener(this);
			if(dataStore.isReady()) {
				dataStoreClosed(dataStore);
			}
		}
		
		private void init() {
			dataStore.addListener(this);
			if(dataStore.isReady()) {
				dataStoreIsReady(dataStore);
			}
		}

		@Override
		public void dataStoreIsReady(DataStore dataStore) {
			// makes sure, that all rulesets are deployed after a fresh installation
			List<RestBeanRelatedRuleBuilder> allRuleBuilders = getAllRuleBuilders();
			if (allRuleBuilders != null) {
				for (RestBeanRelatedRuleBuilder restBeanRelatedRuleBuilder : allRuleBuilders) {
					deployRulesOfRuleBuilder(restBeanRelatedRuleBuilder, dataStore, false);
				}
			}
		}

		@Override
		public void dataStoreClosed(DataStore dataStore) {
			// no-op
		}

		@Override
		public Data dataCreated(DataStore dataStore, Data data) {
			if(isRulesData(data)) {
				RuleSetRestBean rs = buildRuleSetFromData(data);
				if (rs != null) {
					// attach the rule data
					deployRulesOfRuleBuilder(rs, dataStore, true);
				}
			}
			return data;
		}

		@Override
		public Data dataUpdated(DataStore dataStore, Data data) {
			if(isRulesData(data)) {
				RuleSetRestBean rs = buildRuleSetFromData(data);
				if (rs != null) {
					// attach the rule data
					deployRulesOfRuleBuilder(rs, dataStore, true);
				}
			}
			return data;
		}

		@Override
		public Data dataDeleted(DataStore dataStore, Data data) {
			if(isRulesData(data)) {
				// detach the rule data
				String name = getRuleSetNameForData(data);
				if(name != null) {
					undeployRulesOfRuleBuilder(name, dataStore, true);
				}
			}
			return data;
		}
		
	}
	
	private void deployRulesOfRuleBuilder(RestBeanRelatedRuleBuilder ruleBuilder, DataStore dataStore, boolean preventDataStoreEventing) {
		RulesRestBean rules = ruleBuilder.buildRules();
		RuleSetRestBean rs = new RuleSetRestBean();
		rs.setName(ruleBuilder.getRestModelType().getSimpleName());
		rs.setRules(rules);
		deployRulesOfRuleBuilder(rs, dataStore, preventDataStoreEventing);
	}
	
	private void _deployRulesOfRuleBuilder(RuleSetRestBean rs, DataStore dataStore, boolean preventDataStoreEventing, boolean overwriteExisting) {
		String dataName = getDataNameForRuleSet(rs);
		if(!preventDataStoreEventing) {
			if(dataStore != null) {
					Data d = dataStore.findByNameAndContentType(dataName, ContentType.XML.getName());
					if (d == null || overwriteExisting) {
						saveRuleSetToDataStore(rs, dataStore);
					}
			} else {
				dataStoresLock.readLock().lock();
				try {
					Iterator<DeployedDataStore> iterator = dataStores.iterator();
					while (iterator.hasNext()) {
						DeployedDataStore next = iterator.next();
						DataStore oneOfManyDataStores = next.getDataStore();
						Data d = oneOfManyDataStores.findByNameAndContentType(dataName, ContentType.XML.getName());
						if (d == null) {
							saveRuleSetToDataStore(rs, oneOfManyDataStores);
						}
					}
				} finally {
					dataStoresLock.readLock().unlock();
				}
			}
		}
		_knownRuleSets.add(rs.getName());
	}
	
	private void deployRulesOfRuleBuilder(RuleSetRestBean rs, DataStore dataStore, boolean preventDataStoreEventing) {
		_deployRulesOfRuleBuilder(rs, dataStore, preventDataStoreEventing, false);
	}
	private void deployRulesOfRuleBuilderOverride(RuleSetRestBean rs, DataStore dataStore, boolean preventDataStoreEventing) {
		_deployRulesOfRuleBuilder(rs, dataStore, preventDataStoreEventing, true);
	}

	private void undeployRulesOfRuleBuilder(RestBeanRelatedRuleBuilder ruleBuilder, DataStore dataStore, boolean preventDataStoreEventing) {
		undeployRulesOfRuleBuilder(ruleBuilder.getRestModelType().getSimpleName(), dataStore, preventDataStoreEventing);
	}
	
	private void undeployRulesOfRuleBuilder(String ruleSetName, DataStore dataStore, boolean preventDataStoreEventing) {
		RuleSetRestBean rs = new RuleSetRestBean();
		rs.setName(ruleSetName);
		if(!preventDataStoreEventing) {
			String dataName = getDataNameForRuleSet(rs);
			if(dataStore != null) {
				Data d = dataStore.findByNameAndContentType(dataName, ContentType.XML.getName());
				if (d == null) {
					dataStore.delete(d);
				}
			} else {
				dataStoresLock.readLock().lock();
				try {
					Iterator<DeployedDataStore> iterator = dataStores.iterator();
					while (iterator.hasNext()) {
						DeployedDataStore next = iterator.next();
						DataStore oneOfManyDataStores = next.getDataStore();
						Data d = oneOfManyDataStores.findByNameAndContentType(dataName, ContentType.XML.getName());
						if (d == null) {
							oneOfManyDataStores.delete(d);
						}
					}
				} finally {
					dataStoresLock.readLock().unlock();
				}
			}
		}
		_knownRuleSets.remove(ruleSetName);
	}

//	@Reference(
//			bind = "registerDataStore",
//			unbind = "unregisterDataStore",
//			cardinality = ReferenceCardinality.MULTIPLE,
//			policy = ReferencePolicy.DYNAMIC,
//			service = DataStore.class
//	)
//	public void registerDataStore(DataStore dataStore) {
//		if(dataStore != null) {
//			DeployedDataStore deployedDataStore = new DeployedDataStore(dataStore);
//			dataStores.add(deployedDataStore);
//			deployedDataStore.init();
//		}
//	}
//	public void unregisterDataStore(DataStore dataStore) {
//		if(dataStore != null) {
//			Iterator<DeployedDataStore> iterator = dataStores.iterator();
//			while (iterator.hasNext()) {
//				DeployedDataStore next = iterator.next();
//				if(next.getDataStore() == dataStore) {
//					next.destruct();
//					iterator.remove();
//				}
//			}
//		}
//	}
	
	@Override
	public RestBeanRelatedRuleBuilder getRuleBuilderForClass(String className) {
		ruleBuildersLock.readLock().lock();
		try {
			return ruleBuilders.get(className);
		} finally {
			ruleBuildersLock.readLock().unlock();
		}
	}

	@Override
	public boolean ruleBuilderForClassExists(String className) {
		return getRuleBuilderForClass(className) != null;
	}

	@Override
	public List<RestBeanRelatedRuleBuilder> getAllRuleBuilders() {
		ruleBuildersLock.readLock().lock();
		try {
			List<RestBeanRelatedRuleBuilder> list = new ArrayList<>();
			for (Map.Entry<String, RestBeanRelatedRuleBuilder> entry : ruleBuilders.entrySet()) {
				RestBeanRelatedRuleBuilder restBeanRelatedRuleBuilder = entry.getValue();
				list.add(restBeanRelatedRuleBuilder);
			}
			return list;
		} finally {
			ruleBuildersLock.readLock().unlock();
		}
	}

//	@Reference(
//			bind = "addRuleBuilder",
//			unbind = "removeRuleBuilder",
//			cardinality = ReferenceCardinality.MULTIPLE,
//			policy = ReferencePolicy.DYNAMIC,
//			service = RestBeanRelatedRuleBuilder.class
//	)
	@Override
	public void addRuleBuilder(RestBeanRelatedRuleBuilder ruleBuilder) {
		ruleBuildersLock.writeLock().lock();
		try {
			ruleBuilders.put(ruleBuilder.getRestModelType().getSimpleName(), ruleBuilder);
			deployRulesOfRuleBuilder(ruleBuilder, null, false);

		} finally {
			ruleBuildersLock.writeLock().unlock();
		}
	}

	@Override
	public void removeRuleBuilder(RestBeanRelatedRuleBuilder ruleBuilder) {
		ruleBuildersLock.writeLock().lock();
		try {
			ruleBuilders.remove(ruleBuilder.getRestModelType().getSimpleName());
			undeployRulesOfRuleBuilder(ruleBuilder, null, false);
		} finally {
			ruleBuildersLock.writeLock().unlock();
		}
	}

	@Override
	public void saveRuleSet(final RuleSetRestBean ruleSetRestBean) {
		dataStoresLock.readLock().lock();
		try {
			Iterator<DeployedDataStore> iterator = dataStores.iterator();
			while (iterator.hasNext()) {
				DeployedDataStore next = iterator.next();
				deployRulesOfRuleBuilderOverride(ruleSetRestBean, next.getDataStore(), false);
			}
		} finally {
			dataStoresLock.readLock().unlock();
		}
	}
	
	public void saveRuleSetToDataStore(final RuleSetRestBean ruleSetRestBean, DataStore dataStore) {
		try {
			final Marshaller marshaller = ctx.createMarshaller();

			SimpleData.LazyLoader ll = new SimpleData.LazyLoader() {

				@Override
				public ReplayableInputStream getBytes() {
					try {
						try (SmartBufferOutputStream smartBufferOutputStream = SmartBufferOutputStream.newInstance()) {
							marshaller.marshal(ruleSetRestBean, smartBufferOutputStream);
							smartBufferOutputStream.flush();
							return smartBufferOutputStream.getBufferedDataAsReplayableStream();
						} catch (IOException ex) {
							throw new IllegalStateException("could not write xml of rulesetrestbean to buffer: " + ex.getMessage(), ex);
						}
					} catch (JAXBException ex) {
						throw new IllegalStateException("could not marshall rulesetrestbean: " + ex.getMessage(), ex);
					}
				}
			};
			String dataName = getDataNameForRuleSet(ruleSetRestBean);
			Data d = dataStore.findByNameAndContentType(dataName, ContentType.XML.getName());
			if (d != null) {
				if (ChangeableData.class.isInstance(d)) {
					ChangeableData cd = (ChangeableData) d;
					cd.setInputStream(ll.getBytes());
					cd.setUpdatedOn(new Date());
					dataStore.update(d);
				} else {
					LOG.warn("found existing data for rule set {} but the data was not changeable", ruleSetRestBean.getName());
				}
			} else {
				SimpleData sd = new SimpleData(ll);
				sd.setName(dataName);
				sd.setContentType(ContentType.XML.getName());
				sd.setCreatedOn(new Date());
				d = sd;
				dataStore.create(d);
			}
		} catch (JAXBException ex) {
			throw new IllegalStateException("could not set up jaxb context: " + ex.getMessage(), ex);
		}
	}

	@Override
	public RuleSetRestBean getRuleSet(Class<?> type) {
		return getRuleSet(type.getSimpleName());
	}

	@Override
	public RuleSetRestBean getRuleSet(String typeName) {
		dataStoresLock.readLock().lock();
		try {
			Iterator<DeployedDataStore> iterator = dataStores.iterator();
			while (iterator.hasNext()) {
				DeployedDataStore next = iterator.next();
				DataStore dataStore = next.getDataStore();
				Data d = dataStore.findByNameAndContentType(typeName + "Rules."+ContentType.XML.getExtension(), ContentType.XML.getName());
				if(d != null) {
					RuleSetRestBean set = buildRuleSetFromData(d);
					return set;
				}
			}
			return null;
		} finally {
			dataStoresLock.readLock().unlock();
		}
	}

	private RuleSetRestBean buildRuleSetFromData(Data d) {
		RuleSetRestBean set = null;
		try {
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			if(d != null) {
				ReplayableInputStream is = d.getInputStream();
				if (is != null) {
					try {
						is.replay();
						set = (RuleSetRestBean) unmarshaller.unmarshal(is);
					} catch (IOException ex) {
						LOG.error("failed to replay data input stream while building rule set: "+ex.getMessage(), ex);
					}
				}
			}
		} catch (JAXBException ex) {
			LOG.error("tried to parse rule set data, but jaxb failed: "+ex.getMessage(), ex);
		}
		return set;
	}

	private static String getDataNameForRuleSet(RuleSetRestBean ruleSetRestBean) {
		String dataName = ruleSetRestBean.getName() + "Rules."+ContentType.XML.getExtension();
		return dataName;
	}
	
	private static String getRuleSetNameForData(Data data) {
		String name = data.getName();
		String suffix = "Rules."+ContentType.XML.getExtension();
		if(name.endsWith(suffix)) {
			return name.substring(0, name.length()-suffix.length());
		}
		return null;
	}

	@Override
	public List<RuleSetRestBean> list() {
		List<RuleSetRestBean> r = new ArrayList<>();
		dataStoresLock.readLock().lock();
		try {
			Iterator<DeployedDataStore> iterator = dataStores.iterator();
			while (iterator.hasNext()) {
				DeployedDataStore next = iterator.next();
				DataStore dataStore = next.getDataStore();
				List<Data> d = dataStore.list();
				if (d != null) {
					for (Data data : d) {
						if(isRulesData(data)) {
							RuleSetRestBean rs = buildRuleSetFromData(data);
							if (rs != null) {
								r.add(rs);
							}
						}
					}
				}
			}
		} finally {
			dataStoresLock.readLock().unlock();
		}
		return r;
	}
	
	private boolean isRulesData(Data data) {
		String name = getRuleSetNameForData(data);
		return name != null;
	}

	@Override
	public boolean ruleSetExists(Class<?> type) {
		return ruleSetExists(type.getSimpleName());
	}

	@Override
	public boolean ruleSetExists(String typeName) {
		return _knownRuleSets.contains(typeName);
	}

}
