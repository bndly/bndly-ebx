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

import org.bndly.common.reflection.BeanPropertyAccessor;
import org.bndly.common.reflection.UltimateBeanPropertyAccessor;
import org.bndly.ebx.client.service.api.ContentMergeMediator;
import java.lang.reflect.Field;

public class DefaultContentMergeMediator implements ContentMergeMediator<Object> {

    private BeanPropertyAccessor accessor = new UltimateBeanPropertyAccessor();
    
    public Class<?> appliesForContentType() {
        return Object.class;
    }

    public <T> Object resolveContentProperty(Object content, String propertyName, Field keyField, Object keyValue, Class<T> desiredType) {
        Object contentValue = accessor.get(propertyName, content);
        if (contentValue != null) {
            if(desiredType.isInstance(contentValue)) {
                return desiredType.cast(contentValue);
            }
        }
        return null;
    }
    
}
