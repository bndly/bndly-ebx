package org.bndly.business.executor;

/*-
 * #%L
 * org.bndly.ebx.bpm-tasks
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

import org.bndly.common.bpm.api.ContextResolver;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.json.beans.JSONSchemaBeanFactory;

public abstract class AbstractSchemaRelatedTaskExecutor implements ContextResolverDependent {

    protected Engine engine;
    protected SchemaBeanFactory schemaBeanFactory;
    protected JSONSchemaBeanFactory jsonSchemaBeanFactory;
	protected ContextResolver contextResolver;

	protected final <T> T createBeanInContext(Class<T> type, RecordContext recordContext) {
		Record record = recordContext.create(type.getSimpleName());
		T schemaBean = schemaBeanFactory.getSchemaBean(type, record);
		return schemaBean;
	}
	
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
        this.schemaBeanFactory = schemaBeanFactory;
    }

    public void setJsonSchemaBeanFactory(JSONSchemaBeanFactory jsonSchemaBeanFactory) {
        this.jsonSchemaBeanFactory = jsonSchemaBeanFactory;
    }

	@Override
	public void setContextResolver(ContextResolver contextResolver) {
		this.contextResolver = contextResolver;
	}
	
	
    
}
