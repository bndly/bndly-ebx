package org.bndly.ebx.jcr.importer.impl;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-impl
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

//import com.coremedia.cap.content.Content;
import org.bndly.common.reflection.BeanPropertyAccessor;
import org.bndly.common.reflection.GetterBeanPropertyAccessor;
import org.bndly.ebx.jcr.importer.api.CMSSynchronized;

public abstract class AbstractContentMapper<E> {

    private BeanPropertyAccessor accessor = new GetterBeanPropertyAccessor();
    
//    @Override
    public Object getLocalId(E entity) {
        CMSSynchronized annotation = entity.getClass().getAnnotation(CMSSynchronized.class);
        String idProperty = annotation.idProperty();
        return accessor.get(idProperty, entity);
    }
    
    //protected void weakLink(Content content, String propertyName, E entity/*, ContentSynchronizationContext<Content> context*/) {
//        Field field = ReflectionUtil.getFieldByName(propertyName, entity.getClass());
//        if(field == null) {
//            throw new ContentSynchronizationException("could not find field "+propertyName+" in "+entity.getClass().getSimpleName());
//        }
//        Object value = ReflectionUtil.getFieldValue(field, entity);
//        CMSRelation cmsRelationAnnotation = field.getAnnotation(CMSRelation.class);
//        boolean isWeaklyReferenced = cmsRelationAnnotation.weak();
//        if(!isWeaklyReferenced) {
//            throw new ContentSynchronizationException("property "+propertyName+" in "+entity.getClass().getSimpleName()+" is not weakly referenced.");
//        }
//        
//        String referenceProperty = cmsRelationAnnotation.referenceProperty();
//        
//        if("".equals(referenceProperty)) {
//            CMSSynchronized cmsSynchronizedAnnotation = entity.getClass().getAnnotation(CMSSynchronized.class);
//            String localIdProperty = cmsSynchronizedAnnotation.idProperty();
//            referenceProperty = propertyName+"_"+localIdProperty;
//        }
//        
//        if(value != null) {
//            content.set(referenceProperty, getLocalId(entity));
//        } else {
//            content.set(referenceProperty, null);
//        }
//        CapPropertyDescriptor contentPropertyDescriptor = content.getType().getDescriptor(referenceProperty);
//        CapPropertyDescriptorType propertyType = contentPropertyDescriptor.getType();
//        // now i can so some magic >:D
//        if(propertyType.equals(CapPropertyDescriptorType.STRING)) {
//            // convert the getLocalId() result to a string
//        }
    //}
    
   // protected void link(Content content, String propertyName, Object relatedEntity/*, ContentSynchronizationContext<Content> context*/) {
//        List<Content> list = new ArrayList<Content>();
//        if(Collection.class.isInstance(relatedEntity)) {
//            Collection c = (Collection) relatedEntity;
//            for (Object object : c) {
//                Content relatedEntityContent = context.get(object);
//                list.add(relatedEntityContent);
//            }
//        } else {
//            try {
//                Content relatedEntityContent = context.get(relatedEntity);
//                list.add(relatedEntityContent);
//            } catch(ContentSynchronizationException e) {
//                // if the related content can not be found yet
//                context.unprocessedLink(content, propertyName, relatedEntity);
//            }
//        }
//        content.set(propertyName, list);
    //}
    
}
