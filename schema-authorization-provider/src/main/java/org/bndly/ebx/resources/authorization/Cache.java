package org.bndly.ebx.resources.authorization;

/*-
 * #%L
 * org.bndly.ebx.schema-authorization-provider
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public abstract class Cache<K,E> {
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<K, E> items = new HashMap<>();

	protected abstract K buildKey(E item);

	protected abstract void afterReplacement(E oldItem, E newItem);
	
	protected abstract void afterRemoval(E oldItem);

	public final void put(E item) {
		final K key = buildKey(item);
		lock.writeLock().lock();
		try {
			E oldItem = items.get(key);
			items.put(key, item);
			if (oldItem != null) {
				if (oldItem != item) {
					afterReplacement(oldItem, item);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public final boolean isEmpty() {
		lock.readLock().lock();
		try {
			return items.isEmpty();
		} finally {
			lock.readLock().unlock();
		}
	}

	public final E get(K key) {
		lock.readLock().lock();
		try {
			return items.get(key);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public final void dropByKey(K key) {
		lock.writeLock().lock();
		try {
			E oldItem = items.get(key);
			if (oldItem != null) {
				items.remove(key);
				afterRemoval(oldItem);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	public final void dropByEntry(E entry) {
		K key = buildKey(entry);
		lock.writeLock().lock();
		try {
			E oldItem = items.get(key);
			if (oldItem != null) {
				if (entry == oldItem) {
					items.remove(key);
					afterRemoval(oldItem);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public final void clear() {
		lock.writeLock().lock();
		try {
			items.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}
}
