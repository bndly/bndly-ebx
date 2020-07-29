package org.bndly.ebx.example;

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
import org.bndly.ebx.jcr.importer.api.JobBuilder;
import org.bndly.ebx.jcr.importer.api.JobHolder;
import org.bndly.ebx.model.CreateContentJob;
import org.bndly.ebx.model.GenericContentJob;
import org.bndly.ebx.model.Product;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class CouldBeAStrategy implements ContentSynchronizationStrategy<Product> {

	@Override
	public Class<Product> getEntityType() {
		return Product.class;
	}

	@Override
	public void created(Product entity, JobBuilder builder) {
		JobHolder<CreateContentJob> holder = builder.newCreateContentJob("nameOfTheJob");
		holder.getJobInstance().setContentName("nameofthenode");
		holder.getJobInstance().setTargetPath("/path/in/repo");
		holder.getJobInstance().setContentTypeName("nt:unstructured");
		holder.getLocalJobContextWriter().entity().schemaBean(entity).build();

		JobHolder<GenericContentJob> specialJob = builder.newJob(GenericContentJob.class, "somethingspecial");
		specialJob.getLocalJobContextWriter().string().var("myparam").stringValue("myparamvalue").build();
	}

	@Override
	public void updated(Product entity, JobBuilder builder) {
	}

	@Override
	public void deleted(Product entity, JobBuilder builder) {
	}
	
}
