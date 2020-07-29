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

import org.bndly.ebx.jcr.importer.api.CompiledContentMapper;
import org.bndly.ebx.jcr.importer.api.ContentMapper;
import org.bndly.ebx.jcr.importer.api.ContentMapperFacade;
import org.bndly.ebx.jcr.importer.api.ContentObjectMapper;
import org.bndly.ebx.jcr.importer.api.ContentPropertyMapper;
import org.bndly.ebx.jcr.importer.api.ContentQueryMapper;
import org.bndly.ebx.jcr.importer.api.JcrUtil;
import org.bndly.ebx.jcr.importer.api.JobExecution;
import org.bndly.ebx.jcr.importer.api.JobExecutionException;
import org.bndly.ebx.model.ImporterJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(service = ContentMapperFacade.class)
public class ContentMapperFacadeImpl implements ContentMapperFacade {

	
	private final List<ContentMapper> contentMappers = new ArrayList<>();
	private final List<ContentQueryMapper> contentQueryMappers = new ArrayList<>();
	private final Map<String, List<ContentMapper>> contentMappersByContentType = new HashMap<>();

	private ContentMapper getContentMapperFor(ImporterJob job, JobExecution jobExecution) {
		// look for all propertymappers that support the desired content type
		// ask each of those filtered mappers, if they can deal with the provided
		// inputData object.
		for (ContentMapper contentMapper : contentMappers) {
			if (contentMapper.supportsJob(job, jobExecution)) {
				return contentMapper;
			}
		}
		return null;
	}

	@Reference(
			bind = "addContentMapper",
			unbind = "removeContentMapper",
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC,
			service = ContentMapper.class
	)
	public void addContentMapper(ContentMapper contentMapper) {
		if (contentMapper != null) {
			contentMappers.add(contentMapper);
			if (ContentQueryMapper.class.isInstance(contentMapper)) {
				contentQueryMappers.add((ContentQueryMapper) contentMapper);
			}
			List<ContentMapper> list = contentMappersByContentType.get(contentMapper.getSupportedContentTypeName());
			if (list == null) {
				list = new ArrayList<>();
				contentMappersByContentType.put(contentMapper.getSupportedContentTypeName(), list);
			}
			list.add(contentMapper);
		}
	}

	public void removeContentMapper(ContentMapper contentMapper) {
		if (contentMapper != null) {
			Iterator<ContentMapper> iterator = contentMappers.iterator();
			while (iterator.hasNext()) {
				if (iterator.next() == contentMapper) {
					iterator.remove();
				}
			}
			if (ContentQueryMapper.class.isInstance(contentMapper)) {
				Iterator<ContentQueryMapper> iterator1 = contentQueryMappers.iterator();
				while (iterator1.hasNext()) {
					if (iterator1.next() == contentMapper) {
						iterator1.remove();
					}
				}
			}
			List<ContentMapper> list = contentMappersByContentType.get(contentMapper.getSupportedContentTypeName());
			if (list != null) {
				list.remove(contentMapper);
				if (list.isEmpty()) {
					contentMappersByContentType.remove(contentMapper.getSupportedContentTypeName());
				}
			}
		}
	}

	@Override
	public CompiledContentMapper getCompiledMapper(final ImporterJob job, final JobExecution jobExecution) {
		ContentMapper mapper = getContentMapperFor(job, jobExecution);
		if (mapper == null) {
			return null;
		}
		if (ContentPropertyMapper.class.isInstance(mapper)) {
			final ContentPropertyMapper contentPropertyMapper = ((ContentPropertyMapper) mapper);
			return new CompiledContentMapper() {
				@Override
				public void map(Node nodeToUpdate) throws JobExecutionException {
					Map<String, Object> contentObjectProperties = new HashMap<>();
					contentPropertyMapper.mapContextToContentProperties(job, jobExecution, contentObjectProperties);

					for (String key : contentObjectProperties.keySet()) {
						Object valueObj = contentObjectProperties.get(key);
						try {
							JcrUtil.setProperty(nodeToUpdate, key, valueObj);
						} catch (RepositoryException e) {
							throw new JobExecutionException("could not set property: " + e.getMessage(), e);
						}
					}
					if (ContentObjectMapper.class.isInstance(contentPropertyMapper)) {
						((ContentObjectMapper) contentPropertyMapper).mapContextToContentObject(nodeToUpdate, jobExecution);
					}
				}
			};
		} else if (ContentObjectMapper.class.isInstance(mapper)) {
			final ContentObjectMapper contentObjectMapper = ((ContentObjectMapper) mapper);
			return new CompiledContentMapper() {
				@Override
				public void map(Node nodeToUpdate) throws JobExecutionException {
					contentObjectMapper.mapContextToContentObject(nodeToUpdate, jobExecution);
				}
			};
		} else {
			return null;
		}

	}

	@Override
	public ContentQueryMapper getContentQueryMapper(ImporterJob job, JobExecution jobExecution) {
		for (ContentQueryMapper contentMapper : contentQueryMappers) {
			if (contentMapper.supportsJob(job, jobExecution)) {
				return contentMapper;
			}
		}
		return null;
	}
	
	
}
