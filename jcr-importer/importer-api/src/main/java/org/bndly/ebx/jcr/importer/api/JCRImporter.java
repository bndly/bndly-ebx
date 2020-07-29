/*
 * Copyright (c) 2012, cyber:con GmbH, Bonn.
 *
 * All rights reserved. This source file is provided to you for
 * documentation purposes only. No part of this file may be
 * reproduced or copied in any form without the written
 * permission of cyber:con GmbH. No liability can be accepted
 * for errors in the program or in the documentation or for damages
 * which arise through using the program. If an error is discovered,
 * cyber:con GmbH will endeavour to correct it as quickly as possible.
 * The use of the program occurs exclusively under the conditions
 * of the licence contract with cyber:con GmbH.
 */
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

import org.bndly.ebx.model.ImportPlan;

import org.bndly.schema.api.Record;

/**
 * The JCRImporter is the central service for the integration of eBX into JCR instances. The JCRImporter allows to start imports or inspect configuration values.
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public interface JCRImporter {

	void requestImport();

	boolean isEnabled();

	boolean isRunning();

	double getDone();

	void scheduleFullsynchronization();

	void requestFullsynchronization();

	ImportPlan createImportPlanWithScheduledJobs();

	ImportScheduler createImportScheduler();

	void scheduleSynchronization(Record record);

	public boolean isConnected();

	boolean establishTestConnection(int retries);

	public JCRImporterConfiguration getConfig();
	
	void performSynchronization(Iterable<Record> recordsToSynchronize);

}
