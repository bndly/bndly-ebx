package org.bndly.ebx.jcr.importer.executor;

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

import org.bndly.ebx.jcr.importer.api.AbstractJobExecutor;
import org.bndly.ebx.jcr.importer.api.ContentService;
import org.bndly.ebx.jcr.importer.api.ContentMapperFacade;
import org.bndly.ebx.jcr.importer.api.ContentQueryMapper;
import org.bndly.ebx.jcr.importer.api.ImporterJobExecutor;
import org.bndly.ebx.jcr.importer.api.JobExecution;
import org.bndly.ebx.jcr.importer.api.JobExecutionException;
import org.bndly.ebx.model.JCRFindContentJob;
import org.bndly.ebx.jcr.importer.api.EntityContextData;
import org.bndly.schema.beans.SchemaBeanFactory;
import javax.jcr.Node;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

@Component(service = ImporterJobExecutor.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class FindContentJobExecutor extends AbstractJobExecutor<JCRFindContentJob> implements ImporterJobExecutor<JCRFindContentJob> {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Override
	protected SchemaBeanFactory getSchemaBeanFactory() {
		return schemaBeanFactory;
	}

	@Reference
	private ContentMapperFacade contentMapperFacade;

	@Override
	public Class<JCRFindContentJob> getSupportedJobType() {
		return JCRFindContentJob.class;
	}

	@Override
	public void execute(JCRFindContentJob job, JobExecution jobExecution) throws JobExecutionException {
		ContentService contentService = jobExecution.getContentService();
		EntityContextData entityCtxData = jobExecution.getFirstContextDataByTypeAndName(EntityContextData.class, "entity");
		Object entity = loadEntityFromEntityContextData(entityCtxData);
		if (entity != null) {
			ContentQueryMapper mapper = contentMapperFacade.getContentQueryMapper(job, jobExecution);
			if (mapper != null) {
				ContentQueryMapper cqm = ContentQueryMapper.class.cast(mapper);
				String query = cqm.buildXPATHQueryUsingEntity(entity,job);


				if (query == null) {
					query = cqm.buildQueryUsingJobAndContentService(job, jobExecution);
				}
				if (query == null) {
					throw new JobExecutionException("content query could not be build to find content.");
				}

				Node contentNode = contentService.querySingle(query);
				if (contentNode != null) {
					writeJobContentToContext(job, contentNode, jobExecution);
					job.setContentId(contentService.getContentNodeId(contentNode));
				} else {
					job.setNote("got null from: " + query);
				}
			} else {
				throw new JobExecutionException("mapper does not implement " + ContentQueryMapper.class.getSimpleName());
			}
		} else {
			job.setNote("entity did not exist anymore in the shop core.");
		}
	}

	public void setContentMapperFacade(ContentMapperFacade contentMapperFacade) {
		this.contentMapperFacade = contentMapperFacade;
	}

}
