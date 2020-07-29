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

import org.bndly.common.service.model.api.PointInTime;
import org.bndly.ebx.client.service.graph.DateGraphListener.DateContext;
import java.util.Date;

public abstract class DateGraphListener extends NoOpGraphListener<DateContext>{

    @Override
    public Class<DateContext> getIterationContextType() {
        return DateContext.class;
    }

    private void applyCreateDate(Object bean, DateContext context) {
        if(bean != null) {
            applyDate(bean, context.getDate());
        }
    }
    
    protected abstract void applyDate(Object bean, PointInTime d);
    
    @Override
    public void onVisitReference(Object bean, DateContext context) {
        // every time a referenceable object is visited...
        applyCreateDate(bean, context);
    }

    public static class DateContext {
        private PointInTime date;

        public DateContext() {
            date = new PointInTime(new Date());
        }

        public PointInTime getDate() {
            return date;
        }
    }
}
