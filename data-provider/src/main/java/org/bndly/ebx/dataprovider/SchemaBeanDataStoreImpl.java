package org.bndly.ebx.dataprovider;

/*-
 * #%L
 * org.bndly.ebx.data-provider
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
import org.bndly.common.data.io.ReplayableInputStream;
import org.bndly.common.data.api.SimpleData;
import org.bndly.ebx.model.BinaryData;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.listener.DeleteListener;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.api.listener.SchemaDeploymentListener;
import org.bndly.schema.api.services.Deployer;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Schema;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		service = {BinaryDataToDataConverter.class, DataStore.class, SchemaDeploymentListener.class, PersistListener.class, MergeListener.class, DeleteListener.class},
		immediate = true,
		property = {
			"schema=ebx",
			"schemaTypes=BinaryData",
			"service.pid=org.bndly.common.data.api.DataStore.ebx"
		}
)
public class SchemaBeanDataStoreImpl implements DataStore, SchemaDeploymentListener, PersistListener, MergeListener, DeleteListener, BinaryDataToDataConverter {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private final List<DataStoreListener> listeners = new ArrayList<>();
	private final ReadWriteLock listenersLock = new ReentrantReadWriteLock();
	private final ThreadLocal<Boolean> dataStoreCallLocal = new ThreadLocal<>();

	private Engine getEngine(){
		return schemaBeanFactory.getEngine();
	}
	
	@Override
	public List<Data> list() {
		if (!isSchemaDeployed()) {
			return null;
		}
		List<Data> dataList = new ArrayList<>();
		Iterator<Record> res = schemaBeanFactory.getEngine().getAccessor().query("PICK " + BinaryData.class.getSimpleName());
		while (res.hasNext()) {
			Record next = res.next();
			BinaryData binaryData = schemaBeanFactory.getSchemaBean(BinaryData.class, next);
			dataList.add(convertBinaryDataToData(binaryData));
		}
		return dataList;
	}

	@Override
	public BinarySimpleData findByName(String name) {
		if (!isSchemaDeployed()) {
			return null;
		}
		RecordContext recordContext = getEngine().getAccessor().buildRecordContext();
		Record record = findRecordByName(name, recordContext);
		return mapRecordToData(record);
	}

	@Override
	public BinarySimpleData findByNameAndContentType(String name, String contentType) {
		if (!isSchemaDeployed()) {
			return null;
		}
		RecordContext recordContext = getEngine().getAccessor().buildRecordContext();
		Iterator<Record> res = getEngine().getAccessor().query("PICK " + BinaryData.class.getSimpleName() + " b IF b.name=? AND b.contentType=?  LIMIT ?", recordContext, null, name, contentType, 1);
		if (res.hasNext()) {
			return mapRecordToData(res.next());
		}
		return null;
	}

	@Override
	public Data create(Data data) {
		if (!isSchemaDeployed()) {
			return data;
		}
		dataStoreCallLocal.set(Boolean.TRUE);
		try {
			RecordContext recordContext = getEngine().getAccessor().buildRecordContext();
			BinaryData r = mapDataToRecord(data, recordContext);
			ActiveRecord ar = (ActiveRecord) r;
			if (ar.getId() == null) {
				if (r.getCreatedOn() == null) {
					r.setCreatedOn(new Date());
				}
				((ActiveRecord) r).persist();
				listenersLock.readLock().lock();
				try {
					for (DataStoreListener listener : listeners) {
						listener.dataCreated(this, data);
					}
				} finally {
					listenersLock.readLock().unlock();
				}
			} else {
				Date date = new Date();
				if (r.getCreatedOn() == null) {
					r.setCreatedOn(date);
				}
				r.setUpdatedOn(date);
				((ActiveRecord) r).update();
				listenersLock.readLock().lock();
				try {
					for (DataStoreListener listener : listeners) {
						listener.dataUpdated(this, data);
					}
				} finally {
					listenersLock.readLock().unlock();
				}
			}
			return data;
		} finally {
			dataStoreCallLocal.set(Boolean.FALSE);
		}
	}

	@Override
	public Data update(Data data) {
		if (!isSchemaDeployed()) {
			return data;
		}
		dataStoreCallLocal.set(Boolean.TRUE);
		try {
			RecordContext recordContext = getEngine().getAccessor().buildRecordContext();
			BinaryData r = mapDataToRecord(data, recordContext);
			if (r.getCreatedOn() == null) {
				r.setCreatedOn(new Date());
			}
			if (r.getUpdatedOn() == null) {
				r.setUpdatedOn(new Date());
			}
			((ActiveRecord) r).update();
			listenersLock.readLock().lock();
			try {
				for (DataStoreListener listener : listeners) {
					listener.dataUpdated(this, data);
				}
			} finally {
				listenersLock.readLock().unlock();
			}
			return data;
		} finally {
			dataStoreCallLocal.set(Boolean.FALSE);
		}
	}

	@Override
	public void delete(Data data) {
		if (!isSchemaDeployed()) {
			return;
		}
		dataStoreCallLocal.set(Boolean.TRUE);
		try {
			RecordContext recordContext = getEngine().getAccessor().buildRecordContext();
			Record r = findRecordByName(data.getName(), recordContext);
			if (r != null) {
				Transaction tx = getEngine().getQueryRunner().createTransaction();
				getEngine().getAccessor().delete(r, tx);
				tx.commit();
				listenersLock.readLock().lock();
				try {
					for (DataStoreListener listener : listeners) {
						listener.dataDeleted(this, data);
					}
				} finally {
					listenersLock.readLock().unlock();
				}
			}
		} finally {
			dataStoreCallLocal.set(Boolean.FALSE);
		}
	}

	@Override
	public BinarySimpleData convertBinaryDataToData(final BinaryData binaryData) {
		SimpleData.LazyLoader ll = new SimpleData.LazyLoader() {

			@Override
			public ReplayableInputStream getBytes() {
				try {
					InputStream is = binaryData.getBytes();
					if (is == null) {
						return null;
					}
					return ReplayableInputStream.newInstance(is);
				} catch (IOException ex) {
					throw new IllegalStateException("could not build replayable input stream");
				}
			}
		};
		BinarySimpleData d = new BinarySimpleData(binaryData, ll);
		d.setContentType(binaryData.getContentType());
		d.setCreatedOn(binaryData.getCreatedOn());
		d.setName(binaryData.getName());
		d.setUpdatedOn(binaryData.getUpdatedOn());
		return d;
	}

	private BinarySimpleData mapRecordToData(Record record) {
		if (record == null) {
			return null;
		}
		BinaryData bd = schemaBeanFactory.getSchemaBean(BinaryData.class, record);
		return convertBinaryDataToData(bd);
	}

	private BinaryData mapDataToRecord(Data data, RecordContext recordContext) {
		if (BinarySimpleData.class.isInstance(data)) {
			return ((BinarySimpleData) data).getBinaryData();
		}
		boolean reusingExistingRecord;
		Record r = null;
		if (data.getName() != null) {
			r = findRecordByName(data.getName(), recordContext);
		}
		if (r == null) {
			r = recordContext.create(BinaryData.class.getSimpleName());
			reusingExistingRecord = false;
		} else {
			reusingExistingRecord = true;
		}
		BinaryData bd = schemaBeanFactory.getSchemaBean(BinaryData.class, r);
		if (data.getInputStream() != null) {
			bd.setBytes(data.getInputStream());
		}
		bd.setContentType(data.getContentType());
		bd.setName(data.getName());
		if (!reusingExistingRecord) {
			bd.setCreatedOn(data.getCreatedOn());
			bd.setUpdatedOn(data.getUpdatedOn());
		}
		return bd;
	}

	private Record findRecordByName(String name, RecordContext recordContext) {
		Iterator<Record> res = getEngine().getAccessor().query("PICK " + BinaryData.class.getSimpleName() + " b IF b.name=? LIMIT ?", recordContext, null, name, 1);
		if (res.hasNext()) {
			return res.next();
		}
		return null;
	}

	private boolean isSchemaDeployed() {
		boolean result = false;
		if (getEngine() != null) {
			if (getEngine().getDeployer() != null) {
				Schema s = getEngine().getDeployer().getDeployedSchema();
				result = (s != null);
			}
		}
		return result;
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setSchemaDeploymentListeners(List l) {
		l.add(this);
	}

	@Override
	public void addListener(DataStoreListener listener) {
		if (listener != null) {
			listenersLock.writeLock().lock();
			try {
				listeners.add(listener);
			} finally {
				listenersLock.writeLock().unlock();
			}
		}
	}

	@Override
	public void schemaDeployed(Schema deployedSchema, Engine engine) {
		if ("ebx".equals(deployedSchema.getName())) {
			listenersLock.readLock().lock();
			try {
				for (DataStoreListener dataStoreListener : listeners) {
					dataStoreListener.dataStoreIsReady(this);
				}
			} finally {
				listenersLock.readLock().unlock();
			}
		}
	}

	@Override
	public void schemaUndeployed(Schema deployedSchema, Engine engine) {
		if ("ebx".equals(deployedSchema.getName())) {
			listenersLock.readLock().lock();
			try {
				for (DataStoreListener dataStoreListener : listeners) {
					dataStoreListener.dataStoreClosed(this);
				}
			} finally {
				listenersLock.readLock().unlock();
			}
		}
	}

	@Override
	public String getName() {
		return "ebx";
	}

	private boolean isInDataStoreCall() {
		Boolean r = dataStoreCallLocal.get();
		return r == null ? false : r;
	}
	
	@Override
	public void onMerge(Record record) {
		if (isInDataStoreCall()) {
			return;
		}
		// convert the record to data
		BinarySimpleData data = mapRecordToData(record);
		listenersLock.readLock().lock();
		try {
			for (DataStoreListener listener : listeners) {
				listener.dataUpdated(this, data);
			}
		} finally {
			listenersLock.readLock().unlock();
		}
	}

	@Override
	public void onPersist(Record record) {
		if (isInDataStoreCall()) {
			return;
		}
		// convert the record to data
		BinarySimpleData data = mapRecordToData(record);
		listenersLock.readLock().lock();
		try {
			for (DataStoreListener listener : listeners) {
				listener.dataCreated(this, data);
			}
		} finally {
			listenersLock.readLock().unlock();
		}
	}

	@Override
	public void onDelete(Record record) {
		if (isInDataStoreCall()) {
			return;
		}
		// convert the record to data
		BinarySimpleData data = mapRecordToData(record);
		listenersLock.readLock().lock();
		try {
			for (DataStoreListener listener : listeners) {
				listener.dataDeleted(this, data);
			}
		} finally {
			listenersLock.readLock().unlock();
		}
	}
	


	@Override
	public void removeListener(DataStoreListener listener) {
		if (listener != null) {
			listenersLock.writeLock().lock();
			try {
				listeners.remove(listener);
			} finally {
				listenersLock.writeLock().unlock();
			}
		}
	}

	@Override
	public boolean isReady() {
		if (this.schemaBeanFactory == null) {
			return false;
		}
		Engine engine = this.schemaBeanFactory.getEngine();
		if (engine == null) {
			return false;
		}
		Deployer deployer = engine.getDeployer();
		if (deployer == null) {
			return false;
		}
		Schema ds = deployer.getDeployedSchema();
		if (ds == null) {
			return false;
		}
		return true;
	}

}
