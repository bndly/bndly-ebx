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

import org.bndly.ebx.jcr.importer.api.JobBuilder;
import org.bndly.ebx.jcr.importer.api.JobContextEntry;
import org.bndly.ebx.jcr.importer.api.JobContextWriter;
import org.bndly.ebx.jcr.importer.api.JobHolder;
import org.bndly.ebx.model.CreateContentJob;
import org.bndly.ebx.model.DeleteContentJob;
import org.bndly.ebx.model.FindContentJob;
import org.bndly.ebx.model.GenericContentJob;
import org.bndly.ebx.model.ImporterJob;
import org.bndly.ebx.model.UpdateContentJob;
import org.bndly.ebx.model.UploadBlobJob;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class JobHolderImpl<E> implements JobHolder<E>{

	private final JobBuilderImpl jobBuilder;
	private final E jobInstance;
	private final JobContextEntry jobContextEntry;
	private final JobHolderImpl parent;

	public JobHolderImpl(JobBuilderImpl jobBuilder, E jobInstance, JobContextEntry jobContextEntry, JobHolderImpl parent) {
		this.jobBuilder = jobBuilder;
		this.jobInstance = jobInstance;
		this.jobContextEntry = jobContextEntry;
		this.parent = parent;
	}

	public JobHolderImpl(JobBuilderImpl jobBuilder, E jobInstance, JobContextEntry jobContextEntry) {
		this.jobBuilder = jobBuilder;
		this.jobInstance = jobInstance;
		this.jobContextEntry = jobContextEntry;
		this.parent = null;
	}

	@Override
	public JobContextEntry getJobContextEntry() {
		return jobContextEntry;
	}
	
	@Override
	public JobBuilder getJobBuilder() {
		return jobBuilder;
	}

	@Override
	public E getJobInstance() {
		return jobInstance;
	}

	@Override
	public JobContextWriter getLocalJobContextWriter() {
		return jobContextEntry.getLocalJobContextWriter();
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
	public <E extends ImporterJob> JobHolder<E> newJob(Class<E> jobInterface, String jobName) {
		return JobBuilderImpl.newJob(jobInterface, jobName, jobContextEntry.getKey(), jobBuilder);
	}
	
}
