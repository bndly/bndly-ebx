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
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.Purchasable;
import org.bndly.schema.api.Record;
import java.util.Iterator;

public class FindProductByNumberTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    @ProcessVariable(Access.READ)
    private String sku;
    @ProcessVariable(Access.READ)
    private PriceRequest priceRequest;
    @ProcessVariable(Access.WRITE)
    private Purchasable purchasableItem;

    @Override
    public void run() {
        if (sku == null) {
            sku = priceRequest.getSku();
        }
        if(sku == null) {
            purchasableItem = null;
            return;
        }
		Iterator<Record> r = engine.getAccessor().query("PICK "+Purchasable.class.getSimpleName()+" p IF p.sku=? LIMIT ?", schemaBeanFactory.getRecordFromSchemaBean(priceRequest).getContext(), null, sku, 1);
		if(r.hasNext()) {
			purchasableItem = schemaBeanFactory.getSchemaBean(Purchasable.class, r.next());
		} else {
			purchasableItem = null;
		}
    }
}
