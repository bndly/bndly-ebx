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

import org.bndly.common.mapper.MapperFactory;
import org.bndly.common.reflection.BeanPropertyAccessor;
import org.bndly.common.reflection.GetterBeanPropertyAccessor;
import org.bndly.ebx.model.Invoice;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.Shipment;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.beans.ebx.InvoiceRestBean;
import org.bndly.rest.beans.ebx.PurchaseOrderRestBean;
import org.bndly.rest.beans.ebx.ShipmentRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Date;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Path("PurchaseOrder")
@Component(service = PurchaseOrderResource.class, immediate = true)
public class PurchaseOrderResource {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference(target = "(service.pid=org.bndly.common.mapper.MapperFactory.ebx)")
	private MapperFactory mapperFactory;
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
	@Path("{id}/shipment")
	@AtomLink(rel = "shipment", target = PurchaseOrderRestBean.class)
	public Response findShipment(final @PathParam("id") long id) {
		return findOrderRelatedObject(id, Shipment.class, ShipmentRestBean.class);
	}

	@GET
	@Path("{id}/invoice")
	@AtomLink(rel = "invoice", target = PurchaseOrderRestBean.class)
	public Response findInvoice(final @PathParam("id") long id) {
		return findOrderRelatedObject(id, Invoice.class, InvoiceRestBean.class);
	}

	public Response findOrderRelatedObject(final long orderId, Class<?> relatedType, Class<?> relatedRestBeanType) {
		RecordContext ctx = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		PurchaseOrder order = schemaBeanFactory.getSchemaBean(PurchaseOrder.class, ctx.create(PurchaseOrder.class.getSimpleName(), orderId));
		List<Record> shipments = schemaBeanFactory.getEngine().getAccessor().queryByExample(relatedType.getSimpleName(), ctx).eager().attribute("orderNumber", order.getOrderNumber()).all();

		BeanPropertyAccessor accessor = new GetterBeanPropertyAccessor();
		Object result = null;
		Date date = null;
		for (Record record : shipments) {
			Object candidate = schemaBeanFactory.getSchemaBean(relatedType, record);
			Date d = (Date) accessor.get("date", candidate);
			if (d != null) {
				if (date == null) {
					date = d;
					result = candidate;
				} else if (d.getTime() > date.getTime()) {
					result = candidate;
				}
			}
		}
		if (result == null) {
			return Response.status(404);
		}
		return Response.ok(mapperFactory.buildContext().map(result, relatedRestBeanType));
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setMapperFactory(MapperFactory mapperFactory) {
		this.mapperFactory = mapperFactory;
	}
}
