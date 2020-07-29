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
import org.bndly.ebx.jcr.importer.api.CompiledContentMapper;
import org.bndly.ebx.jcr.importer.api.ContentContextData;
import org.bndly.ebx.jcr.importer.api.ContentMapperFacade;
import org.bndly.ebx.jcr.importer.api.ContentService;
import org.bndly.ebx.jcr.importer.api.ImporterJobExecutor;
import org.bndly.ebx.jcr.importer.api.ContentNotFoundException;
import org.bndly.ebx.jcr.importer.api.JobExecutionException;
import org.bndly.ebx.jcr.importer.api.JobExecution;
import org.bndly.ebx.model.JCRUpdateContentJob;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

@Component(service = ImporterJobExecutor.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class UpdateContentJobExecutor extends AbstractJobExecutor<JCRUpdateContentJob> implements ImporterJobExecutor<JCRUpdateContentJob> {

	private static final Logger LOG = LoggerFactory.getLogger(UpdateContentJobExecutor.class);

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Override
	protected SchemaBeanFactory getSchemaBeanFactory() {
		return schemaBeanFactory;
	}

	@Reference
	private ContentMapperFacade contentMapperFacade;

	@Override
	public Class<JCRUpdateContentJob> getSupportedJobType() {
		return JCRUpdateContentJob.class;
	}

	private Node getContentNodeToUpdate(JCRUpdateContentJob job,JobExecution jobExecution) throws JobExecutionException {
		Node nodeToUpdate;
		try {
			ContentService contentService = jobExecution.getContentService();
			// select the content by path name and contenttype
			String sourceVar = job.getSourceVar();
			if (sourceVar != null) {
				List<ContentContextData> contentContextDataObjects = jobExecution.getContextDataByTypeAndName(ContentContextData.class, sourceVar);
				if (contentContextDataObjects != null && contentContextDataObjects.size() == 1) {
					ContentContextData contentContextData = contentContextDataObjects.get(0);
					nodeToUpdate = contentService.getContentNodeFromContextData(contentContextData);
				} else {
					throw new JobExecutionException("couldn't not find a unique content object in the jobContext named " + sourceVar + " of type " + ContentContextData.class.getSimpleName());
				}
			} else {
				String contentName = job.getContentName();
				String targetPath = job.getTargetPath();
				// if it does not exist or does not fulfill the requiredments, throw an exception
				nodeToUpdate = contentService.getContentNodeByNameAndParentPath(contentName, targetPath);
			}

			job.setContentId(contentService.getContentNodeId(nodeToUpdate));
			return nodeToUpdate;
		} catch (ContentNotFoundException e) {
			throw new JobExecutionException("could not find content to update", e);
		}
	}

	@Override
	public void execute(JCRUpdateContentJob job, JobExecution jobExecution) throws JobExecutionException {
		LOG.info("executing update content job");
		// look for a contenttype specific mapper
		CompiledContentMapper mapper = contentMapperFacade.getCompiledMapper(job, jobExecution);
		if (mapper != null) {
			Node nodeToUpdate = getContentNodeToUpdate(job, jobExecution);
			mapper.map(nodeToUpdate);
		}
	}

	public void setContentMapperFacade(ContentMapperFacade contentMapperFacade) {
		this.contentMapperFacade = contentMapperFacade;
	}

}
