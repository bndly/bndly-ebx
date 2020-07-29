package org.bndly.business.util;

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

import org.bndly.schema.api.Record;
import org.bndly.schema.beans.SchemaBeanFactory;

public final class RecordReferenceUtil {

    private RecordReferenceUtil() {
    }
    
    public static Record createRecordIdReference(Object schemaBean, SchemaBeanFactory schemaBeanFactory) {
        return createRecordIdReference(schemaBeanFactory.getRecordFromSchemaBean(schemaBean), schemaBeanFactory);
    }
    
    public static Record createRecordIdReference(Record input, SchemaBeanFactory schemaBeanFactory) {
        Class<?> beanType = schemaBeanFactory.getTypeBindingForType(input);
        Object copy = schemaBeanFactory.getSchemaBean(beanType, input.getContext().create(beanType.getSimpleName()));
        Record copyRecord = schemaBeanFactory.getRecordFromSchemaBean(copy);
        copyRecord.setId(input.getId());
        copyRecord.setIsReference(true);
        return copyRecord;
    }
}
