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

import org.bndly.ebx.jcr.importer.api.ContentSynchronizationStrategy;
import org.bndly.ebx.jcr.importer.api.JobBuilder;
import org.bndly.schema.api.Record;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class ContentSynchronizationStrategyTracker extends ServiceTracker<ContentSynchronizationStrategy, ContentSynchronizationStrategy> {

	private final Map<Class, List<ContentSynchronizationStrategy>> synchronizedTypes = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public ContentSynchronizationStrategyTracker(BundleContext bundleContext) {
		super(bundleContext, ContentSynchronizationStrategy.class, null);
	}

	@Override
	public ContentSynchronizationStrategy addingService(ServiceReference<ContentSynchronizationStrategy> reference) {
		ContentSynchronizationStrategy strategy = super.addingService(reference);
		addStrategy(strategy);
		return strategy;
	}

	@Override
	public void removedService(ServiceReference<ContentSynchronizationStrategy> reference, ContentSynchronizationStrategy service) {
		removeStrategy(service);
		super.removedService(reference, service);
	}
	
	public void addStrategy(ContentSynchronizationStrategy strategy) {
		if (strategy != null) {
			lock.writeLock().lock();
			try {
				List<ContentSynchronizationStrategy> l = synchronizedTypes.get(strategy.getEntityType());
				if (l == null) {
					l = new ArrayList<>();
					synchronizedTypes.put(strategy.getEntityType(), l);
				}
				l.add(strategy);
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	public void removeStrategy(ContentSynchronizationStrategy strategy) {
		if (strategy != null) {
			lock.writeLock().lock();
			try {
				List<ContentSynchronizationStrategy> l = synchronizedTypes.get(strategy.getEntityType());
				if (l != null) {
					Iterator<ContentSynchronizationStrategy> iterator = l.iterator();
					while (iterator.hasNext()) {
						if (iterator.next() == strategy) {
							iterator.remove();
						}
					}
					if (l.isEmpty()) {
						synchronizedTypes.remove(strategy.getEntityType());
					}
				}
			} finally {
				lock.writeLock().unlock();
			}
		}
	}
	
	boolean isCMSSynchronized(Record record) {
		lock.readLock().lock();
		try {
			for (Map.Entry<Class, List<ContentSynchronizationStrategy>> entrySet : synchronizedTypes.entrySet()) {
				Class synchronizedType = entrySet.getKey();
				if (record.getType().getName().equals(synchronizedType.getSimpleName())) {
					List<ContentSynchronizationStrategy> l = entrySet.getValue();
					return l != null && !l.isEmpty();
				}
			}
			return false;
		} finally {
			lock.readLock().unlock();
		}
	}

	ContentSynchronizationStrategy getStrategyForEntity(Record entity) {
		lock.readLock().lock();
		try {
			for (Map.Entry<Class, List<ContentSynchronizationStrategy>> entrySet : synchronizedTypes.entrySet()) {
				final Class synchronizedType = entrySet.getKey();
				if (entity.getType().getName().equals(synchronizedType.getSimpleName())) {
					final List<ContentSynchronizationStrategy> strategies = entrySet.getValue();
					return new ContentSynchronizationStrategy() {
						@Override
						public Class getEntityType() {
							return synchronizedType;
						}

						@Override
						public void created(Object entity, JobBuilder builder) {
							for (ContentSynchronizationStrategy strategy : strategies) {
								strategy.created(entity, builder);
							}
						}

						@Override
						public void updated(Object entity, JobBuilder builder) {
							for (ContentSynchronizationStrategy strategy : strategies) {
								strategy.updated(entity, builder);
							}
						}

						@Override
						public void deleted(Object entity, JobBuilder builder) {
							for (ContentSynchronizationStrategy strategy : strategies) {
								strategy.deleted(entity, builder);
							}
						}
					};
				}
			}
			throw new IllegalStateException("could not find ContentSynchronizationStrategy for " + entity.getClass().getSimpleName());
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private class Dependency {

		private final Class<?> schemaBeanType;
		private final List<Dependency> dependsOn = new ArrayList<>();

		public Dependency(Class<?> schemaBeanType) {
			this.schemaBeanType = schemaBeanType;
		}

		public List<Dependency> getDependsOn() {
			return dependsOn;
		}

		public Class<?> getSchemaBeanType() {
			return schemaBeanType;
		}

	}

	private void defineDependencies(Dependency value, Map<Class, Dependency> depdencyMap, Class<?> schemaBeanType) {
		Method[] methods = schemaBeanType.getMethods();
		for (Method method : methods) {
			Class<?>[] params = method.getParameterTypes();

			if (method.getName().startsWith("get") && (params == null || params.length == 0)) {
				Class<?> rt = method.getReturnType();
				Dependency dep = depdencyMap.get(rt);
				if (dep != null) {
					value.getDependsOn().add(dep);
				}
			}
		}
		Class<?>[] interfaces = schemaBeanType.getInterfaces();
		if (interfaces != null) {
			for (Class<?> aInterface : interfaces) {
				defineDependencies(value, depdencyMap, aInterface);
			}
		}
	}

	private void queueDependency(Dependency dependency, List<Class<?>> r) {
		if (r.contains(dependency.getSchemaBeanType())) {
			return;
		}
		for (Dependency dependsOn : dependency.getDependsOn()) {
			queueDependency(dependsOn, r);
		}
		r.add(dependency.getSchemaBeanType());
	}

	List<Class<?>> listSchemaBeanTypesWithSynchronizationStrategy() {
		lock.readLock().lock();
		try {
			Set<Class> keySet = synchronizedTypes.keySet();
			if (keySet != null && !keySet.isEmpty()) {
				Map<Class, Dependency> depdencyMap = new HashMap<>();
				for (Class key : keySet) {
					depdencyMap.put(key, new Dependency(key));
				}
				for (Dependency value : depdencyMap.values()) {
					defineDependencies(value, depdencyMap, value.getSchemaBeanType());
				}
				List<Class<?>> r = new ArrayList<>();
				for (Dependency dependency : depdencyMap.values()) {
					queueDependency(dependency, r);
				}
				return Collections.unmodifiableList(r);
			} else {
				return Collections.EMPTY_LIST;
			}
		} finally {
			lock.readLock().unlock();
		}
	}
}
