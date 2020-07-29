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

import org.bndly.common.reflection.ReflectionUtil;
import org.bndly.common.service.model.api.ContentAware;
import org.bndly.common.service.model.api.ContentID;
import org.bndly.common.service.model.api.ContentValue;
import org.bndly.ebx.client.service.api.ContentMergeMediator;
import org.bndly.ebx.client.service.api.ContentService;
import org.bndly.ebx.client.service.graph.ContentMergeGraphListener.ContentMergeContext;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentMergeGraphListener extends NoOpGraphListener<ContentMergeContext> {

    private ContentService contentService;
    private Map<Class<?>, ContentMergeMediator> mediators;

    private ContentMergeMediator getMediatorForType(Class<?> contentType) {
        ContentMergeMediator mediator = mediators.get(contentType);
        if(mediator == null) {
            Class<?>[] interfaces = contentType.getInterfaces();
            for (Class<?> class1 : interfaces) {
                mediator = getMediatorForType(class1);
                if(mediator != null) {
                    break;
                }
            }
        }
        if(!contentType.isInterface()) {
            Class<?> superType = contentType.getSuperclass();
            if(superType != null) {
                mediator = getMediatorForType(superType);
            }
        }
        return mediator;
    }
    
    @Override
    public void onStart(Object bean, ContentMergeContext context) {
        context.setContentService(contentService);
    }

    @Override
    public void onVisitValue(Object value, Field field, Object bean, ContentMergeContext context) {
        ContentValue cv = field.getAnnotation(ContentValue.class);
        if (value == null || (cv != null && cv.onNonNull())) {
            if (bean.getClass().isAnnotationPresent(ContentAware.class)) {
                ContentID cid = context.getContentID(bean);
                if (cid != null) {
                    if (cv != null) {
                        Object contentBean = context.getContent(cid);
                        if (contentBean != null) {
                            String propertyName = cv.value();
                            if ("".equals(propertyName)) {
                                propertyName = field.getName();
                            }
                            ContentMergeMediator mediator = getMediatorForType(contentBean.getClass());
                            // get these from a new annotation
                            Field keyField = null;
                            Object keyValue = null;
                            Object contentValue = mediator.resolveContentProperty(contentBean, propertyName, keyField, keyValue, field.getType());
                            if (contentValue != null && field.getType().isInstance(contentValue)) {
                                ReflectionUtil.setFieldValue(field, contentValue, bean);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Class<ContentMergeContext> getIterationContextType() {
        return ContentMergeContext.class;
    }

    public static class ContentMergeContext {

        private Map<Class<?>, Field> cidFields = new HashMap<Class<?>, Field>();
        private Map<ContentID, Object> content = new HashMap<ContentID, Object>();
        private ContentService contentService;

        private ContentID getContentID(Object bean) {
            Class<?> type = bean.getClass();
            if (!cidFields.containsKey(type)) {
                Field cidField = null;
                List<Field> fields = ReflectionUtil.getFieldsOfAssignableType(ContentID.class, type);
                if (fields != null && fields.size() == 1) {
                    cidField = fields.get(0);
                }
                cidFields.put(type, cidField);
            }
            Field cidField = cidFields.get(type);
            if (cidField != null) {
                return (ContentID) ReflectionUtil.getFieldValue(cidField, bean);
            } else {
                return null;
            }
        }

        private Object getContent(ContentID id) {
            if (!content.containsKey(id)) {
                Object contentBean = contentService.getContentBeanById(id);
                content.put(id, contentBean);
            }

            return content.get(id);
        }

        public void setContentService(ContentService contentService) {
            this.contentService = contentService;
        }
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
    
    public void setMediators(List<ContentMergeMediator> mediators) {
        this.mediators = new HashMap<Class<?>, ContentMergeMediator>();
        for (ContentMergeMediator contentMergeMediator : mediators) {
            Class contentType = contentMergeMediator.appliesForContentType();
            this.mediators.put(contentType, contentMergeMediator);
        }
    }
}
