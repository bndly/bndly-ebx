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
import org.bndly.schema.model.NamedAttributeHolderAttribute;
import org.bndly.schema.model.Type;
import java.util.List;

public abstract class TypeSpecificDeletionStrategy implements DeletionStrategy {
    private final Type type;
    private final List<AttributeAssociation> referencedBy;
    protected Engine engine;
    
    public TypeSpecificDeletionStrategy(Type type, List<AttributeAssociation> referencedBy) {
        this.type = type;
        this.referencedBy = referencedBy;
    }

    public Type getType() {
        return type;
    }

    public List<AttributeAssociation> getReferencedBy() {
        return referencedBy;
    }

    @Override
    public final void delete(Record record, Transaction transaction) {
        if(referencedBy != null) {
            for (AttributeAssociation attributeAssociation : referencedBy) {
                // handle the attribute
                NamedAttributeHolderAttribute att = attributeAssociation.getAttribute();
                handle(record, att, attributeAssociation.getType(), transaction);
            }
        }
        engine.getAccessor().delete(record, transaction);
    }

    protected abstract void handle(Record record, NamedAttributeHolderAttribute att, Type refrencingType, Transaction transaction);
    
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
    
}
