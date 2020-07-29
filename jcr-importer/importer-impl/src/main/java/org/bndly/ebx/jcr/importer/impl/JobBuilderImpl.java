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

import org.bndly.ebx.jcr.importer.api.CMSImportInterceptor;
import org.bndly.ebx.jcr.importer.api.JobBuilder;
import org.bndly.ebx.jcr.importer.api.Configuration;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.jcr.importer.api.JobContext;
import org.bndly.ebx.jcr.importer.api.JobContextEntry;
import org.bndly.ebx.jcr.importer.api.JobContextProvider;
import org.bndly.ebx.jcr.importer.api.JobContextWriter;
import org.bndly.ebx.jcr.importer.api.JobHolder;
import org.bndly.ebx.model.CreateContentJob;
import org.bndly.ebx.model.DeleteContentJob;
import org.bndly.ebx.model.FindContentJob;
import org.bndly.ebx.model.GenericContentJob;
import org.bndly.ebx.model.ImporterJob;
import org.bndly.ebx.model.JobContextMap;
import org.bndly.ebx.model.UpdateContentJob;
import org.bndly.ebx.model.UploadBlobJob;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class JobBuilderImpl implements Configuration, JobBuilder {

	private final JobContext jobContext;
	private final JobContextProvider jobContextProvider;
	private final List<ImporterJob> jobs;
	private final JobContextEntry eventContext;
	private final Date creationDate;
	private final JCRImporterConfiguration configuration;
	private final Engine engine;
	private final SchemaBeanFactory schemaBeanFactory;
	private final CMSImportInterceptor.JobListener listener;
	private final RecordContext recordContext;
	private static final CMSImportInterceptor.JobListener NOOP_LISTENER = new CMSImportInterceptor.JobListener() {

		@Override
		public void createdJob(ImporterJob job) {
		}
	};

	public JobBuilderImpl(JobContext jobContext, JCRImporterConfiguration configuration, SchemaBeanFactory schemaBeanFactory, CMSImportInterceptor.JobListener listener) {
		this(null, jobContext, configuration, schemaBeanFactory, listener);
	}
	
	public JobBuilderImpl(JobContextProvider jobContextProvider, JCRImporterConfiguration configuration, SchemaBeanFactory schemaBeanFactory, CMSImportInterceptor.JobListener listener) {
		this(jobContextProvider, null, configuration, schemaBeanFactory, listener);
	}
	private JobBuilderImpl(JobContextProvider jobContextProvider, JobContext jobContext, JCRImporterConfiguration configuration, SchemaBeanFactory schemaBeanFactory, CMSImportInterceptor.JobListener listener) {
		this.engine = schemaBeanFactory.getEngine();
		this.schemaBeanFactory = schemaBeanFactory;
		this.configuration = configuration;
		this.jobContextProvider = jobContextProvider;
		this.jobContext = jobContext;
		jobs = new ArrayList<>();
		recordContext = engine.getAccessor().buildRecordContext();
		eventContext = instantiateContextEntry();
		this.listener = listener == null ? NOOP_LISTENER : listener;
		creationDate = new Date();
		
	}

	private JobContextEntry instantiateContextEntry() {
		return instantiateContextEntry(null);
	}

	private JobContextEntry instantiateContextEntry(final String parentContextKey) {
		JobContextEntry entry = writeJobContext(new JobContextProvider.Callback<JobContextEntry>() {

			@Override
			public JobContextEntry doWithContext(JobContext context) {
				if (parentContextKey == null) {
					return context.create();
				} else {
					return context.create(parentContextKey);
				}
			}
		});
		return entry;
	}

	@Override
	public List<String> supportedLocales() {
		List<String> supportedLanguages = configuration.getSupportedLanguages();
		if(supportedLanguages == null || supportedLanguages.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		return supportedLanguages;
	}

	public final void persist(final Transaction tx) {
		for (ImporterJob importerJob : jobs) {
			((ActiveRecord) importerJob).persist(tx);
			listener.createdJob(importerJob);
		}
	}

	public final void persist() {
		Transaction tx = engine.getQueryRunner().createTransaction();
		persist(tx);
		tx.commit();
	}

	@Override
	public Boolean getConfigBoolean(String key, Boolean defaultValue) {
		if (configuration == null) {
			return defaultValue;
		}
		Boolean val = configuration.getBoolean(key);
		if(val == null) {
			return defaultValue;
		}
		return val;
	}

	@Override
	public JobContextWriter getEventJobContextWriter() {
		return eventContext.getLocalJobContextWriter();
	}

	@Override
	public JobHolder<CreateContentJob> newCreateContentJob(String jobName) {
		return newJob(CreateContentJob.class, jobName);
	}

	@Override
	public JobHolder<DeleteContentJob> newDeleteContentJob(String jobName) {
		return newJob(DeleteContentJob.class, jobName);
	}

	@Override
	public JobHolder<FindContentJob> newFindContentJob(String jobName) {
		return newJob(FindContentJob.class, jobName);
	}

	@Override
	public JobHolder<GenericContentJob> newGenericJob(String jobName) {
		return newJob(GenericContentJob.class, jobName);
	}

	@Override
	public JobHolder<UpdateContentJob> newUpdateContentJob(String jobName) {
		return newJob(UpdateContentJob.class, jobName);
	}

	@Override
	public JobHolder<UploadBlobJob> newUploadBlobJob(String jobName) {
		return newJob(UploadBlobJob.class, jobName);
	}

	@Override
	public <E extends ImporterJob> JobHolder<E> newJob(final Class<E> jobInterface, final String jobName) {
		return newJob(jobInterface, jobName, null, this);
	}

	@Override
	public String getPathByName(String name) {
		List<JCRImporterConfiguration.Path> p = configuration.getPaths();
		if (p == null) {
			return null;
		}
		for (JCRImporterConfiguration.Path path : p) {
			if (name.equals(path.getName()) && path.getLanguage() == null) {
				return path.getJCRNodePath();
			}
		}
		return null;
	}

	@Override
	public String getPathByNameAndContentLanguage(String name, String language) {
		List<JCRImporterConfiguration.Path> p = configuration.getPaths();
		if (p == null) {
			return null;
		}
		for (JCRImporterConfiguration.Path path : p) {
			if (name.equals(path.getName()) && language.equals(path.getLanguage())) {
				return path.getJCRNodePath();
			}
		}
		return null;
	}

	static <E extends ImporterJob> JobHolder<E> newJob(final Class<E> jobInterface, final String jobName, final String parentContextKey, final JobBuilderImpl that) {
		return that.writeJobContext(new JobContextProvider.Callback<JobHolder<E>>() {
			@Override
			public JobHolder<E> doWithContext(JobContext context) {
				E job = that.schemaBeanFactory.getSchemaBean(jobInterface, that.recordContext.create(jobInterface.getSimpleName()));
				if (job == null) {
					throw new IllegalStateException("could not instantiate job: " + jobInterface.getSimpleName());
				}
				JobContextEntry jobContextEntry = that.instantiateContextEntry(parentContextKey);
				job.setCreatedOn(that.creationDate);
				job.setContextKey(jobContextEntry.getKey());
				// each job receives a sub-context of the event-context
				// this means that variables can have different values in the sub-contexts,
				// but share common variables in the event-context
				job.setName(jobName);
				// set context map
				JobContextMap contextMap = ((JobContextImpl) context).getJobContextMap();
				Record contextMapRecord = that.schemaBeanFactory.getRecordFromSchemaBean(contextMap);
				if (contextMapRecord.getId() == null) {
					throw new IllegalStateException("contextMapRecord was not persisted yet.");
				}
				job.setContextMap(that.schemaBeanFactory.getSchemaBean(JobContextMap.class, that.recordContext.create(contextMapRecord.getType(), contextMapRecord.getId())));
				that.jobs.add(job);
				return new JobHolderImpl<E>(that, job, jobContextEntry);
			}
		});
	}
	
	private <E> E writeJobContext(JobContextProvider.Callback<E> callback) {
		if (jobContext != null) {
			return callback.doWithContext(jobContext);
		} else {
			return jobContextProvider.write(callback);
		}
	}

}
