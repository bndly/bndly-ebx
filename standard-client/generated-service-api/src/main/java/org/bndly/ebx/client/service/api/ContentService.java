package org.bndly.ebx.client.service.api;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service-api
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

import org.bndly.common.service.model.api.ContentID;
import java.util.List;

public interface ContentService {
    public static final String NAME = "shopCmsUtils";
    public List<Object> mergeObjectsWithContent(List<Object> objects);

    public <E> List<E> getContentBeanListByIds(Class<E> type, ContentID... contentIds);
    
    public List<Object> getContentBeanListByIds(ContentID... contentIds);

    public <E> E getContentBeanById(Class<E> type, ContentID contentId);

    public Object getContentBeanById(ContentID contentId);
    
    public List<String> getCategoryNamesOfProduct(ContentID contentId);
}
