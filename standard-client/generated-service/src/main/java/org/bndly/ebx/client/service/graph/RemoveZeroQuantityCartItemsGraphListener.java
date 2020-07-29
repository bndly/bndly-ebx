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

import org.bndly.ebx.model.CartItem;
import java.lang.reflect.Field;
import java.util.Collection;

public class RemoveZeroQuantityCartItemsGraphListener extends NoOpGraphListener<Object> {

    @Override
    public void beforeVisitReferenceInCollection(Object object, Collection c, Field field, Object bean, Object context) {
        if(CartItem.class.isInstance(object)) {
            CartItem ci = (CartItem)object;
            if(ci.getQuantity() <= 0) {
                c.remove(object);
            }
        }
    }
    
    @Override
    public Class<Object> getIterationContextType() {
        return Object.class;
    }
    
}
