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

import org.bndly.ebx.model.WishList;
import java.lang.reflect.Field;
import java.util.UUID;

public class AddMissingSecurityTokenGraphListener extends NoOpGraphListener<Object> {

    @Override
    public void onVisitValue(Object value, Field field, Object bean, Object context) {
		if(WishList.class.isInstance(bean)) {
			if(field.getName().equals("securityToken")) {
				if(value == null) {
					((WishList)bean).setSecurityToken(UUID.randomUUID().toString());
				}
			}
		}
    }
    
    
    @Override
    public Class<Object> getIterationContextType() {
        return Object.class;
    }
    
}
