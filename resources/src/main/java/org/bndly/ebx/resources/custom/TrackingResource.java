package org.bndly.ebx.resources.custom;

/*-
 * #%L
 * org.bndly.ebx.resources
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

import org.bndly.ebx.model.ExternalObject;
import org.bndly.ebx.model.ExternalObjectCentricExternalObjectList;
import org.bndly.ebx.model.ExternalObjectList;
import org.bndly.ebx.model.ExternalObjectListAssocation;
import org.bndly.ebx.model.ExternalObjectListType;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.UserCentricExternalObjectList;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.api.StatusWriter;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinks;
import org.bndly.rest.beans.ebx.ExternalObjectCentricExternalObjectListListRestBean;
import org.bndly.rest.beans.ebx.UserCentricExternalObjectListListRestBean;
import org.bndly.rest.beans.ebx.misc.ExternalObjectTrackingRestBean;
import org.bndly.rest.beans.ebx.misc.UserTrackingRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.QueryParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = TrackingResource.class, immediate = true)
@Path("ebx")
public class TrackingResource {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;

	@Activate
	public void activate() {
		controllerResourceRegistry.deploy(this);
	}

	@Deactivate
	public void deactivate() {
		controllerResourceRegistry.undeploy(this);
	}

	@GET
	@Path("tracking")
	public Response track(
			@QueryParam("rootExternalObjectIdentifier") String rootExternalObjectIdentifier, 
			@QueryParam("userIdentifier") String userIdentifier, 
			@QueryParam("externalObjectIdentifier") String externalObjectIdentifier, 
			@QueryParam("listTypeName") String listTypeName, 
			@Meta Context context
	) {
		if (externalObjectIdentifier != null && listTypeName != null) {
			if(userIdentifier != null) {
				return trackUserInternal(userIdentifier, externalObjectIdentifier, listTypeName, context);
			} else if(rootExternalObjectIdentifier != null) {
				return trackExternalObjectInternal(rootExternalObjectIdentifier, externalObjectIdentifier, listTypeName, context);
			} else {
				return Response.status(StatusWriter.Code.BAD_REQUEST.getHttpCode());
			}
		} else {
			return Response.status(StatusWriter.Code.BAD_REQUEST.getHttpCode());
		}
	}
	
	@POST
	@Path("UserCentricExternalObjectList/track")
	@AtomLinks({
		@AtomLink(rel = "trackFast", target = UserCentricExternalObjectListListRestBean.class)
	})
	public Response trackUser(UserTrackingRestBean payload, @Meta Context context) {
		String userIdentifier = payload.getUserIdentifier();
		String externalObjectIdentifier = payload.getExternalObjectIdentifier();
		String listTypeName = payload.getListTypeName();
		if (userIdentifier != null && externalObjectIdentifier != null && listTypeName != null) {
			return trackUserInternal(userIdentifier, externalObjectIdentifier, listTypeName, context);
		} else {
			return Response.status(StatusWriter.Code.BAD_REQUEST.getHttpCode());
		}
	}
	
	private Response trackUserInternal(String userIdentifier, String externalObjectIdentifier, String listTypeName, Context context) {
		// do the tracking here.
		Engine engine = schemaBeanFactory.getEngine();
		Accessor accessor = engine.getAccessor();
		final User user;
		final ExternalObject externalObject;
		final ExternalObjectListType externalObjectListType;
		UserCentricExternalObjectList list = null;
		Transaction tx = engine.getQueryRunner().createTransaction();
		RecordContext recordContext = accessor.buildRecordContext();

		user = getUserByIdentifier(accessor, tx, userIdentifier, recordContext);
		externalObject = getExternalObjectByIdentifier(accessor, tx, externalObjectIdentifier, recordContext);
		externalObjectListType = getExternalObjectListTypeByName(accessor, tx, listTypeName, recordContext);

		if (!isUnpersisted(user) && !isUnpersisted(externalObjectListType)) {
			// look for the list. maybe it exists
			Iterator<Record> userListRec = accessor.query("PICK " + UserCentricExternalObjectList.class.getSimpleName() + " ul IF ul.user.id=? AND ul.type.id=? LIMIT ?", recordContext, null, getId(user), getId(externalObjectListType), 1);
			if (userListRec.hasNext()) {
				list = schemaBeanFactory.getSchemaBean(UserCentricExternalObjectList.class, userListRec.next());
			}
		}
		if (list == null) {
			// create the list, because it didn't exist
			list = schemaBeanFactory.getSchemaBean(UserCentricExternalObjectList.class, recordContext.create(UserCentricExternalObjectList.class.getSimpleName()));
			list.setUser(user);
			list.setType(externalObjectListType);
			((ActiveRecord) list).persist(tx);
		}
		addToList(accessor, tx, UserCentricExternalObjectList.class, list, externalObject);
		tx.commit();
		String schemaName = engine.getDeployer().getDeployedSchema().getName();
		ResourceURI resourceUri = context.createURIBuilder().pathElement(schemaName).pathElement(UserCentricExternalObjectList.class.getSimpleName()).pathElement(((ActiveRecord) list).getId().toString()).build();
		String uri = resourceUri.asString();
		return Response.seeOther(uri);
	}

	@POST
	@Path("ExternalObjectCentricExternalObjectList/track")
	@AtomLinks({
		@AtomLink(rel = "trackFast", target = ExternalObjectCentricExternalObjectListListRestBean.class)
	})
	public Response trackExternalObject(ExternalObjectTrackingRestBean payload, @Meta Context context) {
		String rootExternalObjectIdentifier = payload.getRootExternalObjectIdentifier();
		String externalObjectIdentifier = payload.getExternalObjectIdentifier();
		String listTypeName = payload.getListTypeName();
		if (rootExternalObjectIdentifier != null && externalObjectIdentifier != null && listTypeName != null) {
			return trackExternalObjectInternal(rootExternalObjectIdentifier, externalObjectIdentifier, listTypeName, context);
		} else {
			return Response.status(StatusWriter.Code.BAD_REQUEST.getHttpCode());
		}
	}
	
	private Response trackExternalObjectInternal(String rootExternalObjectIdentifier, String externalObjectIdentifier, String listTypeName, Context context) {
		// do the tracking here.
		Engine engine = schemaBeanFactory.getEngine();
		Accessor accessor = engine.getAccessor();
		final ExternalObject root;
		final ExternalObject externalObject;
		final ExternalObjectListType externalObjectListType;
		ExternalObjectCentricExternalObjectList list = null;

		Transaction tx = engine.getQueryRunner().createTransaction();

		RecordContext recordContext = accessor.buildRecordContext();
		root = getExternalObjectByIdentifier(accessor, tx, rootExternalObjectIdentifier, recordContext);
		externalObject = getExternalObjectByIdentifier(accessor, tx, externalObjectIdentifier, recordContext);
		externalObjectListType = getExternalObjectListTypeByName(accessor, tx, listTypeName, recordContext);
		if (!isUnpersisted(root) && !isUnpersisted(externalObjectListType)) {
			// look for the list. maybe it exists
			Iterator<Record> externalObjectListRec = accessor.query("PICK " + ExternalObjectCentricExternalObjectList.class.getSimpleName() + " el IF el.externalObject.id=? AND el.type.id=? LIMIT ?", recordContext, null, getId(root), getId(externalObjectListType), 1);
			if (externalObjectListRec.hasNext()) {
				list = schemaBeanFactory.getSchemaBean(ExternalObjectCentricExternalObjectList.class, externalObjectListRec.next());
			}
		}
		if (list == null) {
			// create the list, because it didn't exist
			list = schemaBeanFactory.getSchemaBean(ExternalObjectCentricExternalObjectList.class, recordContext.create(ExternalObjectCentricExternalObjectList.class.getSimpleName()));
			list.setExternalObject(root);
			list.setType(externalObjectListType);
			((ActiveRecord) list).persist(tx);
		}
		addToList(accessor, tx, ExternalObjectCentricExternalObjectList.class, list, externalObject);

		tx.commit();
		String schemaName = engine.getDeployer().getDeployedSchema().getName();
		ResourceURI resourceUri = context.createURIBuilder().pathElement(schemaName).pathElement(ExternalObjectCentricExternalObjectList.class.getSimpleName()).pathElement(((ActiveRecord) list).getId().toString()).build();
		String uri = resourceUri.asString();
		return Response.seeOther(uri);
	}

	private boolean isUnpersisted(Object schemaBean) {
		return ((ActiveRecord) schemaBean).getId() == null;
	}

	private long getId(Object schemaBean) {
		return ((ActiveRecord) schemaBean).getId(); // yes, i want a nullpointer when the is id is null. this case would not be allowed.
	}

	private <E extends ExternalObjectList> void addToList(Accessor accessor, Transaction tx, Class<E> listJavaType, E list, ExternalObject externalObject) {
		Record listRecord = schemaBeanFactory.getRecordFromSchemaBean(list);
		RecordContext recordContext = listRecord.getContext();
		ExternalObjectListAssocation assoc = null;
		if (!isUnpersisted(list) && !isUnpersisted(externalObject)) {
			// look for the association
			Iterator<Record> assocRec = accessor.query("PICK " + ExternalObjectListAssocation.class.getSimpleName() + " a IF a.externalObject.id=? AND a.list.id=? AND a.list TYPED ? LIMIT ?", recordContext, null, getId(externalObject), getId(list), listJavaType.getSimpleName(), 1);
			if (assocRec.hasNext()) {
				assoc = schemaBeanFactory.getSchemaBean(ExternalObjectListAssocation.class, assocRec.next());
			}
		}
		if (assoc == null) {
			;
			assoc = schemaBeanFactory.getSchemaBean(ExternalObjectListAssocation.class, recordContext.create(ExternalObjectListAssocation.class.getSimpleName()));
			assoc.setCreatedOn(new Date());
			assoc.setExternalObject(externalObject);
			assoc.setList(list);
			assoc.setQuantity(BigDecimal.ONE);
			((ActiveRecord) assoc).persist(tx);
		} else {
			assoc.setUpdatedOn(new Date());
			assoc.setExternalObject(externalObject);
			assoc.setList(list);
			BigDecimal q = assoc.getQuantity();
			q = q == null ? BigDecimal.ONE : q.add(BigDecimal.ONE);
			assoc.setQuantity(q);
			((ActiveRecord) assoc).update(tx);
		}
	}

	private ExternalObject getExternalObjectByIdentifier(Accessor accessor, Transaction tx, String identifier, RecordContext recordContext) {
		Iterator<Record> userRec = accessor.query("PICK " + ExternalObject.class.getSimpleName() + " u IF u.identifier=? LIMIT ?", recordContext, null, identifier, 1);
		ExternalObject externalObject;
		if (userRec.hasNext()) {
			externalObject = schemaBeanFactory.getSchemaBean(ExternalObject.class, userRec.next());
		} else {
			externalObject = schemaBeanFactory.getSchemaBean(ExternalObject.class, recordContext.create(ExternalObject.class.getSimpleName()));
			externalObject.setIdentifier(identifier);
			((ActiveRecord) externalObject).persist(tx);
		}
		return externalObject;
	}
	
	private User getUserByIdentifier(Accessor accessor, Transaction tx, String userIdentifier, RecordContext recordContext) {
		Iterator<Record> userRec = accessor.query("PICK " + User.class.getSimpleName() + " u IF u.identifier=? LIMIT ?", recordContext, null, userIdentifier, 1);
		User user;
		if (userRec.hasNext()) {
			user = schemaBeanFactory.getSchemaBean(User.class, userRec.next());
		} else {
			user = schemaBeanFactory.getSchemaBean(User.class, recordContext.create(User.class.getSimpleName()));
			user.setIdentifier(userIdentifier);
			((ActiveRecord) user).persist(tx);
		}
		return user;
	}

	private ExternalObjectListType getExternalObjectListTypeByName(Accessor accessor, Transaction tx, String name, RecordContext recordContext) {
		Iterator<Record> userRec = accessor.query("PICK " + ExternalObjectListType.class.getSimpleName() + " lt IF lt.name=? LIMIT ?", recordContext, null, name, 1);
		ExternalObjectListType externalObjectListType;
		if (userRec.hasNext()) {
			externalObjectListType = schemaBeanFactory.getSchemaBean(ExternalObjectListType.class, userRec.next());
		} else {
			externalObjectListType = schemaBeanFactory.getSchemaBean(ExternalObjectListType.class, recordContext.create(ExternalObjectListType.class.getSimpleName()));
			externalObjectListType.setName(name);
			((ActiveRecord) externalObjectListType).persist(tx);
		}
		return externalObjectListType;
	}
}
