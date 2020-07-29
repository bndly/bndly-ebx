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
import org.bndly.ebx.model.Invoice;
import org.bndly.ebx.model.InvoiceItem;
import org.bndly.ebx.model.InvoiceTerm;
import org.bndly.ebx.model.InvoiceTermType;
import org.bndly.ebx.model.LineItem;
import org.bndly.ebx.model.OrderItemBilling;
import org.bndly.ebx.model.PaymentDetails;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.ShipmentOffer;
import org.bndly.ebx.model.TaxQuotaInfo;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordAttributeIterator;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.exception.EmptyResultException;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.model.Attribute;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenerateInvoiceTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

	@ProcessVariable(ProcessVariable.Access.READ)
	private CheckoutRequest checkoutRequest;

	@ProcessVariable(ProcessVariable.Access.WRITE)
	private Invoice invoice;

	public void run() {
		RecordContext ctx = schemaBeanFactory.getRecordFromSchemaBean(checkoutRequest).getContext();
		PurchaseOrder order = checkoutRequest.getOrder();
		generateInvoiceForOrder(order, ctx);
	}

	// für's Erste 1:1-Abbildung LineItem - InvoiceItem - OrderItemBilling
	private Invoice createInvoice(PurchaseOrder order, RecordContext ctx) {
		invoice = createBeanInContext(Invoice.class, ctx);
		invoice.setDate(new Date());
		invoice.setOrderDate(order.getOrderDate());
		invoice.setOrderNumber(order.getOrderNumber());
		final Address address = createBeanInContext(Address.class, ctx);
		Address ba = order.getBillingAddress();
		if (ba == null) {
			ba = order.getAddress();
		}

		invoice.setTotal(order.getMerchandiseValueGross());
		invoice.setGrandTotal(order.getTotalGross());
		invoice.setVat(order.getTotalTax());

		ShipmentOffer so = order.getShipmentOffer();
		if (so != null) {
			invoice.setPortage(so.getPriceGross());
		}
		PaymentDetails pd = order.getPaymentDetails();
		if (pd != null) {
			String pc = schemaBeanFactory.getRecordFromSchemaBean(pd).getType().getName();
			invoice.setPaymentCategory(pc);
		}

		schemaBeanFactory.getRecordFromSchemaBean(ba).iteratePresentValues(new RecordAttributeIterator() {
			private final Record addressRecord = schemaBeanFactory.getRecordFromSchemaBean(address);

			@Override
			public void handleAttribute(Attribute attribute, Record record) {
				addressRecord.setAttributeValue(attribute.getName(), record.getAttributeValue(attribute.getName()));
			}
		});
		invoice.setAddress(address);

		long pos = 1;
		List<InvoiceItem> items = new ArrayList<>();
		for (LineItem lineItem : order.getItems()) {
			InvoiceItem invoiceItem = createBeanInContext(InvoiceItem.class, ctx);

			invoiceItem.setArticleName(lineItem.getProductName());
			invoiceItem.setArticleId(lineItem.getSku());
			invoiceItem.setTax(lineItem.getTaxRate());
			invoiceItem.setQuantity(lineItem.getQuantity());
			invoiceItem.setSinglePrice(lineItem.getPriceGross());
			invoiceItem.setCurrency(lineItem.getCurrency());
			invoiceItem.setPos(pos);
			invoiceItem.setTax(lineItem.getTaxRate());
			invoiceItem.setSumPrice(lineItem.getTotalGross());
			pos++;
			invoiceItem.setInvoice(invoice);
			items.add(invoiceItem);
		}
		invoice.setInvoiceItems(items);

		createPortageInvoiceItem(order, invoice, ctx);
		return invoice;
	}

	public void generateInvoiceForOrder(PurchaseOrder order, RecordContext ctx) {
		if (order != null) {
			createInvoice(order, ctx);
			((ActiveRecord) invoice).persistCascaded();
			invoice.setNumber(((ActiveRecord) invoice).getId().toString());
			((ActiveRecord) invoice).update();

			List<OrderItemBilling> orderItemBillings = createOrderItemBillings(order, invoice, ctx);
			for (OrderItemBilling orderItemBilling : orderItemBillings) {
				((ActiveRecord) orderItemBilling).persist();
			}

			InvoiceTerm invoiceTerm = createBeanInContext(InvoiceTerm.class, ctx);
			PaymentDetails paymentDetails = order.getPaymentDetails();
			if (paymentDetails != null) {
				InvoiceTermType type = null;
				Record typeRec = null;
				try {
					typeRec = engine.getAccessor().queryByExample(InvoiceTermType.class.getSimpleName(), ctx).eager().attribute("name", PaymentDetails.class.getSimpleName()).single();
					if (typeRec != null) {
						type = schemaBeanFactory.getSchemaBean(InvoiceTermType.class, typeRec);
					}
				} catch (EmptyResultException e) {
				}
				if (typeRec == null) {
					typeRec = ctx.create(InvoiceTermType.class.getSimpleName());
					type = schemaBeanFactory.getSchemaBean(InvoiceTermType.class, typeRec);
					type.setName(PaymentDetails.class.getSimpleName());
					((ActiveRecord) type).persist();
				}
				if (type != null) {
					invoiceTerm.setInvoice(invoice);
					String val = null;
					if(schemaBeanFactory.isSchemaBean(paymentDetails)) {
						Record rec = schemaBeanFactory.getRecordFromSchemaBean(paymentDetails);
						val = rec.getType().getName();
					} else {
						val = paymentDetails.getClass().getSimpleName();
					}
					invoiceTerm.setStringValue(val);
					invoiceTerm.setInvoiceTermType(type);
					((ActiveRecord) invoiceTerm).persist();
				}
			}
		}
	}

	private List<OrderItemBilling> createOrderItemBillings(PurchaseOrder order, Invoice invoice, RecordContext ctx) {
		List<OrderItemBilling> orderItemBillings = new ArrayList<>();

		for (LineItem lineItem : order.getItems()) {
			OrderItemBilling orderItemBilling = createBeanInContext(OrderItemBilling.class, ctx);
			InvoiceItem invoiceItem = invoiceItemForLineItem(lineItem, invoice, ctx);

			orderItemBilling.setLineItem(lineItem);
			orderItemBilling.setInvoiceItem(invoiceItem);
			orderItemBilling.setQuantity(lineItem.getQuantity());
			long q = 1;
			if (lineItem.getQuantity() != null) {
				q = lineItem.getQuantity();
			}
			BigDecimal amount = lineItem.getPriceGross().multiply(new BigDecimal(q));
			orderItemBilling.setAmount(amount);

			orderItemBillings.add(orderItemBilling);
		}

		return orderItemBillings;
	}

	/**
	 * takes a lineItem from an order and looks up its appearance in an invoice.
	 *
	 * @param lineItem the lineItem of the order
	 * @param invoice the invoice that is associated to the order of the
	 * lineItem
	 * @return the invoiceItem that corresponds to the lineItem
	 */
	private InvoiceItem invoiceItemForLineItem(LineItem lineItem, Invoice invoice, RecordContext ctx) {
		List<InvoiceItem> items = invoice.getInvoiceItems();
		if (items != null) {
			for (InvoiceItem invoiceItem : items) {
				String type = invoiceItem.getInvoiceItemType();
				if (!"PORTAGE".equals(type)) {
					String lineItemProduct = lineItem.getSku();
					String invoiceItemProduct = invoiceItem.getArticleId();

					if (lineItemProduct != null && lineItemProduct.equals(invoiceItemProduct)) {
						return invoiceItem;
					}
				}
			}
		}
		return null;
	}

	private void createPortageInvoiceItem(PurchaseOrder order, Invoice invoice, RecordContext ctx) {
		InvoiceItem invoiceItem = createBeanInContext(InvoiceItem.class, ctx);
		invoiceItem.setInvoiceItemType("PORTAGE");
		invoiceItem.setSinglePrice(order.getShipmentOffer().getPrice());
		invoiceItem.setSumPrice(order.getShipmentOffer().getPrice());
		invoiceItem.setArticleName("PORTAGE");
		invoiceItem.setArticleId("");
		TaxQuotaInfo tq = order.getTaxQuotaInfo();
		if (tq != null) {
			ValueAddedTax vatSide = tq.getValueAddedTaxForSideCosts();
			if (vatSide != null) {
				BigDecimal rate = vatSide.getValue();
				invoiceItem.setTax(rate);
			}
		}
		invoiceItem.setCurrency(order.getShipmentOffer().getCurrency());
		invoiceItem.setQuantity(1L);
		invoiceItem.setInvoice(invoice);
		invoice.getInvoiceItems().add(invoiceItem);
	}

}
