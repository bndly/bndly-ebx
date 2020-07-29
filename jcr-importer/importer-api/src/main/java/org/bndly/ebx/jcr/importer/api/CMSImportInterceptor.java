package org.bndly.ebx.jcr.importer.api;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-api
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

import org.bndly.ebx.model.ImporterJob;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import java.util.List;

public interface CMSImportInterceptor {
	public static interface JobListener {

		void createdJob(ImporterJob job);
	}

	List<Class<?>> listSchemaBeanTypesWithSynchronizationStrategy();

	void scheduleImportOfRecord(Record record, Transaction transaction, JobListener listener);
	
	void scheduleImportOfRecord(Record record, Transaction transaction, JobListener listener, JobContext jobContext);
}
