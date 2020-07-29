package org.bndly.ebx.client.impl;

/*-
 * #%L
 * org.bndly.ebx.client.spring-context
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
import org.bndly.ebx.client.service.api.ContentService;
import org.bndly.ebx.client.service.impl.AbstractContentServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestContentServiceImpl extends AbstractContentServiceImpl implements ContentService {

    private Map<ContentID, Object> content = new HashMap<ContentID, Object>();
    
    public void addContent(ContentID cid, Object contentObject) {
        content.put(cid, contentObject);
    }
    
    public Object getContent(ContentID cid) {
        return content.get(cid);
    }
    
    @Override
    public Object getContentBeanById(ContentID id) {
        return getContent(id);
    }

    @Override
    public List<String> getCategoryNamesOfProduct(ContentID contentId) {
        // i don't care
        return new ArrayList<String>();
    }
    
}
