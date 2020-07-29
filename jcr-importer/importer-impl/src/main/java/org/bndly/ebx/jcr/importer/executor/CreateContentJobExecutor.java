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
import org.bndly.ebx.jcr.importer.api.ContentNotFoundException;
import org.bndly.ebx.jcr.importer.api.ContentService;
import org.bndly.ebx.jcr.importer.api.ImporterJobExecutor;
import org.bndly.ebx.jcr.importer.api.JobExecution;
import org.bndly.ebx.jcr.importer.api.JobExecutionException;
import org.bndly.ebx.jcr.importer.api.StringContextData;
import org.bndly.ebx.model.JCRCreateContentJob;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import javax.jcr.Node;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

@Component(service = ImporterJobExecutor.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class CreateContentJobExecutor extends AbstractJobExecutor<JCRCreateContentJob> implements ImporterJobExecutor<JCRCreateContentJob> {

	private static final Logger LOG = LoggerFactory.getLogger(CreateContentJobExecutor.class);
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Override
	protected SchemaBeanFactory getSchemaBeanFactory() {
		return schemaBeanFactory;
	}

	@Override
	public Class<JCRCreateContentJob> getSupportedJobType() {
		return JCRCreateContentJob.class;
	}

	@Override
	public void execute(JCRCreateContentJob job, JobExecution jobExecution) throws JobExecutionException {
		LOG.info("executing create content job");

		ContentService contentService = jobExecution.getContentService();

		//String contentTypeName = job.getContentTypeName();
		String jcrPrimaryType = job.getJcrPrimaryType();
		String targetPath = job.getTargetPath();
		String contentName = job.getContentName();

		StringContextData createsubfolders = jobExecution.getSingleContextDataByTypeAndName(StringContextData.class, "createSubFolders");
		boolean createSubFolders = createsubfolders == null ? false : "true".equals(createsubfolders.getStringValue());

		if (createSubFolders) {
			String parentPath = job.getParentPath();
			contentService.createFolders(parentPath, targetPath);
		}
		Node targetNode = contentService.getContentNodeByPath(targetPath);

		// if an object with a compatible type exists at the target path,
		// we will use that rather than running in a duplicate name exception.
		Node createdContent = null;
		try {
			createdContent = contentService.getContentNodeByName(contentName, targetNode);
			job.setNote("object already existed.");
		} catch (ContentNotFoundException e) {

			//lets do some special treatment for image content, since assets consist of more than
			//a single node
			if ("createImage".equals(job.getName())) {
				createdContent = contentService.createImageContent(targetNode, contentName, jcrPrimaryType);
			} else {
				createdContent = contentService.createContent(targetNode, contentName, jcrPrimaryType);
			}
		}

		if (createdContent != null) {
			writeJobContentToContext(job, createdContent, jobExecution);
			job.setContentId(contentService.getContentNodeId(createdContent));
		} else {
			LOG.error("createdContent node is null for name(" + contentName + ") and targetPath(" + targetPath + ")");
		}
	}
}
