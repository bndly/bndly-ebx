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

import org.bndly.common.json.model.JSArray;
import org.bndly.common.json.model.JSMember;
import org.bndly.common.json.model.JSObject;
import org.bndly.common.json.model.JSString;
import org.bndly.common.json.model.JSValue;
import org.bndly.common.json.serializing.JSONSerializer;
import org.bndly.ebx.jcr.importer.api.ContentContextData;
import org.bndly.ebx.jcr.importer.api.EntityContextData;
import org.bndly.ebx.jcr.importer.api.JobContext;
import org.bndly.ebx.jcr.importer.api.JobContextEntry;
import org.bndly.ebx.model.ImporterJob;
import org.bndly.ebx.jcr.importer.api.JobContextData;
import org.bndly.ebx.jcr.importer.api.StringContextData;
import org.bndly.ebx.model.JobContextMap;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class JobContextImpl implements JobContext {

	static JobContextImpl createFrom(SchemaBeanFactory schemaBeanFactory, JSValue parsed, JobContextMap jobContextMap) {
		if(!JSObject.class.isInstance(parsed)) {
			return null;
		}
		JSObject doc = (JSObject) parsed;
		Map<String, JSObject> entryMap = new LinkedHashMap<>();
		Set<JSMember> members = doc.getMembers();
		if(members != null) {
			for (JSMember member : members) {
				JSString name = member.getName();
				if(name != null) {
					String nameString = name.getValue();
					if(nameString != null) {
						JSValue value = member.getValue();
						if(JSObject.class.isInstance(value)) {
							entryMap.put(nameString, (JSObject) value);
						}
					}
				}
			}
		}
		JobContextImpl jobContextImpl = new JobContextImpl(schemaBeanFactory, jobContextMap);
		for (Map.Entry<String, JSObject> entrySet : entryMap.entrySet()) {
			String key = entrySet.getKey();
			JSObject value = entrySet.getValue();
			JobContextEntryImpl entry = jobContextImpl.getEntry(key);
			if(entry == null) {
				String parentKey = value.getMemberStringValue("parent");
				entry = jobContextImpl.create(key, parentKey);
			}
			final PrivateJobContextWriter localJobContextWriter = entry.getLocalJobContextWriter();
			JSArray dataArray = value.getMemberValue("data", JSArray.class);
			if(dataArray != null) {
				for (JSValue dataItem : dataArray) {
					if(JSObject.class.isInstance(dataItem)) {
						localJobContextWriter.fromJson(value);
					}
				}
			}
		}
		return jobContextImpl;
	}

	public class JobContextEntryImpl implements JobContextEntry {
		private final String key;
		private final String parentKey;
		private PrivateJobContextWriter localWriter;
		private List<JobContextData> entryData;

		public JobContextEntryImpl(String key, String parentKey) {
			if(key == null) {
				throw new IllegalArgumentException("key is not allowed to be null");
			}
			this.key = key;
			this.parentKey = parentKey;
		}

		public void addEntryData(JobContextData dataItem) {
			if(dataItem == null) {
				return;
			}
			if(entryData == null) {
				entryData = new ArrayList<>();
			}
			entryData.add(dataItem);
		}

		@Override
		public List<JobContextData> getEntryData() {
			return entryData;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public JobContextEntryImpl getParent() {
			if(parentKey == null) {
				return null;
			}
			return data.get(parentKey);
		};

		@Override
		public PrivateJobContextWriter getLocalJobContextWriter() {
			if(localWriter == null) {
				localWriter = createJobContextWriter(this);
			}
			return localWriter;
		}

		@Override
		public PrivateJobContextWriter getRootJobContextWriter() {
			JobContextEntryImpl current = this;
			JobContextEntryImpl next = current;
			while(next != null) {
				current = next;
				next = current.getParent();
			}
			return current.getLocalJobContextWriter();
		}

	}

	private final Map<String, JobContextEntryImpl> data = new LinkedHashMap<>();
	private final SchemaBeanFactory schemaBeanFactory;
	private final JobContextMap jobContextMap;

	public JobContextImpl(SchemaBeanFactory schemaBeanFactory, JobContextMap jobContextMap) {
		if(schemaBeanFactory == null) {
			throw new IllegalArgumentException("schemaBeanFactory is not allowed to be null");
		}
		this.schemaBeanFactory = schemaBeanFactory;
		if(jobContextMap == null) {
			throw new IllegalArgumentException("jobContextMap is not allowed to be null");
		}
		this.jobContextMap = jobContextMap;
	}

	public final JobContextMap getJobContextMap() {
		return jobContextMap;
	}

	@Override
	public JobContextEntryImpl create() {
		return create(null);
	}

	@Override
	public JobContextEntryImpl create(String parentContextKey) {
		return create(UUID.randomUUID().toString(), parentContextKey);
	}

	public JobContextEntryImpl create(final String key, String parentContextKey) {
		if(key == null) {
			throw new IllegalArgumentException("key is not allowed to be null");
		}
		JobContextEntryImpl jobContextEntry = new JobContextEntryImpl(key, parentContextKey);
		data.put(key, jobContextEntry);
		return jobContextEntry;
	}

	@Override
	public JobContextEntry get(String key) {
		return getEntry(key);
	}

	public JobContextEntryImpl getEntry(ImporterJob job) {
		if(job == null) {
			return null;
		}
		return getEntry(job.getContextKey());
	}

	public JobContextEntryImpl getEntry(String contextKey) {
		if(contextKey == null) {
			return null;
		}
		return data.get(contextKey);
	}

	public void writeTo(OutputStream outputStream, String encoding) throws IOException {
		// in order to keep the memory footprint low, i will not create the JSON object tree in advance.
		// instead i will create subtrees, that will be individually rendered to the writer.
		Writer writer = new OutputStreamWriter(outputStream, encoding);
		JSONSerializer jsonSerializer = new JSONSerializer();
		writer.write("{");
		writer.flush();
		boolean first = true;
		for (Map.Entry<String, JobContextEntryImpl> entrySet : data.entrySet()) {
			if(!first) {
				writer.write(",");
				writer.flush();
			}
			String key = entrySet.getKey();
			jsonSerializer.serialize(new JSString(key), outputStream, encoding, false);

			writer.write(":");
			writer.flush();

			JobContextEntryImpl value = entrySet.getValue();
			JSObject entryObject = new JSObject();
			JobContextEntryImpl parent = value.getParent();
			if(parent != null) {
				entryObject.createMember("parent").setValue(new JSString(parent.getKey()));
			}
			List<JobContextData> entryData = value.getEntryData();
			if(entryData != null && !entryData.isEmpty()) {
				JSArray jsArray = new JSArray();
				entryObject.createMember("data").setValue(jsArray);
				for (JobContextData entryDataItem : entryData) {
					if(EntityContextData.class.isInstance(entryDataItem)) {
						JSObject toJson = EntityContextDataBuilderImpl.toJson((EntityContextData) entryDataItem);
						if(toJson != null) {
							jsArray.add(toJson);
						}
					} else if(ContentContextData.class.isInstance(entryDataItem)) {
						JSObject toJson = ContentContextDataBuilderImpl.toJson((ContentContextData) entryDataItem);
						if(toJson != null) {
							jsArray.add(toJson);
						}
					} else if(StringContextData.class.isInstance(entryDataItem)) {
						JSObject toJson = StringContextDataBuilderImpl.toJson((StringContextData) entryDataItem);
						if(toJson != null) {
							jsArray.add(toJson);
						}
					}
				}
			}
			jsonSerializer.serialize(entryObject, outputStream, encoding, false);

			first = false;
		}
		writer.write("}");
		writer.flush();
	}

	private PrivateJobContextWriter createJobContextWriter(final JobContextEntryImpl jobContextEntry) {
		return new PrivateJobContextWriter() {

			@Override
			public ContentContextDataBuilderImpl content() {
				return new ContentContextDataBuilderImpl(this, jobContextEntry);
			}

			@Override
			public EntityContextDataBuilderImpl entity() {
				return new EntityContextDataBuilderImpl(schemaBeanFactory, this, jobContextEntry);
			}

			@Override
			public StringContextDataBuilderImpl string() {
				return new StringContextDataBuilderImpl(this, jobContextEntry);
			}

			@Override
			public PrivateJobContextWriter fromJson(JSObject jsObject) {
				// try them all
				content().fromJson(jsObject).build();
				entity().fromJson(jsObject).build();
				string().fromJson(jsObject).build();
				return this;
			}

		};
	}

}

