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

import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.ebx.model.Address;
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.LineItem;
import org.bndly.ebx.model.OrderShipment;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.Shipment;
import org.bndly.ebx.model.ShipmentItem;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class GenerateShipmentTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {
	
	@ProcessVariable(ProcessVariable.Access.READ)
	private CheckoutRequest checkoutRequest;
	
	@ProcessVariable(ProcessVariable.Access.WRITE)
	private Shipment shipment;
	
	@Override
	public void run() {
		PurchaseOrder order = checkoutRequest.getOrder();
		Record recordFromSchemaBean = schemaBeanFactory.getRecordFromSchemaBean(order);
		RecordContext context = recordFromSchemaBean.getContext();
		shipment = createShipment(order, context);
		List<OrderShipment> orderShipments = createOrderShipments(order, shipment, context);
		((ActiveRecord) shipment).persistCascaded();
		for (OrderShipment orderShipment : orderShipments) {
			((ActiveRecord) orderShipment).persist();
		}

		// set the shipment number
		GregorianCalendar calNow = new GregorianCalendar();
		calNow.setTime(shipment.getDate());
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(calNow.get(GregorianCalendar.YEAR), calNow.get(GregorianCalendar.MONTH), calNow.get(GregorianCalendar.DATE), 0, 0, 0);
		Date leftDate = cal.getTime();
		
		// there might be some date rounding issues here.
		Long value = engine.getAccessor().count("COUNT "+Shipment.class.getSimpleName()+" s IF s.date INRANGE ?,?", leftDate, shipment.getDate());
		cal = new GregorianCalendar();
		cal.setTime(shipment.getDate());
		int year = cal.get(Calendar.YEAR) % 100;
		int month = cal.get(Calendar.MONTH) + 1;
		StringBuffer sb = new StringBuffer("LS-");
		if (year < 10) {
			sb.append('0');
		}
		sb.append(year);
		if (month < 10) {
			sb.append('0');
		}
		sb.append(month);
		sb.append('/');
		sb.append(value);
		shipment.setNumber(sb.toString());
		((ActiveRecord) shipment).update();
	}
	
	private Shipment createShipment(PurchaseOrder order, RecordContext recordContext) {
		shipment = createBeanInContext(Shipment.class, recordContext);
		shipment.setDate(new Date());
		shipment.setOrderDate(order.getOrderDate());
		shipment.setOrderNumber(order.getOrderNumber());
		Person orderer = order.getOrderer();
		Address dest = order.getDeliveryAddress();
		if (dest == null && orderer != null) {
			dest = order.getAddress();
		}
		shipment.setAddress(dest);
		
		return shipment;
	}
	
	private List<OrderShipment> createOrderShipments(PurchaseOrder order, Shipment shipment, RecordContext recordContext) {
		List<OrderShipment> orderShipments = new ArrayList<>();
		List<ShipmentItem> shipmentItems = new ArrayList<>();
		
		long pos = 1;
		for (LineItem lineItem : order.getItems()) {
			ShipmentItem shipmentItem = createBeanInContext(ShipmentItem.class, recordContext);
			
			shipmentItem.setArticleName(lineItem.getProductName());
			shipmentItem.setArticleId(lineItem.getSku());
			shipmentItem.setQuantity(lineItem.getQuantity());
			shipmentItem.setShipment(shipment);
			shipmentItem.setPos(pos);
			pos++;
			shipmentItems.add(shipmentItem);
			
			OrderShipment orderShipment = createBeanInContext(OrderShipment.class, recordContext);
			orderShipment.setLineItem(lineItem);
			orderShipment.setShipmentItem(shipmentItem);
			orderShipment.setQuantity(lineItem.getQuantity());
			
			orderShipments.add(orderShipment);
		}
		
		shipment.setShipmentItems(shipmentItems);
		
		return orderShipments;
	}
	
}
