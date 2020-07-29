package org.bndly.ebx.client.service.property;

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
import org.bndly.common.service.model.api.AbstractEntity;
import org.bndly.common.service.model.api.PropertiesField;
import org.bndly.common.service.model.api.PropertyField;
import org.bndly.common.service.model.api.PropertyRootNode;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtils {

	private static final Logger LOG = LoggerFactory.getLogger(PropertyUtils.class);
    private static final String SEPARATOR = ".";
    private static final Map<PropertyKey, Field> foundFields = new HashMap<>();

    public static List<PropertyKey> buildPathOfPropertiesFieldAnnotation(Class<?> model) throws InstantiationException {
        List<Field> propertyFields = getPropertiesFieldList(model);
        return buildPathOfPropertiesFieldOrPropertyFields(model, propertyFields);
    }

    public static List<PropertyKey> buildPathOfPropertyFieldAnnotation(Class<?> model) throws InstantiationException {
        List<Field> propertyFields = getPropertyFieldList(model);
        return PropertyUtils.buildPathOfPropertiesFieldOrPropertyFields(model, propertyFields);
    }

    public static String buildPathForField(Class<?> model, String fieldName) {
        String path = PropertyUtils.buildPath(model);
        path += "." + fieldName;

        return path;
    }

    public static String buildPath(Class<?> modelType) {
        StringBuffer sb = new StringBuffer();
        _buildPath(modelType, sb);
        return sb.toString();
    }

    private static String _buildPath(Class<?> modelType, StringBuffer sb) {
        if(modelType != null) {
            Class<?> superType = modelType.getSuperclass();
            String sep = _buildPath(superType, sb);
            sb.append(sep);
            PropertyRootNode rootNode = modelType.getAnnotation(PropertyRootNode.class);
            if(rootNode != null) {
                String pathSegment = rootNode.value();
                if ("".equals(pathSegment)) {
                    pathSegment = modelType.getSimpleName();
                }
                sb.append(pathSegment);
                return SEPARATOR;
            }
        }
        return "";
    }

    public static List<PropertyKey> buildPathOfPropertiesFieldOrPropertyFields(Class<?> modelType, List<Field> propertyFields) throws InstantiationException {
        String rootPath = buildPath(modelType);

        if (propertyFields != null && !propertyFields.isEmpty()) {

            List<PropertyKey> propertyFieldPaths = new ArrayList<>(propertyFields.size());
            for (Field field : propertyFields) {
                PropertiesField propertiesField = field.getAnnotation(PropertiesField.class);
                PropertyField propertyField = field.getAnnotation(PropertyField.class);
                if (propertiesField != null) {
                    PropertyField[] pathArray = propertiesField.value();
                    List<PropertyField> pathList = Arrays.asList(pathArray);
                    for (PropertyField propertyField0 : pathList) {
                        String p = getPathName(propertyField0, field);
                        PropertyKey propertyKey = new PropertyKey(rootPath + "." + p, null);
                        foundFields.put(propertyKey, field);
                        propertyFieldPaths.add(propertyKey);
                    }
                } else if (propertyField != null) {
                    String p = getPathName(propertyField, field);
                    PropertyKey propertyKey = new PropertyKey(rootPath + "." + p, null);
                    foundFields.put(propertyKey, field);
                    propertyFieldPaths.add(propertyKey);
                }
            }

            return propertyFieldPaths;
        }

        return Collections.EMPTY_LIST;
    }

    public static Object getObjectOfClass(Class<?> model) throws InstantiationException {

        Object modelInstance = null;
        if (!Modifier.isAbstract(model.getModifiers())) {
            try {
                modelInstance = model.newInstance();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            throw new InstantiationException("Couldn't instantiate abstract class: " + model.getSimpleName());
        }

        return modelInstance;
    }

    public static List<Field> getFieldsAsList(Class<?> model, Class annotation) throws InstantiationException {
        if (annotation == null) {
            return new ArrayList<>();
        }
        Object modelInstance = PropertyUtils.getObjectOfClass(model);
        List<Field> propertyFields = ReflectionUtil.getFieldsWithAnnotation(annotation, modelInstance);
        return propertyFields;

    }

    public static List<Field> getPropertiesFieldList(Class<?> model) throws InstantiationException {
        return getFieldsAsList(model, PropertiesField.class);
    }

    public static List<Field> getPropertyFieldList(Class<?> model) throws InstantiationException {
        return getFieldsAsList(model, PropertyField.class);
    }

    public static Field findTargetFieldByPropertyKey(PropertyKey propertyKey) {
        return foundFields.get(propertyKey);
    }

    public static Field findTargetFieldByPath(List<PropertyKey> propertyKeys, String path) {
        PropertyKey propertyKey = findPropertyKeyByPath(propertyKeys, path);

        if (propertyKey != null) {
            return findTargetFieldByPropertyKey(propertyKey);
        }

        return null;
    }

    public static boolean stringEquals(String a, String b) {
        if(a == b) {
            return true;
        } else {
            if(a == null || b == null) {
                return false;
            } else {
                return a.equals(b);
            }
        }
    }
    
    public static PropertyKey findPropertyKeyByPath(List<PropertyKey> propertyKeys, String path) {

        if (propertyKeys != null && !propertyKeys.isEmpty()) {
            for (PropertyKey propertyKey : propertyKeys) {
                if (stringEquals(propertyKey.getPath(), path)) {
                    return propertyKey;
                }
            }
        }

        return null;
    }

    public static <T extends Object, B extends T> B getPropertyValue(List<PropertyKey> propertyKeys, String path, Class<T> targetObjectClass) {

        PropertyKey field = PropertyUtils.findPropertyKeyByPath(propertyKeys, path);

        if (field != null && !stringEquals("", field.getValue())) {
            Method valueOf = null;
            try {
                valueOf = targetObjectClass.getMethod("valueOf", String.class);
            } catch (NoSuchMethodException e) {
                LOG.error("could not find valueOf method in "+targetObjectClass+" while getting a property value from path "+path);
            }

            if (valueOf != null) {
                try {
                    return (B) valueOf.invoke(null, field.getValue());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOG.error("failed to invoke valueOf method of "+targetObjectClass+" while getting a property value from path "+path);
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public static <T extends Object, B extends T, TARGETDOMAINMODEL extends AbstractEntity> void setPropertyValue(PropertyKey propertyKey, Class<T> targetObjectClass, TARGETDOMAINMODEL targetDomainModel) {

        Field field = null;

        if (propertyKey != null && !stringEquals("", propertyKey.getValue())) {

            field = findTargetFieldByPropertyKey(propertyKey);

            if (field != null) {
                setPropertyValue(field, propertyKey.getValue(), targetObjectClass, targetDomainModel);
            }
        }
    }

    public static <T extends Object, B extends T, TARGETDOMAINMODEL extends Object> void setPropertyValue(Field field, Object value, Class<T> targetObjectClass, TARGETDOMAINMODEL targetDomainModel) {
        B objectToSet = null;

        Method valueOf = null;
        try {
            valueOf = targetObjectClass.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            LOG.error("could not find valueOf method in "+targetObjectClass+" while setting a property value to field "+field);
        }

        if (valueOf != null) {
            try {
                objectToSet = (B) valueOf.invoke(null, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.error("failed to invoke valueOf method of "+targetObjectClass+" while setting a property value to field "+field);
            }
        }

        if (objectToSet != null && field != null) {
            ReflectionUtil.setFieldValue(field, objectToSet, targetDomainModel);
        }
    }

    public static void setPropertyValue(List<PropertyKey> propertyKeys, String path, String newValue) {

        PropertyKey field = PropertyUtils.findPropertyKeyByPath(propertyKeys, path);

        if (field != null && !stringEquals("", newValue)) {
            field.setValue(newValue);
        }
    }

    private static String getPathName(PropertyField propertyField, Field field) {
        String p = propertyField.value();
        if ("".equals(p)) {
            p = field.getName();
        }
        return p;
    }
}
