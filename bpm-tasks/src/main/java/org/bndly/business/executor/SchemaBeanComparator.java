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

import org.bndly.business.util.ValueHolder;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordAttributeIterator;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Attribute;
import org.bndly.schema.model.InverseAttribute;
import java.util.Comparator;

public class SchemaBeanComparator implements Comparator<Object> {

    private final SchemaBeanFactory schemaBeanFactory;

    public SchemaBeanComparator(SchemaBeanFactory schemaBeanFactory) {
        this.schemaBeanFactory = schemaBeanFactory;
    }
    
    @Override
    public int compare(Object a, Object b) {
        final Record ra = schemaBeanFactory.getRecordFromSchemaBean(a);
        final Record rb = schemaBeanFactory.getRecordFromSchemaBean(b);
        if(ra.getType() == rb.getType()) {
            return 1;
        }
        
        final ValueHolder<Boolean> equals = new ValueHolder<Boolean>();
        equals.setValue(true);
        ra.iterateValues(new RecordAttributeIterator() {

            @Override
            public void handleAttribute(Attribute attribute, Record record) {
                if(!equals.getValue()) {
                    return;
                }
                if(attribute.isVirtual()) {
                    return;
                }
                if(InverseAttribute.class.isInstance(attribute)) {
                    return;
                }
                
                boolean presentInA = ra.isAttributePresent(attribute.getName());
                boolean presentInB = rb.isAttributePresent(attribute.getName());
                if(presentInA != presentInB) {
                    equals.setValue(false);
                    return;
                }
                if(presentInA) {
                    Object valueA = ra.getAttributeValue(attribute.getName());
                    Object valueB = rb.getAttributeValue(attribute.getName());
                    if(valueA != valueB) {
                        if(valueA != null) {
                            if(Comparable.class.isInstance(valueA)) {
                                Comparable ca = (Comparable) valueA;
                                Comparable cb = (Comparable) valueB;
                                if(ca.compareTo(cb) != 0) {
                                    equals.setValue(false);
                                }
                            } else {
                                if(schemaBeanFactory.isSchemaBean(valueA)) {
                                    if(new SchemaBeanComparator(schemaBeanFactory).compare(valueA, valueB) != 0) {
                                        equals.setValue(false);
                                    }
                                } else {
                                    equals.setValue(false);
                                }
                            }
                        }
                    }
                }
            }
        });
        
        if (equals.getValue()) {
            return 0;
        }
        return 1;
    }
}
