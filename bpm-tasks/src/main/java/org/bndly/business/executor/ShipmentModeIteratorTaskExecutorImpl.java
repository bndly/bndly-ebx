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
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.ebx.model.ShipmentMode;
import org.bndly.schema.api.Pagination;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import java.util.Iterator;
import java.util.List;

public class ShipmentModeIteratorTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    @ProcessVariable
    private Long shipmentModeIndex;
    @ProcessVariable(ProcessVariable.Access.WRITE)
    private ShipmentMode shipmentMode;

	@Override
    public void run() {
		RecordContext ctx = contextResolver.getContext(RecordContext.class);
		if(ctx == null) {
			ctx = engine.getAccessor().buildRecordContext();
		}
        Long shipmentModeCount = engine.getAccessor().count("COUNT "+ShipmentMode.class.getSimpleName());
        if (shipmentModeIndex == null) {
            shipmentModeIndex = 0L;
        }

        if (shipmentModeCount != null && shipmentModeIndex < shipmentModeCount) {
			Iterator<Record> records = engine.getAccessor().query("PICK "+ShipmentMode.class.getSimpleName()+" LIMIT ? OFFSET ?",ctx, null, 1L, shipmentModeIndex);
            if (records.hasNext()) {
                shipmentMode = schemaBeanFactory.getSchemaBean(ShipmentMode.class, records.next());
                if (shipmentModeIndex == shipmentModeCount - 1) {
                    shipmentModeIndex = null;
                } else {
                    shipmentModeIndex++;
                }
            } else {
                noMoreEntries();
            }
        } else {
            noMoreEntries();
        }
    }

    private void noMoreEntries() {
        shipmentModeIndex = null;
        shipmentMode = null;
    }
}
