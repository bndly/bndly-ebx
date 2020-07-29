package org.bndly.ebx.jcr.importer.api;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-api
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

import org.bndly.ebx.model.ContextWritingJob;
import org.bndly.ebx.model.ImporterJob;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.SchemaBeanFactory;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

public abstract class AbstractJobExecutor<JOB_TYPE extends ImporterJob> implements ImporterJobExecutor<JOB_TYPE> {

    protected Object loadEntityFromEntityContextData(EntityContextData entityContextData) {
        return loadEntityFromEntityContextData(entityContextData, Object.class);
    }
    
	protected abstract SchemaBeanFactory getSchemaBeanFactory();
	
    protected <T> T loadEntityFromEntityContextData(EntityContextData entityContextData, Class<T> type) {
        Long id = new Long(entityContextData.getIdValue());
        String typeName = entityContextData.getType();
		RecordContext context = getSchemaBeanFactory().getRecordFromSchemaBean(entityContextData).getContext();
		if(Object.class.equals(type)) {
			return (T) getSchemaBeanFactory().getSchemaBean(context.create(typeName, id));
		} else {
			return (T) getSchemaBeanFactory().getSchemaBean(type, context.create(typeName, id));
		}
    }
    
    protected <E extends ImporterJob & ContextWritingJob> void writeJobContentToContext(E job, Node contentNode, JobExecution jobExecution) {
        String contextVar = job.getContextVar();
        if(contextVar != null) {
            writeContextData(contentNode, contextVar, jobExecution.getLocalJobContextWriter());
        }
        String rootContextVar = job.getRootContextVar();
        if(rootContextVar != null) {
            writeContextData(contentNode, rootContextVar, jobExecution.getRootJobContextWriter());
        }
    }
        
    private void writeContextData(Node createdContentNode, String varName, JobContextWriter jobContextIO) {
		try {
			jobContextIO.content()
					.var(varName)
					.id(getUUIDFromContentNode(createdContentNode))
					.path(createdContentNode.getPath())
					.type(createdContentNode.getPrimaryNodeType().getName())
					.build();
		}catch(RepositoryException e){
			//TODO: insert logging
		}
    }

	private static String getUUIDFromContentNode(Node contentNode){
		if(contentNode != null){
			try {
				if (contentNode.hasProperty("id")) {
					return contentNode.getProperty("id").getString();
				}
			}catch(RepositoryException e){
				return null;
			}
		}else{
			throw new IllegalArgumentException("contentNode must not be null");
		}

		return null;
	}

}
