package org.bndly.ebx.cors;

/*-
 * #%L
 * org.bndly.ebx.cors-filter
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

import org.bndly.common.cors.api.Origin;
import org.bndly.common.cors.api.OriginService;
import org.bndly.ebx.model.CORSOrigin;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.listener.DeleteListener;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = {OriginService.class, PersistListener.class, MergeListener.class, DeleteListener.class}, immediate = true)
public class OriginServiceImpl implements OriginService, PersistListener, MergeListener, DeleteListener {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private final Map<Long, CORSOrigin> schemaStoredCorsOriginsById = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	@Override
	public void onPersist(Record record) {
		if (isCORSOriginRecord(record)) {
			lock.writeLock().lock();
			try {
				schemaStoredCorsOriginsById.put(record.getId(), schemaBeanFactory.getSchemaBean(CORSOrigin.class, record));
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	@Override
	public void onMerge(Record record) {
		if (isCORSOriginRecord(record)) {
			lock.writeLock().lock();
			try {
				CORSOrigin r = schemaStoredCorsOriginsById.get(record.getId());
				((ActiveRecord) r).reload();
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	@Override
	public void onDelete(Record record) {
		if (isCORSOriginRecord(record)) {
			lock.writeLock().lock();
			try {
				schemaStoredCorsOriginsById.remove(record.getId());
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	protected boolean isCORSOriginRecord(Record record) {
		Class<?> t = schemaBeanFactory.getTypeBindingForType(record);
		if (t == null) {
			return false;
		}
		return CORSOrigin.class.equals(t);
	}

	private static class OriginImpl implements Origin {

		private final CORSOrigin corsOrigin;

		public OriginImpl(CORSOrigin corsOrigin) {
			this.corsOrigin = corsOrigin;
		}

		@Override
		public void setProtocol(String protocol) {
			corsOrigin.setProtocol(protocol);
		}

		@Override
		public void setDomainName(String domainName) {
			corsOrigin.setDomainName(domainName);
		}

		@Override
		public void setPort(int port) {
			corsOrigin.setPort(new Long(port));
		}

		public CORSOrigin getCorsOrigin() {
			return corsOrigin;
		}

	}

	@Override
	public Origin newInstance() {
		Class<CORSOrigin> type = CORSOrigin.class;
		RecordContext ctx = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		CORSOrigin or = schemaBeanFactory.getSchemaBean(type, ctx.create(type.getSimpleName()));
		OriginImpl o = new OriginImpl(or);
		o.setPort(80);
		return o;
	}

	@Activate
	public void activate() {
		lock.writeLock().lock();
		try {
			Iterator<Record> origins = schemaBeanFactory.getEngine().getAccessor().query("PICK CORSOrigin");
			while (origins.hasNext()) {
				Record origin = origins.next();
				schemaStoredCorsOriginsById.put(origin.getId(), schemaBeanFactory.getSchemaBean(CORSOrigin.class, origin));
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean isAcceptedOrigin(Origin origin) {
		// we probably might want to store a list of allowed origins somewhere
		if (!OriginImpl.class.isInstance(origin)) {
			return false;
		}
		lock.readLock().lock();
		try {
			OriginImpl impl = (OriginImpl) origin;
			CORSOrigin o = impl.getCorsOrigin();
			Collection<CORSOrigin> values = schemaStoredCorsOriginsById.values();
			for (CORSOrigin corsOrigin : values) {
				if (
						equalsAndNotNull(o.getDomainName(), corsOrigin.getDomainName())
						&& equalsAndNotNull(o.getPort(), corsOrigin.getPort())
						&& equalsAndNotNull(o.getProtocol(), corsOrigin.getProtocol())
					) {
					return true;
				}
			}
			return false;
		} finally {
			lock.readLock().unlock();
		}
	}

	private boolean equalsAndNotNull(Object a, Object b) {
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
