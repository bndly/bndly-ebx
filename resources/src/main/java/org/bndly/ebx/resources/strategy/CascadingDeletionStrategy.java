package org.bndly.ebx.resources.strategy;

/*-
 * #%L
 * org.bndly.ebx.resources
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

import org.bndly.schema.api.DeletionStrategy;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.api.services.QueryByExample;
import org.bndly.schema.model.NamedAttributeHolderAttribute;
import org.bndly.schema.model.Type;
import java.util.List;

public class CascadingDeletionStrategy extends TypeSpecificDeletionStrategy implements DeletionStrategy {

    private DelegatingTypeDeletionStrategy delegatingTypeDeletionStrategy;
    
    public CascadingDeletionStrategy(Type type, List<AttributeAssociation> referencedBy) {
        super(type, referencedBy);
    }

    @Override
    protected void handle(Record record, NamedAttributeHolderAttribute att, Type refrencingType, Transaction transaction) {
        delete(record, att, refrencingType, engine, delegatingTypeDeletionStrategy, transaction);
    }
    
    public static void delete(Record record, NamedAttributeHolderAttribute att, Type refrencingType, Engine engine, DelegatingTypeDeletionStrategy delegatingTypeDeletionStrategy, Transaction transaction) {
        QueryByExample q = engine.getAccessor().queryByExample(refrencingType.getName(), record.getContext());
//        Record lazyRec = engine.getAccessor().lazyRecord(record.getType().getName(), record.getId());
        long id = record.getId();
        if(att.getNamedAttributeHolder() != record.getType()) {
            // this could be faster, when not making an id conversion in advance.
            // it would be faster if the id conversion is delayed into the database.
            id = engine.getAccessor().readIdAsNamedAttributeHolder(att.getNamedAttributeHolder(), record.getType(), id, record.getContext());
        }
        q.attribute(att.getName(), id);
        List<Record> refs = q.all();
        if(refs != null) {
            for (Record referencingRecord : refs) {
                delegatingTypeDeletionStrategy.delete(referencingRecord, transaction);
            }
        }
    }

    public void setDelegatingTypeDeletionStrategy(DelegatingTypeDeletionStrategy delegatingTypeDeletionStrategy) {
        this.delegatingTypeDeletionStrategy = delegatingTypeDeletionStrategy;
    }
    
}
