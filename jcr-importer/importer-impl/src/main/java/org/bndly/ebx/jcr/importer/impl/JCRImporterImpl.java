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

import org.bndly.common.lang.IteratorChain;
import org.bndly.common.lang.TransformingIterator;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.jcr.importer.api.JobContextData;
import org.bndly.ebx.jcr.importer.api.JobExecutionException;
import org.bndly.ebx.jcr.importer.api.ContentService;
import org.bndly.ebx.jcr.importer.api.ImportScheduler;
import org.bndly.ebx.jcr.importer.api.JobContextEntry;
import org.bndly.ebx.jcr.importer.api.CMSImportInterceptor;
import org.bndly.ebx.jcr.importer.api.ImporterJobExecutor;
import org.bndly.ebx.jcr.importer.api.CmsDao;
import org.bndly.ebx.jcr.importer.api.JobExecution;
import org.bndly.ebx.jcr.importer.api.JCRImporter;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.jcr.importer.api.JobContext;
import org.bndly.ebx.jcr.importer.api.JobContextProvider;
import org.bndly.ebx.jcr.importer.api.JobContextWriter;
import org.bndly.ebx.jcr.importer.api.PreconfiguredCmsDao;
import org.bndly.ebx.jcr.importer.api.PublicationService;
import org.bndly.ebx.jcr.importer.executor.ImporterJobExecutorTracker;
import org.bndly.ebx.model.ImportPlan;
import org.bndly.ebx.model.ImporterJob;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.listener.QueryByExampleIteratorListener;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = JCRImporter.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(factory = true, ocd = JCRImporterImpl.Configuration.class)
public class JCRImporterImpl implements JCRImporter {

	@ObjectClassDefinition(
			name = "JCR Importer"
	)
	public @interface Configuration {

		@AttributeDefinition(name = "Name", description = "The name for this JCR Importer instance.")
		String name();

		@AttributeDefinition(name = "Hook in persistence lifecycle", description = "If the hook into the persistence lifecycle is enabled, then the importer call synchronization strategies for successfull persistence operations.")
		boolean persistenceLifecycleHookEnabled() default true;
		
		@AttributeDefinition(name = "Bulk size", description = "The bulk size to use during a full synchronization. The value should be greater than 0. ")
		int fullSynchronizationBulkSize() default 20;

		@AttributeDefinition(name = "CMS DAO", description = "The filter expression to get the CMS DAO to use for this importer.")
		String cmsDao_target();

		@AttributeDefinition(name = "Publication service", description = "The filter expression to get the publication service to use for this importer.")
		String publicationService_target();
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(JCRImporterImpl.class);

	private boolean running = false;
	@Reference(name = "cmsDao")
	private PreconfiguredCmsDao cmsDao;

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference(name = "publicationService")
	private PublicationService publicationService;
	private double done;

	@Reference
	private JobContextProvider jobContextProvider;
	private ImporterJobExecutorTracker importerJobExecutorTracker;
	private ContentSynchronizationStrategyTracker contentSynchronizationStrategyTracker;
	private CMSImportInterceptorImpl cmsImportInterceptorImpl;
	private boolean persistenceLifecycleHookEnabled;
	private int fullSynchronizationBulkSize = 20;

	@Override
	public boolean isConnected() {
		Boolean r = cmsDao.run(new CmsDao.JCRSessionCallback<Boolean>() {
			@Override
			public Boolean doInJCRSession(Session session) throws RepositoryException {
				return true;
			}
		});
		return r == null ? false : r;
	}

	private Engine getEngine() {
		return schemaBeanFactory.getEngine();
	}

	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties());
		persistenceLifecycleHookEnabled = dictionaryAdapter.getBoolean("persistenceLifecycleHookEnabled", Boolean.TRUE);
		fullSynchronizationBulkSize = dictionaryAdapter.getInteger("fullSynchronizationBulkSize", fullSynchronizationBulkSize);
		importerJobExecutorTracker = new ImporterJobExecutorTracker(componentContext.getBundleContext());
		importerJobExecutorTracker.open();
		contentSynchronizationStrategyTracker = new ContentSynchronizationStrategyTracker(componentContext.getBundleContext());
		contentSynchronizationStrategyTracker.open();
		cmsImportInterceptorImpl = new CMSImportInterceptorImpl(schemaBeanFactory, jobContextProvider, contentSynchronizationStrategyTracker, cmsDao);
		if (persistenceLifecycleHookEnabled) {
			schemaBeanFactory.getEngine().addListener(cmsImportInterceptorImpl);
		}
	}

	@Deactivate
	public void deactivate() {
		importerJobExecutorTracker.close();
		importerJobExecutorTracker = null;
		contentSynchronizationStrategyTracker.close();
		contentSynchronizationStrategyTracker = null;
		if (persistenceLifecycleHookEnabled) {
			schemaBeanFactory.getEngine().removeListener(cmsImportInterceptorImpl);
		}
		cmsImportInterceptorImpl = null;
	}

	@Override
	public ImportScheduler createImportScheduler() {
		return createImportScheduler(null);
	}

	private ImportScheduler createImportScheduler(final List<ImporterJob> jobs) {
		final Transaction tx = schemaBeanFactory.getEngine().getQueryRunner().createTransaction();
		final CMSImportInterceptor.JobListener listener;
		if (jobs == null) {
			listener = null;
		} else {
			listener = new CMSImportInterceptor.JobListener() {

				@Override
				public void createdJob(ImporterJob job) {
					jobs.add(job);
				}
			};
		}
		return new ImportScheduler() {

			@Override
			public void scheduleSynchronization(Record record) {
				cmsImportInterceptorImpl.scheduleImportOfRecord(record, tx, listener);
			}

			@Override
			public void scheduleSynchronization(Record record, JobContext jobContext) {
				cmsImportInterceptorImpl.scheduleImportOfRecord(record, tx, listener, jobContext);
			}

			@Override
			public void close() throws Exception {
				tx.commit();
			}

		};
	}

	@Override
	public void scheduleSynchronization(Record record) {
		cmsImportInterceptorImpl.onPersist(record);
	}

	@Override
	public void scheduleFullsynchronization() {
		scheduleFullsynchronization(null, getEngine().getAccessor().buildRecordContext());
	}

	private void scheduleFullsynchronization(List<ImporterJob> jobs, final RecordContext recordContext) {
		// the batch sizes are set individually in regard to the average size of the objects
		List<Class<?>> supportedSchemaBeanTypes = cmsImportInterceptorImpl.listSchemaBeanTypesWithSynchronizationStrategy();
		if (supportedSchemaBeanTypes.isEmpty()) {
			LOG.info("no types have to be iterated for synchronization events. therefore full synchronization has been skipped.");
			return;
		}
		LOG.info("{} types have to be iterated for synchronization events", supportedSchemaBeanTypes.size());
		try (ImportScheduler importScheduler = createImportScheduler(jobs)) {
			for (Class<?> schemaBeanType : supportedSchemaBeanTypes) {
				LOG.info("requesting synchronization for {}", schemaBeanType.getName());
				requestSynchronization(importScheduler, schemaBeanType, 20, recordContext);
				LOG.info("requested synchronization for {}", schemaBeanType.getName());
			}
			LOG.info("requested synchronization for all types. flushing the import jobs now.");
		} catch (Exception e) {
			LOG.error("failed to schedule full synchronization: " + e.getMessage(), e);
		}
	}
	
	private void requestFullsynchronization(int bulkSize) {
		for(Runnable runnable : createSynchronizationRunnables(bulkSize)) {
			runnable.run();
		}
	}

	@Override
	public void requestFullsynchronization() {
		LOG.debug("Request FullSync");
		for(Runnable runnable : createSynchronizationRunnables(fullSynchronizationBulkSize)) {
			runnable.run();
		}
	}
	
	private Iterator<Record> iterate(String typeName, final int bulkSize) {
		final String query = "PICK " + typeName + " OFFSET ? LIMIT ?";
		final Accessor accessor = schemaBeanFactory.getEngine().getAccessor();
		return new Iterator<Record>() {

			private boolean reachedEnd = false;
			private Integer offset;
			private Iterator<Record> result;

			@Override
			public boolean hasNext() {
				if (reachedEnd) {
					return false;
				}
				if (offset == null) {
					offset = 0;
					result = accessor.query(query, offset, bulkSize);
					if (!result.hasNext()) {
						reachedEnd = true;
						return false;
					}
				}
				if (result.hasNext()) {
					return true;
				} else {
					offset = offset + bulkSize;
					result = accessor.query(query, offset, bulkSize);
					if (result.hasNext()) {
						return true;
					} else {
						reachedEnd = true;
						return false;
					}
				}
			}

			@Override
			public Record next() {
				if (!hasNext()) {
					throw new NoSuchElementException("no more records available");
				}
				return result.next();
			}

			@Override
			public void remove() {
				// not supported
			}
		};
	}

	private Iterable<Runnable> createSynchronizationRunnables(final int bulkSize) {
		final List<Class<?>> supportedSchemaBeanTypes = cmsImportInterceptorImpl.listSchemaBeanTypesWithSynchronizationStrategy();
		if (supportedSchemaBeanTypes.isEmpty()) {
			LOG.info("no types have to be iterated for synchronization events. therefore full synchronization has been skipped.");
			return Collections.EMPTY_LIST;
		}
		return new Iterable<Runnable>() {
			@Override
			public Iterator<Runnable> iterator() {
				return new TransformingIterator<Runnable, Class<?>>(supportedSchemaBeanTypes.iterator()) {
					@Override
					protected Runnable transform(final Class<?> schemaBeanType) {
						return new Runnable() {
							@Override
							public void run() {
								// synchronize this schema bean type with the defined bulk size
								Iterator<Record> iter = iterate(schemaBeanType.getSimpleName(), bulkSize);
								List<Record> batch = new ArrayList<>(bulkSize);
								while (iter.hasNext()) {
									batch.add(iter.next());
									if (batch.size() == bulkSize) {
										performSynchronization(batch);
										batch.clear();
									}
								}
								if (!batch.isEmpty()) {
									performSynchronization(batch);
								}
							}
						};
					}
				};
			}
		};
	}

	private void requestSynchronization(final ImportScheduler importScheduler, Class<?> schemaBeanType, final int batchSize, RecordContext recordContext) {
		LOG.debug("requestSync");
		getEngine().getAccessor().iterate(schemaBeanType.getSimpleName(), new QueryByExampleIteratorListener() {
			@Override
			public void handleRecord(Record record) {
				importScheduler.scheduleSynchronization(record);
			}
		}, batchSize, false, recordContext);
	}

	@Override
	public ImportPlan createImportPlanWithScheduledJobs() {
		return createImportPlanWithScheduledJobs(null, schemaBeanFactory.getEngine().getAccessor().buildRecordContext());
	}

	private ImportPlan createImportPlanWithScheduledJobs(List<ImporterJob> jobs, final RecordContext recordContext) {
		LOG.info("Creating import plan");
		ImportPlan plan = schemaBeanFactory.getSchemaBean(ImportPlan.class, recordContext.create(ImportPlan.class.getSimpleName()));
		plan.setBatch(UUID.randomUUID().toString());
		LOG.info("creating import plan {} from unscheduled jobs.", plan.getBatch());
		((ActiveRecord) plan).persist();
		Transaction tx = getEngine().getQueryRunner().createTransaction();
		if (jobs == null || jobs.isEmpty()) {
			LOG.info("loading unscheduled jobs.");
			// there might be jobs with no plan assigned, but they had been executed already
			Iterator<Record> unscheduled = getEngine().getAccessor().query("PICK ImporterJob i IF i.plan=? AND i.startedOn=?", recordContext, null, null, null);
			LOG.info("loaded unscheduled jobs.");
			if (jobs == null) {
				jobs = new ArrayList<>();
			}
			while (unscheduled.hasNext()) {
				Record unscheduledJobRecord = unscheduled.next();
				ImporterJob job = schemaBeanFactory.getSchemaBean(ImporterJob.class, unscheduledJobRecord);
				if (job != null) {
					job.setPlan(plan);
					((ActiveRecord) job).update(tx);
					jobs.add(job);
				}
			}
		} else {
			LOG.info("assigning {} already loaded jobs to import plan", jobs.size());
			for (ImporterJob job : jobs) {
				if (job != null) {
					job.setPlan(plan);
					((ActiveRecord) job).update(tx);
				}
			}
		}
		LOG.info("updating jobs that are now assigned to import plan {}", plan.getBatch());
		tx.commit();
		LOG.info("updated jobs that are now assigned to import plan {} successfully", plan.getBatch());
		return plan;
	}

	@Override
	public double getDone() {
		return done;
	}

	private synchronized void startImport(final ImportPlan plan, final List<ImporterJob> jobs, final RecordContext recordContext) {
		final Set<String> pathsToPublish = new LinkedHashSet<>();
		cmsDao.run(new CmsDao.JCRSessionCallback<Object>() {
			@Override
			public Object doInJCRSession(final Session session) throws RepositoryException {
				LOG.info("Triggered 'startImport'");
				final ContentService contentService = new ContentServiceImpl(session, cmsDao.getConfiguration(), publicationService);
				jobContextProvider.newJobContext(null, new JobContextProvider.VoidCallback() {

					@Override
					public void doWithContext(final JobContext context) {
						done = 0;
						if (plan != null) {
							RecordContext ctx = schemaBeanFactory.getRecordFromSchemaBean(plan).getContext();
							final List<ImporterJob> jobsToUse;
							if (jobs == null || jobs.isEmpty()) {
								jobsToUse = loadJobsOfPlan(ctx, plan);
							} else {
								LOG.info("starting import with a specified list of jobs. no jobs will be loaded additionally.");
								jobsToUse = jobs;
							}
							if (!jobsToUse.isEmpty()) {
								Transaction tx = getEngine().getQueryRunner().createTransaction();
								final Date startedOn = new Date();
								int total = jobsToUse.size();
								int index = 0;
								for (ImporterJob importerJob : jobsToUse) {
									index++;
									JobExecution execution = createJobExecution(contentService, importerJob, context, recordContext, pathsToPublish);
									handleImporterJob(session, importerJob, plan, startedOn, execution);
									((ActiveRecord) importerJob).update(tx);
									done = ((double) index) / total;
								}
								tx.commit();
							}
						}
						done = 0;
					}

				});
				return null;
			}
		});
		if (!pathsToPublish.isEmpty()) {
			publicationService.doPublish(cmsDao.getConfiguration(), pathsToPublish);
		}
	}

	private void handleImporterJob(Session session, ImporterJob importerJob, ImportPlan plan, final Date startedOn, JobExecution execution) {
		try {
			importerJob.setStartedOn(startedOn);
			ImporterJobExecutor executor = importerJobExecutorTracker.createExecutorForJob(importerJob);
			if (executor == null) {
				handleUnsupportedJob(importerJob);
			} else {
				executor.execute(importerJob, execution);
				importerJob.setFinishedOn(startedOn);
			}
		} catch (Exception e) {
			importerJob.setCanceledOn(new Date());
			importerJob.setNote("EXCEPTION: " + e.getMessage());
			if (!JobExecutionException.class.isInstance(e)) {
				LOG.error("failed to execute importer job: " + e.getMessage(), e);
			}
		} finally {
			importerJob.setPlan(plan);
			try {
				/*TODO: really wanna save after every single job or should we be
				  able to do a complete rollback for the whole import plan*/
				session.save();
			} catch (Exception e) {
				LOG.error("failed to save the JCR session: " + e.getMessage(), e);
			}
		}
	}
	private List<ImporterJob> loadJobsOfPlan(RecordContext ctx, ImportPlan plan) {
		LOG.info("starting import with an unspecified list of jobs. loading jobs now.");
		List<ImporterJob> jobs = new ArrayList<>();
		List<Record> scheduled = getEngine().getAccessor().queryByExample(ImporterJob.class.getSimpleName(), ctx).attribute("plan", ((ActiveRecord) plan).getId()).all();
		for (Record record : scheduled) {
			jobs.add((ImporterJob) schemaBeanFactory.getSchemaBean(record));
		}
		return jobs;
	}

	private void handleUnsupportedJob(ImporterJob importerJob) {
		importerJob.setCanceledOn(new Date());
		importerJob.setNote("unsupported importer job type: " + importerJob.getClass().getSimpleName());
	}

	@Override
	public void requestImport() {
		if (getConfig().isEnabled()) {
			if (!isRunning()) {
				running = true;
				try {
					List<ImporterJob> jobs = new ArrayList<>();
					RecordContext recordContext = getEngine().getAccessor().buildRecordContext();
					ImportPlan plan = createImportPlanWithScheduledJobs();
					startImport(plan, jobs, recordContext);
				} finally {
					running = false;
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return getConfig().isEnabled();
	}

	@Override
	public JCRImporterConfiguration getConfig() {
		return cmsDao.getConfiguration();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean establishTestConnection(int retries) {
		while (retries > 0) {
			if (isConnected()) {
				return true;
			}
			retries--;
		}
		return false;
	}

	private JobExecution createJobExecution(final ContentService contentService, final ImporterJob job, final JobContext jobContext, final RecordContext recordContext, final Set<String> pathsToPublish) {
		return new JobExecution() {
			private JobContextEntry entry;
			private JobContextWriter localWriter;
			private JobContextWriter rootWriter;

			@Override
			public RecordContext getRecordContext() {
				return recordContext;
			}

			@Override
			public ContentService getContentService() {
				return contentService;
			}

			private JobContextEntry getEntry() {
				if (entry == null) {
					entry = jobContext.get(job.getContextKey());
				}
				return entry;
			}

			@Override
			public JobContextWriter getLocalJobContextWriter() {
				if (localWriter == null) {
					localWriter = getEntry().getLocalJobContextWriter();
				}
				return localWriter;
			}

			@Override
			public JobContextWriter getRootJobContextWriter() {
				if (rootWriter == null) {
					rootWriter = getEntry().getRootJobContextWriter();
				}
				return rootWriter;
			}

			@Override
			public JobContextData getSingleContextDataByName(String dataName) {
				return getSingleContextDataByTypeAndName(null, dataName);
			}

			@Override
			public List<JobContextData> getContextDataByName(String dataName) {
				return getContextDataByTypeAndName(null, dataName);
			}

			@Override
			public <T extends JobContextData> T getSingleContextDataByType(Class<T> type) {
				return getSingleContextDataByTypeAndName(type, null);
			}

			@Override
			public <T extends JobContextData> List<T> getContextDataByType(Class<T> type) {
				return getContextDataByTypeAndName(type, null);
			}

			@Override
			public <T extends JobContextData> T getSingleContextDataByTypeAndName(Class<T> type, String dataName) {
				List<T> l = getContextDataByTypeAndName(type, dataName);
				if (l.size() == 1) {
					return l.get(0);
				} else {
					return null;
				}
			}

			@Override
			public <T extends JobContextData> List<T> getContextDataByTypeAndName(Class<T> type, String dataName) {
				List<T> data = getContextDataByTypeAndNameInternal(getEntry(), type, dataName);
				return data == null ? Collections.EMPTY_LIST : data;
			}

			private <T extends JobContextData> List<T> getContextDataByTypeAndNameInternal(JobContextEntry jobContextEntry, Class<T> type, String dataName) {
				List<T> data = null;
				List<JobContextData> items = jobContextEntry.getEntryData();
				if (items != null) {
					for (JobContextData jobContextData : items) {
						if (type == null || type.isAssignableFrom(jobContextData.getClass())) {
							if (dataName == null || dataName.equals(jobContextData.getName())) {
								if (data == null) {
									data = new ArrayList<>();
								}
								data.add((T) jobContextData);
							}
						}
					}
				}
				JobContextEntry parent = jobContextEntry.getParent();
				if (parent != null) {
					List<T> parentData = getContextDataByTypeAndNameInternal(parent, type, dataName);
					if (parentData != null) {
						if (data == null) {
							data = new ArrayList<>();
						}
						data.addAll(parentData);
					}
				}
				return data;
			}

			@Override
			public <T extends JobContextData> T getFirstContextDataByTypeAndName(Class<T> type, String dataName) {
				List<T> data = getContextDataByTypeAndName(type, dataName);
				if (data.isEmpty()) {
					return null;
				}
				return data.get(0);
			}

			@Override
			public JobExecution schedulePublicationForPath(String path) {
				if (path != null && !path.isEmpty() && !pathsToPublish.contains(path)) {
					pathsToPublish.add(path);
				}
				return this;
			}
			
		};
	}

	@Override
	public void performSynchronization(final Iterable<Record> recordsToSynchronize) {
		final List<ImporterJob> jobs = new ArrayList<>();
		try (ImportScheduler importScheduler = createImportScheduler(jobs)) {
			jobContextProvider.inMemoryContext(new JobContextProvider.VoidCallback() {
				@Override
				public void doWithContext(final JobContext context) {
					// call the synchronization strategies and perform the jobs right away
					RecordContext recordContext = null;
					for (Record record : recordsToSynchronize) {
						if (recordContext == null) {
							recordContext = record.getContext();
						}
						// writes to the wrong job context
						importScheduler.scheduleSynchronization(record, context);
					}

					if (recordContext == null) {
						return;
					}
					final Date startedOn = new Date();
					final RecordContext finalRecordContext = recordContext;
					final Set<String> pathsToPublish = new LinkedHashSet<>();
					cmsDao.run(new CmsDao.JCRSessionCallback<Object>() {
						@Override
						public Object doInJCRSession(final Session session) throws RepositoryException {
							final ContentService contentService = new ContentServiceImpl(session, cmsDao.getConfiguration(), publicationService);
							ImportPlan plan = null; // we do not need an import plan
							// now execute those jobs
							for (ImporterJob importerJob : jobs) {
								JobExecution execution = createJobExecution(contentService, importerJob, context, finalRecordContext, pathsToPublish);
								handleImporterJob(session, importerJob, plan, startedOn, execution);
							}
							return null;
						}

					});
				}
				
			});
		} catch (Exception e) {
			LOG.error("failed to schedule full synchronization: " + e.getMessage(), e);
		}
	}
	

}
