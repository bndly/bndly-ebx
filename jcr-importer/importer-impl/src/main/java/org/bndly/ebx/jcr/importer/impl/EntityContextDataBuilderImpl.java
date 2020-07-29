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

import org.bndly.common.json.model.JSObject;
import org.bndly.common.json.model.JSString;
import org.bndly.ebx.jcr.importer.api.EntityContextData;
import org.bndly.ebx.jcr.importer.api.EntityContextDataBuilder;
import org.bndly.ebx.jcr.importer.api.JobContextWriter;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.SchemaBeanFactory;

import java.lang.reflect.Proxy;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class EntityContextDataBuilderImpl extends AbstractContextDataBuilder<EntityContextDataBuilder> implements EntityContextDataBuilder {

	private final SchemaBeanFactory schemaBeanFactory;
	private String typeName;
	private String idProperty;
	private String idValue;

	public EntityContextDataBuilderImpl(SchemaBeanFactory schemaBeanFactory, JobContextWriter writer, JobContextImpl.JobContextEntryImpl entry) {
		super(writer, entry);
		if(schemaBeanFactory == null) {
			throw new IllegalArgumentException("schemaBeanFactory is not allowed to be null");
		}
		this.schemaBeanFactory = schemaBeanFactory;
	}


	@Override
	public final EntityContextDataBuilder schemaBean(Object schemaBean) {
		Record record = schemaBeanFactory.getRecordFromSchemaBean(schemaBean);
		if(record != null) {
			Long id = record.getId();
			if(id != null) {
				String idAsString = id.toString();
				type(record.getType().getName()).idProperty("id").idValue(idAsString);
			}
		}
		return this;
	}

	@Override
	public final EntityContextDataBuilder type(String typeName) {
		this.typeName = typeName;
		return this;
	}

	@Override
	public final EntityContextDataBuilder idProperty(String idProperty) {
		this.idProperty = idProperty;
		return this;
	}

	@Override
	public final EntityContextDataBuilder idValue(String idValue) {
		this.idValue = idValue;
		return this;
	}

	@Override
	public final EntityContextDataBuilder fromJson(JSObject jsObject) {
		String name = jsObject.getMemberStringValue("name");
		if(name == null) {
			return this;
		}
		if(!"entity".equals(jsObject.getMemberStringValue("type"))) {
			return this;
		}
		var(name);
		idValue(jsObject.getMemberStringValue("entityIdValue"));
		idProperty(jsObject.getMemberStringValue("entityIdProperty"));
		type(jsObject.getMemberStringValue("entityType"));
		return this;
	}

	public static JSObject toJson(EntityContextData contextData) {
		String name = contextData.getName();
		if (name == null) {
			return null;
		}
		JSObject jsObject = new JSObject();
		jsObject.createMember("name").setValue(new JSString(name));
		jsObject.createMember("type").setValue(new JSString("entity"));
		if (contextData.getType() != null) {
			jsObject.createMember("entityType").setValue(new JSString(contextData.getType()));
		}
		if (contextData.getIdProperty() != null) {
			jsObject.createMember("entityIdProperty").setValue(new JSString(contextData.getIdProperty()));
		}
		if (contextData.getIdValue() != null) {
			jsObject.createMember("entityIdValue").setValue(new JSString(contextData.getIdValue()));
		}
		return jsObject;
	}

	@Override
	protected EntityContextData createDataEntryInstance(final String name) {
		final String finalType = typeName;
		final String finalIdProperty = idProperty;
		final String finalIdValue = idValue;

		return new EntityContextData() {

			@Override
			public String getType() {
				return finalType;
			}

			@Override
			public String getIdProperty() {
				return finalIdProperty;
			}

			@Override
			public String getIdValue() {
				return finalIdValue;
			}

			@Override
			public String getName() {
				return name;
			}
		};
	}

}
