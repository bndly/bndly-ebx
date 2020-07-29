package org.bndly.ebx.client.service.graph;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service
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

import org.bndly.common.service.shared.ReferableResourceCollectionDetector;
import org.bndly.common.service.shared.ReferableResourceDetector;
import org.bndly.common.graph.BeanGraphIterator;
import org.bndly.common.graph.BeanGraphIteratorListener;
import org.bndly.common.graph.EntityCollectionDetector;
import org.bndly.common.graph.ReferenceDetector;
import org.bndly.common.reflection.InstantiationUtil;
import org.bndly.common.reflection.ReflectionUtil;
import org.bndly.common.service.model.api.CollectionFilter;
import org.bndly.common.service.model.api.FilterFunction;
import org.bndly.common.service.model.api.Identity;
import org.bndly.common.service.model.api.IdentityBuilder;
import org.bndly.common.service.shared.api.BeanPartialMerger;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class BeanPartialMergerImpl implements BeanPartialMerger {

    private ReferenceDetector referenceDetector = new ReferableResourceDetector();
    private EntityCollectionDetector collectionDetector = new ReferableResourceCollectionDetector("org.bndly.ebx.model");

    public static class PartialMergeContext {

        private Stack<Object> visitedObjects = new Stack<Object>();

        public Object current() {
            return visitedObjects.peek();
        }

        public void add(Object o) {
            visitedObjects.push(o);
        }

        public Object pop() {
            return visitedObjects.pop();
        }
    }
    
    private static class IdentityFilterFunction implements FilterFunction<Object> {

        final Identity identity;

        public IdentityFilterFunction(Identity identity) {
            this.identity = identity;
        }
                
        @Override
        public boolean applies(Object o) {
            return identity.appliesTo(o);
        }
        
    }

    @Override
    public <E> void mergePartialIntoTarget(E partial, final E target) {
        if (partial != null && target != null) {
            BeanGraphIteratorListener<PartialMergeContext> listener = new NoOpGraphListener<PartialMergeContext>() {
                @Override
                public Class<PartialMergeContext> getIterationContextType() {
                    return PartialMergeContext.class;
                }

                @Override
                public void onStart(Object bean, PartialMergeContext context) {
                    context.add(target);
                }

                @Override
                public void beforeVisitReference(Object value, Field field, Object bean, PartialMergeContext context) {
                    injectNewReferenceToTarget(context, field, value);
                }

                @Override
                public void afterVisitReference(Object value, Field field, Object bean, PartialMergeContext context) {
                    context.pop();
                }

                @Override
                public void beforeVisitCollection(Object value, Field field, Object bean, PartialMergeContext context) {
//                    injectNewReferenceToTarget(context, field, value);
                }

                @Override
                public void beforeVisitReferenceCollection(Object value, Field field, Object bean, PartialMergeContext context) {
					if(value != null) {
						injectNewReferenceToTarget(context, field, value);
					}
                }

                @Override
                public void beforeVisitReferenceInCollection(Object object, Collection c, Field field, Object bean, PartialMergeContext context) {
                    // get the collection from the current pendant in the context
                    Collection currentCollection = (Collection) context.current();

                    // look for the entry that shares the same identity (whatever makes up that identity)
                    final Identity partialIdentity = IdentityBuilder.buildOrIdentity(object);
                    if (partialIdentity == null) {
                        throw new IllegalStateException("could not build identity for " + object);
                    }
                    List<Object> pendantsInTheTargetCollection = new CollectionFilter<Object>().filter(currentCollection, new IdentityFilterFunction(partialIdentity));


                    Object pendant;
                    // if no object with the matching identity can be found in the target
                    if (pendantsInTheTargetCollection.isEmpty()) {
                        // add a copy of the currently visited collection entry
                        Object copyOfVisitedCollectionEntry = InstantiationUtil.instantiateType(object.getClass());
                        currentCollection.add(copyOfVisitedCollectionEntry);
                        pendant = copyOfVisitedCollectionEntry;
                    } else {
                        if (pendantsInTheTargetCollection.size() == 1) {
                            pendant = pendantsInTheTargetCollection.get(0);
                        } else {
                            throw new IllegalStateException("could not merge partial, because a partial had ambiguous counterparts in a target collection.");
                        }
                    }

                    // add the found or created target reference to the context, because it will be visited as well
                    context.add(pendant);
                }

                @Override
                public void afterVisitReferenceInCollection(Object object, Collection c, Field field, Object bean, PartialMergeContext context) {
                    context.pop();
                }

                @Override
                public void afterVisitReferenceCollection(Object value, Field field, Object bean, PartialMergeContext context) {
                    // remove all entries from the target collection that where not matched up to any value in the partial collection
					if(value != null) {
						Collection targetCollection = (Collection) context.pop();
						Collection itemsToRemove = new ArrayList();
						for (Object object : targetCollection) {
							final Identity identity = IdentityBuilder.buildOrIdentity(object);
							if (identity == null) {
								throw new IllegalStateException("could not build identity for " + object);
							}
							List<Object> pendantsInThePartialCollection = new CollectionFilter<Object>().filter((Collection)value, new IdentityFilterFunction(identity));
							if(pendantsInThePartialCollection == null || pendantsInThePartialCollection.isEmpty()) {
								// remove object from targetCollection
								itemsToRemove.add(object);
							}
						}
						if(!itemsToRemove.isEmpty()) {
							targetCollection.removeAll(itemsToRemove);
						}
					}
                }

                @Override
                public void onVisitValue(Object value, Field field, Object bean, PartialMergeContext context) {
                    if(value != null) {
                        injectValueToTarget(context, field, value, true);
                    }
                }

                private Object injectNewReferenceToTarget(PartialMergeContext context, Field field, Object value) {
                    Object valueToInsert = InstantiationUtil.instantiateType(value.getClass());
                    Object valueInCurrent = injectValueToTarget(context, field, valueToInsert, false);
                    context.add(valueInCurrent);
                    return valueInCurrent;
                }

                private Object injectValueToTarget(PartialMergeContext context, Field field, Object valueToInsert, boolean force) {
                    Object current = context.current();
                    Object valueInCurrent = ReflectionUtil.getFieldValue(field, current);
                    if (valueInCurrent == null || force) {
                        ReflectionUtil.setFieldValue(field, valueToInsert, current);
                        valueInCurrent = valueToInsert;
                    }
                    return valueInCurrent;
                }
            };
            new BeanGraphIterator(referenceDetector, collectionDetector, listener).traverse(partial);
        }
    }
}
