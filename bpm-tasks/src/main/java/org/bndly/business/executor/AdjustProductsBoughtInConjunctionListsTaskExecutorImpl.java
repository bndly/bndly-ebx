package org.bndly.business.executor;

/*-
 * #%L
 * org.bndly.ebx.bpm-tasks
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

import org.bndly.business.util.RecordReferenceUtil;
import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.ExternalObject;
import org.bndly.ebx.model.ExternalObjectCentricExternalObjectList;
import org.bndly.ebx.model.ExternalObjectListAssocation;
import org.bndly.ebx.model.ExternalObjectListType;
import org.bndly.ebx.model.LineItem;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.exception.EmptyResultException;
import org.bndly.schema.beans.ActiveRecord;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjustProductsBoughtInConjunctionListsTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    private static final String LIST_TYPE_NAME = "PRODUCTS_BOUGHT_IN_CONJUNCTION";
    @ProcessVariable(ProcessVariable.Access.READ)
    private CheckoutRequest checkoutRequest;
    private Map<String, ExternalObject> externalObjectsByProductUUID;

	@Override
    public void run() {
        RecordContext ctx = schemaBeanFactory.getRecordFromSchemaBean(checkoutRequest).getContext();
        PurchaseOrder order = checkoutRequest.getOrder();
        ExternalObjectListType listType;
        Record typeRecord = null;
        try {
            typeRecord = engine.getAccessor().queryByExample(ExternalObjectListType.class.getSimpleName(), ctx).attribute("name", LIST_TYPE_NAME).single();
        } catch(EmptyResultException e) {
        }
        if (typeRecord == null) {
            listType = schemaBeanFactory.getSchemaBean(ExternalObjectListType.class, ctx.create(ExternalObjectListType.class.getSimpleName()));
            listType.setName(LIST_TYPE_NAME);
            ((ActiveRecord) listType).persist();
        } else {
            listType = schemaBeanFactory.getSchemaBean(ExternalObjectListType.class, typeRecord);
        }
        if (listType == null) {
            throw new IllegalStateException("listType '" + LIST_TYPE_NAME + "' does not exist.");
        }
//        ResourceHandlingContext ctx = new ResourceHandlingContextImpl();
        externalObjectsByProductUUID = new HashMap<>();
        List<LineItem> items = order.getItems();
        if (items != null) {
            for (LineItem lineItem : items) {
                if (lineItem.getProductUUID() != null) {
                    handleItem(lineItem, items, listType, ctx);
                }
            }
        }
    }

    private void handleItem(LineItem lineItem, List<LineItem> items, ExternalObjectListType listType, RecordContext ctx/*, ResourceHandlingContext ctx*/) {
        ExternalObject owner = getExternalObjectForLineItemFromContext(lineItem, ctx);
        ExternalObjectCentricExternalObjectList list = getOrCreateObjectListForOwner(owner, listType, ctx);
        // load the assocations of the list
        List<Record> assocRecords = engine.getAccessor().queryByExample(ExternalObjectListAssocation.class.getSimpleName(), ctx)
                .attribute("list", RecordReferenceUtil.createRecordIdReference(list, schemaBeanFactory))
                .eager()
                .all();

        List<ExternalObjectListAssocation> assocs = schemaBeanFactory.getSchemaBeans(assocRecords, ExternalObjectListAssocation.class);
        for (LineItem item : items) {
            if (item != lineItem) {
                if (item.getProductUUID() != null) {
                    ExternalObject extObject = getExternalObjectForLineItemFromContext(item, ctx);
                    addExternalObjectToList(extObject, list, assocs, ctx);
                }
            }
        }
        for (ExternalObjectListAssocation externalObjectListAssocation : assocs) {
            ActiveRecord ar = (ActiveRecord)externalObjectListAssocation;
            if(ar.getId() == null) {
                ar.persist();
            } else {
                ar.update();
            }
        }
    }

    private ExternalObject getExternalObjectForLineItemFromContext(LineItem lineItem, RecordContext ctx/*, ResourceHandlingContext ctx*/) {
        String productUUID = lineItem.getProductUUID();
        if (!externalObjectsByProductUUID.containsKey(productUUID)) {
            // get it from or create it in the DB
            ExternalObject eo;
            Record o = null;
            try {
                o = engine.getAccessor().queryByExample(ExternalObject.class.getSimpleName(), ctx).attribute("identifier", productUUID).single();
            } catch(EmptyResultException e) {
            }
            if (o == null) {
                eo = schemaBeanFactory.getSchemaBean(ExternalObject.class, ctx.create(ExternalObject.class.getSimpleName()));
                eo.setIdentifier(productUUID);
                ((ActiveRecord) eo).persist();
            } else {
                eo = schemaBeanFactory.getSchemaBean(ExternalObject.class, o);
            }
            externalObjectsByProductUUID.put(productUUID, eo);
        }
        return externalObjectsByProductUUID.get(productUUID);
    }

    private ExternalObjectCentricExternalObjectList getOrCreateObjectListForOwner(ExternalObject owner, ExternalObjectListType listType, RecordContext ctx) {
        Record ownerRec = RecordReferenceUtil.createRecordIdReference(owner, schemaBeanFactory);
        Record listTypeRec = RecordReferenceUtil.createRecordIdReference(listType, schemaBeanFactory);
        Record listRec = null;
        try {
            listRec = engine.getAccessor().queryByExample(ExternalObjectCentricExternalObjectList.class.getSimpleName(), ctx)
                    .attribute("type", listTypeRec)
                    .attribute("externalObject", ownerRec)
                    .single();
        }catch(EmptyResultException e) {
        }
        ExternalObjectCentricExternalObjectList list;
        if (listRec == null) {
            list = schemaBeanFactory.getSchemaBean(ExternalObjectCentricExternalObjectList.class, ctx.create(ExternalObjectCentricExternalObjectList.class.getSimpleName()));
            list.setExternalObject(owner);
            list.setType(listType);
            ((ActiveRecord)list).persist();
        } else {
            list = schemaBeanFactory.getSchemaBean(ExternalObjectCentricExternalObjectList.class, listRec);
        }
        if (list == null) {
            throw new IllegalStateException("list '" + listType.getName() + "' for external object '" + owner.getIdentifier() + "' could not be read or created.");
        }
        return list;
    }

    private void addExternalObjectToList(ExternalObject externalObject, ExternalObjectCentricExternalObjectList list, List<ExternalObjectListAssocation> assocs, RecordContext ctx) {
        ExternalObjectListAssocation assoc = null;
        for (ExternalObjectListAssocation externalObjectListAssociation : assocs) {
            if (externalObjectListAssociation.getExternalObject().getIdentifier().equals(externalObject.getIdentifier())) {
                assoc = externalObjectListAssociation;
                break;
            }
        }
        if (assoc == null) {
            assoc = schemaBeanFactory.getSchemaBean(ExternalObjectListAssocation.class, ctx.create(ExternalObjectListAssocation.class.getSimpleName()));
            assoc.setCreatedOn(new Date());
            assoc.setList(list);
            assoc.setExternalObject(externalObject);
            assocs.add(assoc);
        } else {
            assoc.setUpdatedOn(new Date());
        }
        BigDecimal qty = assoc.getQuantity();
        if (qty == null) {
            qty = BigDecimal.ONE;
        } else {
            qty = qty.add(BigDecimal.ONE);
        }
        assoc.setQuantity(qty);
    }

}
