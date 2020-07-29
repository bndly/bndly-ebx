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
import org.bndly.ebx.jcr.importer.api.JobContextWriter;
import org.bndly.ebx.jcr.importer.api.StringContextData;
import org.bndly.ebx.jcr.importer.api.StringContextDataBuilder;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class StringContextDataBuilderImpl extends AbstractContextDataBuilder<StringContextDataBuilder> implements StringContextDataBuilder {

	private String stringValue;

	public StringContextDataBuilderImpl(JobContextWriter writer, JobContextImpl.JobContextEntryImpl entry) {
		super(writer, entry);
	}

	@Override
	public final StringContextDataBuilder stringValue(String stringValue) {
		this.stringValue = stringValue;
		return this;
	}

	@Override
	public final StringContextDataBuilder fromJson(JSObject jsObject) {
		String name = jsObject.getMemberStringValue("name");
		if(name == null) {
			return this;
		}
		if(!"string".equals(jsObject.getMemberStringValue("type"))) {
			return this;
		}
		var(name);
		stringValue(jsObject.getMemberStringValue("stringValue"));
		return this;
	}

	public static JSObject toJson(StringContextData contextData) {
		String name = contextData.getName();
		if (name == null) {
			return null;
		}
		JSObject jsObject = new JSObject();
		jsObject.createMember("name").setValue(new JSString(name));
		jsObject.createMember("type").setValue(new JSString("string"));
		if (contextData.getStringValue() != null) {
			jsObject.createMember("stringValue").setValue(new JSString(contextData.getStringValue()));
		}
		return jsObject;
	}

	@Override
	protected final StringContextData createDataEntryInstance(final String name) {
		final String finalStringValue = this.stringValue;
		return new StringContextData() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getStringValue() {
				return finalStringValue;
			}
		};
	}

}
