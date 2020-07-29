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

import org.bndly.schema.api.AttributeMediator;
import org.bndly.schema.api.DeletionStrategy;
import org.bndly.schema.api.MediatorRegistry;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.db.AttributeColumn;
import org.bndly.schema.api.db.TypeTable;
import org.bndly.schema.api.query.Criteria;
import org.bndly.schema.api.query.Query;
import org.bndly.schema.api.query.QueryContext;
import org.bndly.schema.api.query.Update;
import org.bndly.schema.model.NamedAttributeHolder;
import org.bndly.schema.model.NamedAttributeHolderAttribute;
import org.bndly.schema.model.Type;
import java.util.List;

public class NullDeletionStrategy extends TypeSpecificDeletionStrategy implements DeletionStrategy {

    private MediatorRegistry mediatorRegistry;
    
    public NullDeletionStrategy(Type type, List<AttributeAssociation> referencedBy) {
        super(type, referencedBy);
    }

    @Override
    protected void handle(Record record, NamedAttributeHolderAttribute att, Type refrencingType, Transaction transaction) {
        QueryContext qc = engine.getQueryContextFactory().buildQueryContext();
        Update update = qc.update();
        TypeTable table = engine.getTableRegistry().getTypeTableByType(refrencingType);
        AttributeColumn col = null;
        for (AttributeColumn attributeColumn : table.getColumns()) {
            if(attributeColumn.getAttribute() == att) {
                col = attributeColumn;
                break;
            }
        }
        if(col == null) {
            throw new IllegalStateException("could not find column for attribute in type table");
        }
        update.table(table.getTableName());
        String columnName = col.getColumnName();
        AttributeMediator<NamedAttributeHolderAttribute> mediator = mediatorRegistry.getMediatorForAttribute(att);
        int sqlType = mediator.columnSqlType(att);
        update.setNull(columnName, sqlType);
        Criteria howToCompare = update.where().expression().and().criteria()
                         .field(columnName).equal();
        
        
        NamedAttributeHolder namedAttributeHolder = att.getNamedAttributeHolder();
        long idOfRecordInAttributeColumn = record.getId();
        if(namedAttributeHolder != record.getType()) {
            // this could be faster, when not making an id conversion in advance.
            // it would be faster if the id conversion is delayed into the database.
            idOfRecordInAttributeColumn = engine.getAccessor().readIdAsNamedAttributeHolder(namedAttributeHolder, record.getType(), record.getId(), record.getContext());
        }
        howToCompare.value(mediator.createPreparedStatementValueProvider(att, idOfRecordInAttributeColumn));
        
        Query q = qc.build(record.getContext());
		transaction.getQueryRunner().run(q);
//        engine.getQueryRunner().run(q);
    }

    public void setMediatorRegistry(MediatorRegistry mediatorRegistry) {
        this.mediatorRegistry = mediatorRegistry;
    }
}
