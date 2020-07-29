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
import org.bndly.common.service.model.api.PropertiesField;
import org.bndly.common.service.model.api.PropertyField;
import org.bndly.common.service.model.api.ReferableResource;
import org.bndly.ebx.model.Property;
import org.bndly.ebx.model.PropertySet;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyToObjectMappingUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PropertyToObjectMappingUtil.class);
	
    private PropertyToObjectMappingUtil() {
    }
    
    public static List<PropertyKey> transferPropertiesToTargetDomainModel(PropertySet sourceDomainModel, Object targetDomainModel) {
        if (sourceDomainModel == null) {
            return null;
        }
        List<PropertyKey> mappedList = new ArrayList<>();
        List<Field> fields;
        try {
            fields = PropertyUtils.getPropertiesFieldList(targetDomainModel.getClass());
            List<Field> propertyFields = PropertyUtils.getPropertyFieldList(targetDomainModel.getClass());
            if (fields != null) {
                fields.addAll(propertyFields);
            }
            List<PropertyKey> buildPathOfPropertyFieldPaths = PropertyUtils.buildPathOfPropertiesFieldOrPropertyFields(targetDomainModel.getClass(), fields);
            for (Property property : sourceDomainModel.getProperties()) {
                PropertyKey targetProperty = PropertyUtils.findPropertyKeyByPath(buildPathOfPropertyFieldPaths, property.getName());
                Field targetField = PropertyUtils.findTargetFieldByPropertyKey(targetProperty);
                if (targetProperty != null && targetField != null) {
                    if (targetField.isAnnotationPresent(PropertiesField.class)) {
                        Class listType = targetField.getType();
                        if (Collection.class.isAssignableFrom(listType)) {
                            if (ReflectionUtil.isCollectionFieldFillableWithObjectsInheritedOfType(PropertyKey.class, targetField)) {
                                mappedList = (List<PropertyKey>) ReflectionUtil.getFieldValue(targetField, targetDomainModel);
                                //targetProperty.setProperty(property.buildReference());
                                targetProperty.setProperty(property);
                                targetProperty.setValue(property.getActualValue());
                                mappedList.add(targetProperty);
                                ReflectionUtil.setFieldValue(targetField, mappedList, targetDomainModel);
                            }
                        }
                    } else if (targetField.isAnnotationPresent(PropertyField.class)) {
                        Class fieldType = targetField.getType();
                        //if target field ar not an collection!
                        if (!Collection.class.isAssignableFrom(fieldType)) {
                            targetProperty.setProperty(property);
                            targetProperty.setValue(property.getActualValue());
                            PropertyUtils.setPropertyValue(targetField, property.getActualValue(), fieldType, targetDomainModel);
                            mappedList.add(targetProperty);
                        }
                    }
                }
            }
        } catch (InstantiationException e) {
			LOG.error("failed to instantiate while transfering properties to target domain model: "+e.getMessage(), e);
        }
        return mappedList;
    }

    public static void transferPropertiesToSourceDomainModel(List<PropertyKey> sourceDomainModels, PropertySet targetDomainModel) {
        for (PropertyKey source : sourceDomainModels) {
            Property property = source.getProperty();
			if(ReferableResource.class.isInstance(property)) {
				if (!((ReferableResource)property).isResourceReference()) {
					if ("".equals(property.getName())) {
						property.setName(source.getPath());
					}
					property.setActualValue(source.getValue());
				}
			}
            targetDomainModel.getProperties().add(property);
        }
    }
    
}
