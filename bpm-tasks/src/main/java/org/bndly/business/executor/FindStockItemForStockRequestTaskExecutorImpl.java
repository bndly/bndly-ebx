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
import org.bndly.ebx.model.StockItem;
import org.bndly.ebx.model.StockRequest;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.exception.EmptyResultException;

public class FindStockItemForStockRequestTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    @ProcessVariable(ProcessVariable.Access.READ)
    private StockRequest stockRequest;
    
    @ProcessVariable(ProcessVariable.Access.WRITE)
    private StockItem stockItem;
            
    public void run() {
        Record si = null;
        try {
            si = engine.getAccessor().queryByExample(StockItem.class.getSimpleName(), engine.getAccessor().buildRecordContext()).attribute("sku", stockRequest.getSku()).single();
        } catch(EmptyResultException e) {
        }
        
        if(si == null) {
            stockItem = null;
        } else {
            stockItem = schemaBeanFactory.getSchemaBean(StockItem.class, si);
        }
    }
}
