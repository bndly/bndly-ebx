package org.bndly.ebx.client.service.impl;

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

import org.bndly.common.reflection.GetterBeanPropertyAccessor;
import org.bndly.common.reflection.ReflectionUtil;
import org.bndly.common.service.model.api.ContentAware;
import org.bndly.common.service.model.api.ContentID;
import org.bndly.common.service.model.api.ContentValue;
import org.bndly.ebx.client.service.api.ContentService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContentServiceImpl implements ContentService {

    @Override
    public List<Object> getContentBeanListByIds(ContentID... contentIds) {
        return getContentBeanListByIds(Object.class, contentIds);
    }

    @Override
    public <E> List<E> getContentBeanListByIds(Class<E> type, ContentID... contentIds) {
        List<E> resultList = new ArrayList<E>();
        if (contentIds != null && contentIds.length > 0) {
            for (ContentID contentId : contentIds) {
                Object contentBean = getContentBeanById(contentId);
                if (null != contentBean) {
                    if (type.isAssignableFrom(contentBean.getClass())) {
                        resultList.add(type.cast(contentBean));
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public <E> E getContentBeanById(Class<E> type, ContentID contentId) {
        List<E> result = getContentBeanListByIds(type, contentId);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * returns the list of objects, that have been removed, because their
     * content could not be found.
     *
     * @param objects a collection of objects, that shall be merged with the
     * content.
     * @return null if no objects have been removed. otherwise the removed
     * objects
     */
    @Override
    public List<Object> mergeObjectsWithContent(List<Object> objects) {
        List<Object> removedObjects = null;
        if (objects != null) {
            List<Object> defensiveCopy = new ArrayList<Object>(objects);
            for (Object object : defensiveCopy) {
                Class<?> objectType = object.getClass();
                ContentAware ca = objectType.getAnnotation(ContentAware.class);
                if (ca != null) {
                    List<Field> contentIDFields = ReflectionUtil.getFieldsOfAssignableType(ContentID.class, object);
                    if (contentIDFields != null) {
                        if (contentIDFields.size() == 1) {
                            Field contentIDField = contentIDFields.get(0);
                            ContentID id = (ContentID) ReflectionUtil.getFieldValue(contentIDField, object);
                            if (id != null && id.getValue() != null) {
                                Object content = getContentBeanById(Object.class, id);
                                if (content != null) {
                                    List<Field> conentValueFields = ReflectionUtil.getFieldsWithAnnotation(ContentValue.class, objectType);
                                    if (conentValueFields != null) {
                                        for (Field field : conentValueFields) {
                                            ContentValue cv = field.getAnnotation(ContentValue.class);
                                            String contentProperty = cv.value();
                                            if ("".equals(contentProperty)) {
                                                contentProperty = field.getName();
                                            }
                                            Object contentPropertyValue = new GetterBeanPropertyAccessor().get(contentProperty, content);
                                            if (contentPropertyValue != null) {
                                                if (!field.getType().isAssignableFrom(contentPropertyValue.getClass())) {
                                                    throw new IllegalStateException("can't merge content into " + objectType.getSimpleName() + " because content property " + contentProperty + " is of type " + contentPropertyValue.getClass().getSimpleName() + " but " + field.getType().getSimpleName() + " is required.");
                                                }
                                            }
                                            ReflectionUtil.setFieldValue(field, contentPropertyValue, object);
                                        }
                                    }
                                } else {
                                    // if no content exists, the object will be removed
                                    if (ca.autoRemove()) {
                                        if (removedObjects == null) {
                                            removedObjects = new ArrayList<Object>();
                                        }
                                        objects.remove(object);
                                        removedObjects.add(object);
                                    }
                                }
                            }
                        } else {
                            throw new IllegalStateException("found multiple contentIDs in " + objectType.getSimpleName());
                        }
                    }
                }
            }
        }
        return removedObjects;
    }
}
