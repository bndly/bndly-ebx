package org.bndly.business.api;

/*-
 * #%L
 * org.bndly.ebx.bpm-tasks
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

import org.bndly.common.bpm.api.TaskExecutor;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public interface TaskDelegationRegistry {
	public static TaskDelegationRegistry INSTANCE = null;
	public void registerTaskExecutor(TaskExecutor taskExecutor);
	public void unregisterTaskExecutor(TaskExecutor taskExecutor);
	public TaskExecutor getTaskExecutorByName(String name);
}
