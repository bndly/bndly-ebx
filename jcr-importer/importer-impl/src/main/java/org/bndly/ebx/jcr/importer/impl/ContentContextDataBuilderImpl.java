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
import org.bndly.ebx.jcr.importer.api.ContentContextData;
import org.bndly.ebx.jcr.importer.api.ContentContextDataBuilder;
import org.bndly.ebx.jcr.importer.api.JobContextWriter;
import org.bndly.ebx.jcr.importer.api.StringContextData;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class ContentContextDataBuilderImpl extends AbstractContextDataBuilder<ContentContextDataBuilder> implements ContentContextDataBuilder {

	private String contentId;
	private String contentPath;
	private String contentTypeName;

	public ContentContextDataBuilderImpl(JobContextWriter writer, JobContextImpl.JobContextEntryImpl entry) {
		super(writer, entry);
	}

	@Override
	public final ContentContextDataBuilder id(String contentId) {
		this.contentId = contentId;
		return this;
	}

	@Override
	public final ContentContextDataBuilder path(String contentPath) {
		this.contentPath = contentPath;
		return this;
	}

	@Override
	public final ContentContextDataBuilder type(String contentTypeName) {
		this.contentTypeName = contentTypeName;
		return this;
	}

	@Override
	public final ContentContextDataBuilder fromJson(JSObject jsObject) {
		String name = jsObject.getMemberStringValue("name");
		if (name == null) {
			return this;
		}
		if (!"content".equals(jsObject.getMemberStringValue("type"))) {
			return this;
		}
		var(name);
		id(jsObject.getMemberStringValue("contentId"));
		path(jsObject.getMemberStringValue("contentPath"));
		type(jsObject.getMemberStringValue("contentType"));
		return this;
	}

	public static JSObject toJson(ContentContextData contextData) {
		String name = contextData.getName();
		if (name == null) {
			return null;
		}
		JSObject jsObject = new JSObject();
		jsObject.createMember("name").setValue(new JSString(name));
		jsObject.createMember("type").setValue(new JSString("content"));
		if (contextData.getContentId() != null) {
			jsObject.createMember("contentId").setValue(new JSString(contextData.getContentId()));
		}
		if (contextData.getContentLocation() != null) {
			jsObject.createMember("contentPath").setValue(new JSString(contextData.getContentLocation()));
		}
		if (contextData.getContentType() != null) {
			jsObject.createMember("contentType").setValue(new JSString(contextData.getContentType()));
		}
		return jsObject;
	}

	@Override
	protected final ContentContextData createDataEntryInstance(final String name) {
		final String id = contentId;
		final String path = contentPath;
		final String type = contentTypeName;
		return new ContentContextData() {

			@Override
			public String getContentType() {
				return type;
			}

			@Override
			public String getContentId() {
				return id;
			}

			@Override
			public String getContentLocation() {
				return path;
			}

			@Override
			public String getName() {
				return name;
			}
		};
	}

}
