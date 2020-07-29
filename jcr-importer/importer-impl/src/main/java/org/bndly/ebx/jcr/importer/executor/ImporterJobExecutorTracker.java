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

import org.bndly.ebx.jcr.importer.api.ImporterJobExecutor;
import org.bndly.ebx.jcr.importer.api.JobExecution;
import org.bndly.ebx.jcr.importer.api.JobExecutionException;
import org.bndly.ebx.model.ImporterJob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class ImporterJobExecutorTracker extends ServiceTracker<ImporterJobExecutor, ImporterJobExecutor>{

	private static final Logger LOG = LoggerFactory.getLogger(ImporterJobExecutorTracker.class);
	private final Map<Class<? extends ImporterJob>, List<ImporterJobExecutor>> executorsByJobType = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public ImporterJobExecutorTracker(BundleContext context) {
		super(context, ImporterJobExecutor.class, null);
	}

	@Override
	public ImporterJobExecutor addingService(ServiceReference<ImporterJobExecutor> reference) {
		ImporterJobExecutor executor = super.addingService(reference);
		lock.writeLock().lock();
		try {
			List<ImporterJobExecutor> l = executorsByJobType.get(executor.getSupportedJobType());
			if (l == null) {
				l = new ArrayList<>();
				executorsByJobType.put(executor.getSupportedJobType(), l);
			}
			l.add(executor);
		} finally {
			lock.writeLock().unlock();
		}
		return executor;
	}

	@Override
	public void removedService(ServiceReference<ImporterJobExecutor> reference, ImporterJobExecutor executor) {
		lock.writeLock().lock();
		try {
			List<ImporterJobExecutor> l = executorsByJobType.get(executor.getSupportedJobType());
			if (l != null) {
				Iterator<ImporterJobExecutor> iterator = l.iterator();
				while (iterator.hasNext()) {
					ImporterJobExecutor next = iterator.next();
					if (next == executor) {
						iterator.remove();
					}
				}
				if (l.isEmpty()) {
					executorsByJobType.remove(executor.getSupportedJobType());
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		super.removedService(reference, executor);
	}
	
	public ImporterJobExecutor createExecutorForJob(final ImporterJob importerJob) {
		final List<ImporterJobExecutor> res = new ArrayList<>();
		lock.readLock().lock();
		try {
			for (Map.Entry<Class<? extends ImporterJob>, List<ImporterJobExecutor>> entrySet : executorsByJobType.entrySet()) {
				Class<? extends ImporterJob> executorJobType = entrySet.getKey();
				List<ImporterJobExecutor> executorImplementations = entrySet.getValue();
				if (executorJobType.isInstance(importerJob)) {
					res.addAll(executorImplementations);
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		
		return new ImporterJobExecutor() {

			@Override
			public Class getSupportedJobType() {
				return importerJob.getClass();
			}

			@Override
			public void execute(ImporterJob job, JobExecution jobExecution) throws JobExecutionException {
				if (res == null || res.isEmpty()) {
					job.setNote("no executors for job found.");
					LOG.error("could not find executor for job: " + job.getContextKey());
					return ;
				}
				for (ImporterJobExecutor implementation : res) {
					implementation.execute(job, jobExecution);
				}
			}
		};
	}
}
