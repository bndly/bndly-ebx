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

import org.bndly.ebx.jcr.importer.api.ContentSynchronizationStrategy;
import org.bndly.ebx.jcr.importer.api.CMSImportInterceptor;
import org.bndly.ebx.jcr.importer.api.JobContext;
import org.bndly.ebx.jcr.importer.api.JobContextProvider;
import org.bndly.ebx.jcr.importer.api.PreconfiguredCmsDao;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.listener.DeleteListener;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CMSImportInteceptor registers all persistence related operations on entities that are synchronized with the CMS. Once such an operation occurs, an import job entry might be stored in the
 * database. This job might be picked up by the actual import component.
 */
public class CMSImportInterceptorImpl implements CMSImportInterceptor, PersistListener, MergeListener, DeleteListener {

	private static final Logger LOG = LoggerFactory.getLogger(CMSImportInterceptorImpl.class);

	private final SchemaBeanFactory schemaBeanFactory;
	private final JobContextProvider jobContextProvider;
	private final ContentSynchronizationStrategyTracker contentSynchronizationStrategyTracker;
	private final PreconfiguredCmsDao preconfiguredCmsDao;

	public CMSImportInterceptorImpl(SchemaBeanFactory schemaBeanFactory, JobContextProvider jobContextProvider, ContentSynchronizationStrategyTracker contentSynchronizationStrategyTracker, PreconfiguredCmsDao preconfiguredCmsDao) {
		this.schemaBeanFactory = schemaBeanFactory;
		this.jobContextProvider = jobContextProvider;
		this.contentSynchronizationStrategyTracker = contentSynchronizationStrategyTracker;
		this.preconfiguredCmsDao = preconfiguredCmsDao;
	}

	@Override
	public List<Class<?>> listSchemaBeanTypesWithSynchronizationStrategy() {
		return contentSynchronizationStrategyTracker.listSchemaBeanTypesWithSynchronizationStrategy();
	}

	@Override
	public void scheduleImportOfRecord(Record record, Transaction transaction, JobListener listener) {
		if (contentSynchronizationStrategyTracker.isCMSSynchronized(record)) {
			JobBuilderImpl builder = createJobBuilder(listener, null);
			builder.getEventJobContextWriter().string().var("event").stringValue("created").build();
			// make sure that the created entity is stored in the job context
			builder.getEventJobContextWriter().entity().var("created").type(record.getType().getName()).idProperty("id").idValue(Long.toString(record.getId())).build();
			// look for a strategy instance, that handles the create event
			// for the given entity type
			ContentSynchronizationStrategy strat = contentSynchronizationStrategyTracker.getStrategyForEntity(record);
			Object bean = schemaBeanFactory.getSchemaBean(strat.getEntityType(), record);
			strat.created(bean, builder);
			builder.persist(transaction);
		}
	}

	@Override
	public void scheduleImportOfRecord(Record record, Transaction transaction, JobListener listener, JobContext jobContext) {
		if (contentSynchronizationStrategyTracker.isCMSSynchronized(record)) {
			JobBuilderImpl builder = createJobBuilder(listener, jobContext);
			builder.getEventJobContextWriter().string().var("event").stringValue("created").build();
			// make sure that the created entity is stored in the job context
			builder.getEventJobContextWriter().entity().var("created").type(record.getType().getName()).idProperty("id").idValue(Long.toString(record.getId())).build();
			// look for a strategy instance, that handles the create event
			// for the given entity type
			ContentSynchronizationStrategy strat = contentSynchronizationStrategyTracker.getStrategyForEntity(record);
			Object bean = schemaBeanFactory.getSchemaBean(strat.getEntityType(), record);
			strat.created(bean, builder);
			builder.persist(transaction);
		}
	}

	@Override
	public void onPersist(Record record) {
		LOG.debug("Called 'onPersist'");
		if (contentSynchronizationStrategyTracker.isCMSSynchronized(record)) {
			JobBuilderImpl builder = createJobBuilder();
			builder.getEventJobContextWriter().string().var("event").stringValue("created").build();
			// make sure that the created entity is stored in the job context
			builder.getEventJobContextWriter().entity().var("created").type(record.getType().getName()).idProperty("id").idValue(Long.toString(record.getId())).build();
			// look for a strategy instance, that handles the create event 
			// for the given entity type
			ContentSynchronizationStrategy strat = contentSynchronizationStrategyTracker.getStrategyForEntity(record);
			Object bean = schemaBeanFactory.getSchemaBean(strat.getEntityType(), record);

			LOG.info("onPersist called '{}'", bean.getClass().getSimpleName());
			strat.created(bean, builder);
			builder.persist();
		}
	}

	@Override
	public void onMerge(Record record) {
		LOG.debug("Called 'onMerge'");
		if (contentSynchronizationStrategyTracker.isCMSSynchronized(record)) {
			JobBuilderImpl builder = createJobBuilder();
			builder.getEventJobContextWriter().string().var("event").stringValue("updated").build();
			builder.getEventJobContextWriter().entity().var("updated").type(record.getType().getName()).idProperty("id").idValue(Long.toString(record.getId())).build();
			ContentSynchronizationStrategy strat = contentSynchronizationStrategyTracker.getStrategyForEntity(record);
			Object bean = schemaBeanFactory.getSchemaBean(strat.getEntityType(), record);
			strat.updated(bean, builder);
			builder.persist();
		}
	}

	@Override
	public void onDelete(Record record) {
		LOG.debug("Called 'onDelete'");
		if (contentSynchronizationStrategyTracker.isCMSSynchronized(record)) {
			JobBuilderImpl builder = createJobBuilder();
			builder.getEventJobContextWriter().string().var("event").stringValue("deleted").build();
			builder.getEventJobContextWriter().entity().var("deleted").type(record.getType().getName()).idProperty("id").idValue(Long.toString(record.getId())).build();
			ContentSynchronizationStrategy strat = contentSynchronizationStrategyTracker.getStrategyForEntity(record);
			Object bean = schemaBeanFactory.getSchemaBean(strat.getEntityType(), record);
			strat.deleted(bean, builder);
			builder.persist();
		}
	}

	private JobBuilderImpl createJobBuilder() {
		return createJobBuilder(null, null);
	}

	private JobBuilderImpl createJobBuilder(JobListener listener, JobContext jobContext) {
		if (jobContext != null) {
			return new JobBuilderImpl(jobContext, preconfiguredCmsDao.getConfiguration(), schemaBeanFactory, listener);
		} else {
			return new JobBuilderImpl(jobContextProvider, preconfiguredCmsDao.getConfiguration(), schemaBeanFactory, listener);
		}
	}

}
