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

import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.PurchaseOrderCancelation;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.api.ResourceURIBuilder;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.beans.ebx.PurchaseOrderRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Date;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Path("PurchaseOrderCancelation")
@Component(service = PurchaseOrderCancelationResource.class, immediate = true)
public class PurchaseOrderCancelationResource {

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

	@POST
	@Path("order/{id}")
	@AtomLink(rel = "cancel", target = PurchaseOrderRestBean.class, constraint = "${this.getHasCancelations() == null || this.getHasCancelations() == false}")
	public Response cancel(final @PathParam("id") long id, @Meta Context context) {
		RecordContext ctx = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		PurchaseOrderCancelation cancelation = schemaBeanFactory.getSchemaBean(PurchaseOrderCancelation.class, ctx.create(PurchaseOrderCancelation.class.getSimpleName()));
		cancelation.setCreatedOn(new Date());

		Record rec = ctx.create(PurchaseOrder.class.getSimpleName(), id);
		rec.setIsReference(true);

		cancelation.setPurchaseOrder(schemaBeanFactory.getSchemaBean(PurchaseOrder.class, rec));
		ActiveRecord ar = (ActiveRecord) cancelation;
		ar.persist();
		ResourceURIBuilder builder = context.createURIBuilder();
		ResourceURI cancelationUri = builder.pathElement("PurchaseOrderCancelation").pathElement(ar.getId().toString()).build();
		return Response.created(cancelationUri.asString());
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
