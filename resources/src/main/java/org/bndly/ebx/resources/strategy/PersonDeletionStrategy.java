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

import org.bndly.ebx.model.AddressAssignment;
import org.bndly.ebx.model.PaymentDetailAssignment;
import org.bndly.schema.api.DeletionStrategy;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.model.NamedAttributeHolderAttribute;
import org.bndly.schema.model.Type;
import java.util.List;

public class PersonDeletionStrategy implements DeletionStrategy {

    private DelegatingTypeDeletionStrategy delegatingTypeDeletionStrategy;
    private Engine engine;
    private List<AttributeAssociation> referencedBy;
    
    @Override
    public void delete(Record record, Transaction transaction) {
        for (AttributeAssociation attributeAssociation : referencedBy) {
            NamedAttributeHolderAttribute att = attributeAssociation.getAttribute();
            Type holder = attributeAssociation.getType();
            
            if(holder.getName().equals(PaymentDetailAssignment.class.getSimpleName())) {
                CascadingDeletionStrategy.delete(record, att, holder, engine, delegatingTypeDeletionStrategy, transaction);

            } else if(holder.getName().equals(AddressAssignment.class.getSimpleName())) {
                CascadingDeletionStrategy.delete(record, att, holder, engine, delegatingTypeDeletionStrategy, transaction);
            }
            // other references to the person will not be deleted automatically.
        }
        Record fullRec = engine.getAccessor().queryByExample(record.getType().getName(), record.getContext()).eager().attribute("id", record.getId()).single();
        Record addressRec = fullRec.getAttributeValue("address", Record.class);
        if(addressRec != null) {
            delegatingTypeDeletionStrategy.delete(addressRec,transaction);
        }
        engine.getAccessor().delete(record,transaction);
    }

    public void setReferencedBy(List<AttributeAssociation> referencedBy) {
        this.referencedBy = referencedBy;
    }

    public void setDelegatingTypeDeletionStrategy(DelegatingTypeDeletionStrategy delegatingTypeDeletionStrategy) {
        this.delegatingTypeDeletionStrategy = delegatingTypeDeletionStrategy;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
    
}
