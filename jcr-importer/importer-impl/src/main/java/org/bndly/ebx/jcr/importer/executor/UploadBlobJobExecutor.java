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
import org.bndly.ebx.jcr.importer.api.ImporterJobExecutor;
import org.bndly.ebx.model.BinaryData;
import org.bndly.ebx.jcr.importer.api.ContentContextData;
import org.bndly.ebx.jcr.importer.api.JobExecutionException;
import org.bndly.ebx.jcr.importer.api.JobExecution;
import org.bndly.ebx.model.JCRUploadBlobJob;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

@Component(service = ImporterJobExecutor.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class UploadBlobJobExecutor extends AbstractJobExecutor<JCRUploadBlobJob> implements ImporterJobExecutor<JCRUploadBlobJob> {

	private static final Logger LOG = LoggerFactory.getLogger(UploadBlobJobExecutor.class);

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Override
	protected SchemaBeanFactory getSchemaBeanFactory() {
		return schemaBeanFactory;
	}

	@Override
	public Class<JCRUploadBlobJob> getSupportedJobType() {
		return JCRUploadBlobJob.class;
	}

	@Override
	public void execute(JCRUploadBlobJob job, JobExecution jobExecution) throws JobExecutionException {

		ContentService contentService = jobExecution.getContentService();
		BinaryData data = job.getData();
		if (data != null) {

			//TODO: not really suitable for all kinds of blob data...currently only for images
			ContentContextData targetContentData = jobExecution.getSingleContextDataByTypeAndName(ContentContextData.class, "target");
			if(targetContentData != null) {
				Node contentNode = contentService.getContentNodeFromContextData(targetContentData);

				if(contentNode != null) {
					Node originalRenditionsFolder = contentService.getRenditionsFolderFromAsset(contentNode);

					try {

						contentService.createOriginalRendition(originalRenditionsFolder,data.getBytes(),data.getContentType());
					} catch (RepositoryException e) {
						LOG.error("Failed to create and set binary data to contentNode: {}", e.getMessage());
						job.setNote("RepositoryException: " + e.getMessage());
					}
				}else{
					job.setNote("Couldn't retrieve content from context data");
				}

			} else {
				job.setNote("content context data 'target' was null");
			}
		} else {
			job.setNote("data was null");
		}
	}

}
