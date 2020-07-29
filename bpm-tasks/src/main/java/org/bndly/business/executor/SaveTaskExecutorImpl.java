package org.bndly.business.executor;

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

import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.common.bpm.annotation.ProcessVariable.Access;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.ebx.model.Country;

public class SaveTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    @ProcessVariable(Access.READ)
    private Country foo;
    @ProcessVariable
    private Country foo2;

    @Override
    public void run() {
        if (foo2 == null) {
            // because foo2 is defined as READ_WRITE it will be written to the process once this executor has finished
            foo2 = foo;
        }
    }
}
