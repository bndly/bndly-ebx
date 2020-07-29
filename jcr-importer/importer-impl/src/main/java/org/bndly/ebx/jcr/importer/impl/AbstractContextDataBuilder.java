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
import org.bndly.ebx.jcr.importer.api.ContextDataBuilder;
import org.bndly.ebx.jcr.importer.api.JobContextData;
import org.bndly.ebx.jcr.importer.api.JobContextWriter;
import org.bndly.ebx.jcr.importer.impl.JobContextImpl.JobContextEntryImpl;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public abstract class AbstractContextDataBuilder<E extends ContextDataBuilder> implements ContextDataBuilder<E> {
	private final JobContextWriter writer;
	private final JobContextEntryImpl entry;
	private String varName;

	public AbstractContextDataBuilder(JobContextWriter writer, JobContextEntryImpl entry) {
		if(writer == null) {
			throw new IllegalArgumentException("writer is not allowed to be null");
		}
		this.writer = writer;
		if(entry == null) {
			throw new IllegalArgumentException("entry is not allowed to be null");
		}
		this.entry = entry;
	}

	@Override
	public final E var(String varName) {
		this.varName = varName;
		return (E) this;
	}

	public abstract E fromJson(JSObject jsObject);

	@Override
	public final JobContextWriter build() {
		if(varName == null) {
			return writer;
		}
		// set the value in the entry
		JobContextData dataEntry = createDataEntryInstance(varName);
		if(dataEntry == null) {
			return writer;
		}
		entry.addEntryData(dataEntry);
		return writer;
	}

	protected abstract JobContextData createDataEntryInstance(String name);

}
