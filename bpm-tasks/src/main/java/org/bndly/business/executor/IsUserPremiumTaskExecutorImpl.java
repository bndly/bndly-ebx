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

import org.bndly.business.util.RecordReferenceUtil;
import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.UserAttribute;
import org.bndly.ebx.model.UserAttributeValue;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import java.util.Iterator;
import java.util.List;

public class IsUserPremiumTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    @ProcessVariable(ProcessVariable.Access.READ)
    private User user;
    @ProcessVariable(ProcessVariable.Access.WRITE)
    private Boolean isPremiumUser;

	@Override
    public void run() {
        if (user == null) {
            isPremiumUser = false;
            return;
        }
		Record userRecord = schemaBeanFactory.getRecordFromSchemaBean(user);
        RecordContext ctx = userRecord.getContext();
		engine.getAccessor().query("PICK "+UserAttribute.class.getSimpleName()+" a IF a.name=? LIMIT ?", ctx, null, "GROUP", 1);
		Iterator<Record> valueRecords = engine.getAccessor().query("PICK "+UserAttributeValue.class.getSimpleName()+" v IF v.attribute.name=? AND v.user.id=?", ctx, null, "GROUP", userRecord.getId());
		while (valueRecords.hasNext()) {
			Record valueRecord = valueRecords.next();
			UserAttributeValue userAttributeValue = schemaBeanFactory.getSchemaBean(UserAttributeValue.class, valueRecord);
			if ("GOLD".equals(userAttributeValue.getStringValue())) {
				isPremiumUser = true;
				return;
			}
		}
        isPremiumUser = false;
    }
}
