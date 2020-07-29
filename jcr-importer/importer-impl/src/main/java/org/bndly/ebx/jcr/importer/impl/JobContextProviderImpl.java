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

import org.bndly.common.data.io.SmartBufferOutputStream;
import org.bndly.common.json.model.JSValue;
import org.bndly.common.json.parsing.JSONParser;
import org.bndly.ebx.jcr.importer.api.JobContextProvider;
import org.bndly.ebx.model.JobContextMap;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
@Component(service = JobContextProvider.class, immediate = true)
public class JobContextProviderImpl implements JobContextProvider {

	private static final Logger LOG = LoggerFactory.getLogger(JobContextProviderImpl.class);

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private JobContextImpl jobContextImpl;

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	@Activate
	public void activate() {
		// restore the job context from persistence
		writeLock.lock();
		try {
			Accessor accessor = schemaBeanFactory.getEngine().getAccessor();
			Iterator<Record> res = accessor.query("PICK JobContextMap j ORDERBY j.createdOn DESC LIMIT ?", 1);
			if(res.hasNext()) {
				// load old
				LOG.info("found at least one job context");
				final JobContextMap jobContextMap = schemaBeanFactory.getSchemaBean(JobContextMap.class, res.next());
				InputStream mapDataInputStream = jobContextMap.getMapData();
				if(mapDataInputStream == null) {
					LOG.info("found job context, but it did not contain any data. creating new job context.");
					jobContextImpl = new JobContextImpl(schemaBeanFactory, jobContextMap);
				} else {
					// parse the inputStreamData
					JSValue parsed = new JSONParser().parse(mapDataInputStream, "UTF-8");
					jobContextImpl = JobContextImpl.createFrom(schemaBeanFactory, parsed, jobContextMap);
					if(jobContextImpl == null) {
						LOG.info("found job context, but the data could not be loaded. creating new job context.");
						final JobContextMap newFixedJobContextMap = createAndPersistFreshJobContextMap();
						jobContextImpl = new JobContextImpl(schemaBeanFactory, newFixedJobContextMap);
					}
				}
			} else {
				LOG.info("no job context found. creating new one.");
				// create new
				final JobContextMap jobContextMap = createAndPersistFreshJobContextMap();
				jobContextImpl = new JobContextImpl(schemaBeanFactory, jobContextMap);
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Deactivate
	public void deactivate() {
		// flush the job context to persistence
		writeLock.lock();
		try {
			try {
				if(jobContextImpl != null) {
					save(jobContextImpl);
				}
			} finally {
				jobContextImpl = null;
			}
		} finally {
			writeLock.unlock();
		}
	}

	public void save(JobContextImpl jobContextImpl) {
		LOG.info("writing job context to buffer now");
		try(SmartBufferOutputStream buffer = SmartBufferOutputStream.newInstance()){
			jobContextImpl.writeTo(buffer, "UTF-8");
			buffer.flush();
			JobContextMap jobContextMap = jobContextImpl.getJobContextMap();
			jobContextMap.setMapData(buffer.getBufferedDataAsStream());
			ActiveRecord ar = ((ActiveRecord)jobContextMap);
			if(ar.getId() == null) {
				ar.persist();
			} else {
				ar.update();
			}
		} catch(IOException e) {
			LOG.error("failed to write job context to buffer: "+e.getMessage(), e);
		}
	}

	@Override
	public void newJobContext() {
		writeLock.lock();
		try {
			if(jobContextImpl != null) {
				save(jobContextImpl);
			}
		} finally {
			JobContextMap jobContextMap = createAndPersistFreshJobContextMap();
			jobContextImpl = new JobContextImpl(schemaBeanFactory, jobContextMap);
			writeLock.unlock();
		}
	}

	@Override
	public void newJobContext(VoidCallback newContextCallback, VoidCallback oldContextCallback) {
		JobContextImpl oldContext;
		writeLock.lock();
		try {
			oldContext = jobContextImpl;
			JobContextMap jobContextMap = createAndPersistFreshJobContextMap();
			jobContextImpl = new JobContextImpl(schemaBeanFactory, jobContextMap);
			if(newContextCallback != null) {
				newContextCallback.doWithContext(jobContextImpl);
			}
		} finally {
			writeLock.unlock();
		}
		if(oldContext != null) {
			if(oldContextCallback != null) {
				oldContextCallback.doWithContext(oldContext);
			}
			save(oldContext);
		}
	}

	@Override
	public void inMemoryContext(VoidCallback callback) {
		callback.doWithContext(new JobContextImpl(schemaBeanFactory, createAndPersistFreshJobContextMap()));
	}

	private JobContextMap createAndPersistFreshJobContextMap() {
		final JobContextMap jobContextMap = schemaBeanFactory.getSchemaBean(JobContextMap.class, schemaBeanFactory.getEngine().getAccessor().buildRecordContext().create(JobContextMap.class.getSimpleName()));
		jobContextMap.setCreatedOn(new Date());
		((ActiveRecord)jobContextMap).persist();
		return jobContextMap;
	}

	@Override
	public <E> E write(Callback<E> callback) {
		if(callback == null) {
			return null;
		}
		writeLock.lock();
		try {
			return callback.doWithContext(jobContextImpl);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public <E> E read(Callback<E> callback) {
		if(callback == null) {
			return null;
		}
		readLock.lock();
		try {
			return callback.doWithContext(jobContextImpl);
		} finally {
			readLock.unlock();
		}
	}

}
