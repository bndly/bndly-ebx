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
import org.bndly.ebx.model.Address;
import org.bndly.ebx.model.AddressAssignment;
import org.bndly.ebx.model.BankPaymentDetails;
import org.bndly.ebx.model.CashOnDeliveryPaymentDetails;
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.ClassicBankPaymentDetails;
import org.bndly.ebx.model.CreditCardPaymentDetails;
import org.bndly.ebx.model.IBANBankPaymentDetails;
import org.bndly.ebx.model.InvoicePaymentDetails;
import org.bndly.ebx.model.PaymentDetailAssignment;
import org.bndly.ebx.model.PaymentDetails;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.TransferPaymentDetails;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordAttributeIterator;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.model.Attribute;
import java.util.List;

public class AdjustPersonDataAfterCheckoutTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

	@ProcessVariable(ProcessVariable.Access.READ)
	private CheckoutRequest checkoutRequest;

	public void run() {
		PurchaseOrder order = checkoutRequest.getOrder();
		RecordContext ctx = schemaBeanFactory.getRecordFromSchemaBean(checkoutRequest).getContext();
		Person orderer = order.getOrderer();
		Address ordererAddress = orderer.getAddress();
		Address orderAddress = order.getAddress();
		Address billingAddressFromOrder = order.getBillingAddress();
		Address delivergAddressFromOrder = order.getDeliveryAddress();

		String externalUserId = orderer.getExternalUserId();
		if (externalUserId != null) {
			Record personRecord = engine.getAccessor().queryByExample(Person.class.getSimpleName(), ctx).attribute("externalUserId", externalUserId).single();
			Person person = schemaBeanFactory.getSchemaBean(Person.class, personRecord);
			if (person == null) {
				((ActiveRecord) orderer).persistCascaded();
				person = orderer;
			}

			boolean personHasChanged = false;
			if (person != null) {
				Address addressFromDb = person.getAddress();
				Address oldStammadresse = null;

				// if the person was persisted before this checkout but did not have a full adress yet or if the person was submitted without an address
				if (addressFromDb == null) {
		    // user the order address as the address for the person
					// a clone is used, to prevent changes to the order
					person.setAddress(cloneWithoutId(orderAddress, ctx));
					personHasChanged = true;
				} else {
		    // if an address for the person exists in the database, compare it to the address of the order
					// if the order address differs, then update the person address and move the previous person address to the person's further addresses
					if (new SchemaBeanComparator(schemaBeanFactory).compare(addressFromDb, orderAddress) != 0) {
						person.setAddress(cloneWithoutId(orderAddress, ctx));
						personHasChanged = true;
						oldStammadresse = addressFromDb;
					}
				}
				((ActiveRecord) person).updateCascaded();
//                personService.updateEntry(person);
				if (oldStammadresse != null) {
					injectIntoPerson(oldStammadresse, person, ctx);
				}

				// do the same address merge for billing address and delivery address as well
				injectIntoPerson(billingAddressFromOrder, person, ctx);
				injectIntoPerson(delivergAddressFromOrder, person, ctx);

				// load all payment details of the person
				List<Record> records = engine.getAccessor().queryByExample(PaymentDetailAssignment.class.getSimpleName(), ctx).eager().attribute("person", createRecordIdReference(personRecord)).all();
				List<PaymentDetailAssignment> assignments = schemaBeanFactory.getSchemaBeans(records, PaymentDetailAssignment.class);
				PaymentDetails paymentDetails = order.getPaymentDetails();
				if (paymentDetails != null && isGenerallyPersonAssigned(paymentDetails.getClass())) {
					if (!isKnownPaymentDetail(paymentDetails, assignments)) {
						PaymentDetailAssignment pda = clonePaymentDetails(paymentDetails, person, ctx);
						if (pda != null) {
							((ActiveRecord) pda).persistCascaded();
						}
					}
				}

				order.setOrderer(person);
			}
		}
	}

	private boolean isGenerallyPersonAssigned(Class<? extends PaymentDetails> type) {
		return CreditCardPaymentDetails.class.isAssignableFrom(type) || BankPaymentDetails.class.isAssignableFrom(type);
	}

	private PaymentDetailAssignment clonePaymentDetails(PaymentDetails paymentDetails, Person person, RecordContext ctx) {
		if (paymentDetails == null) {
			return null;
		}

		PaymentDetails pd;
		if (paymentDetails instanceof ClassicBankPaymentDetails) {
//	    ClassicBankPaymentDetails bp =  paymentDetails;
			ClassicBankPaymentDetails p = cloneWithoutId((ClassicBankPaymentDetails) paymentDetails, ctx);
//	    p.setAccountNumber(bp.getAccountNumber());
//	    p.setBankCode(bp.getBankCode());
//	    p.setBankName(bp.getBankName());
//	    p.setOwner(bp.getOwner());
			pd = p;
		} else if (paymentDetails instanceof IBANBankPaymentDetails) {
			IBANBankPaymentDetails p = cloneWithoutId((IBANBankPaymentDetails) paymentDetails, ctx);
//	    p.setBankName(bp.getBankName());
//	    p.setBic(bp.getBic());
//	    p.setIban(bp.getIban());
//	    p.setOwner(bp.getOwner());
			pd = p;
		} else if (paymentDetails instanceof CreditCardPaymentDetails) {
//	    CreditCardPaymentDetails cp = (CreditCardPaymentDetails) paymentDetails;
			CreditCardPaymentDetails p = cloneWithoutId((CreditCardPaymentDetails) paymentDetails, ctx);
//	    p.setCardBrand(cp.getCardBrand());
//	    p.setCardHolderName(cp.getCardHolderName());
//	    p.setCreditCardNumber(cp.getCreditCardNumber());
//	    p.setExpiry(cp.getExpiry());
			pd = p;
		} else if (paymentDetails instanceof InvoicePaymentDetails) {
//	    InvoicePaymentDetails ip = (InvoicePaymentDetails) paymentDetails;
			InvoicePaymentDetails p = cloneWithoutId((InvoicePaymentDetails) paymentDetails, ctx);
			pd = p;
		} else if (paymentDetails instanceof CashOnDeliveryPaymentDetails) {
//	    CashOnDeliveryPaymentDetails ip = (CashOnDeliveryPaymentDetails) paymentDetails;
			CashOnDeliveryPaymentDetails p = cloneWithoutId((CashOnDeliveryPaymentDetails) paymentDetails, ctx);
			pd = p;
		} else if (paymentDetails instanceof TransferPaymentDetails) {
//	    TransferPaymentDetails ip = (TransferPaymentDetails) paymentDetails;
			TransferPaymentDetails p = cloneWithoutId((TransferPaymentDetails) paymentDetails, ctx);
			pd = p;
		} else {
			return null;
		}
		PaymentDetailAssignment a = createBeanInContext(PaymentDetailAssignment.class, ctx);
		a.setPaymentDetail(pd);
		a.setPerson(person);
		return a;
	}

	private boolean injectIntoPerson(Address otherAddress, Person person, RecordContext ctx) {
		if (otherAddress != null) {
			// load all payment details of the person
			List<Record> records = engine.getAccessor().queryByExample(AddressAssignment.class.getSimpleName(), ctx).eager().attribute("person", createRecordIdReference(schemaBeanFactory.getRecordFromSchemaBean(person))).all();
			List<AddressAssignment> assignments = schemaBeanFactory.getSchemaBeans(records, AddressAssignment.class);
			boolean existsInSet = false;
			for (AddressAssignment assignment : assignments) {
				if (new SchemaBeanComparator(schemaBeanFactory).compare(assignment.getAddress(), otherAddress) == 0) {
					existsInSet = true;
					break;
				}
			}

			if (!existsInSet) {
				AddressAssignment copy = createBeanInContext(AddressAssignment.class, ctx);
				copy.setAddress(cloneWithoutId(otherAddress, ctx));
				copy.setPerson(person);
				copy.setRelation("billing");
				((ActiveRecord) copy).persistCascaded();
				return true;
			}
		}
		return false;
	}

	private boolean isKnownPaymentDetail(PaymentDetails detail, List<PaymentDetailAssignment> paymentDetailsFromDb) {
		if (paymentDetailsFromDb != null) {
			for (PaymentDetailAssignment paymentDetailsAssignment : paymentDetailsFromDb) {
				PaymentDetails paymentDetails = paymentDetailsAssignment.getPaymentDetail();
				if (paymentDetails.getClass().isAssignableFrom(detail.getClass())) {
					if (CreditCardPaymentDetails.class.isAssignableFrom(detail.getClass())) {
						if (new SchemaBeanComparator(schemaBeanFactory).compare((CreditCardPaymentDetails) detail, (CreditCardPaymentDetails) paymentDetails) != 0) {
						} else {
							return true;
						}
					} else if (BankPaymentDetails.class.isAssignableFrom(detail.getClass())) {
						if (new SchemaBeanComparator(schemaBeanFactory).compare((BankPaymentDetails) detail, (BankPaymentDetails) paymentDetails) != 0) {
						} else {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private <E> E cloneWithoutId(E bean, RecordContext ctx) {
		Record r = schemaBeanFactory.getRecordFromSchemaBean(bean);
		Class<?> beanType = schemaBeanFactory.getTypeBindingForType(r);
		E copy = (E) createBeanInContext(beanType, ctx);
		final Record copyRecord = schemaBeanFactory.getRecordFromSchemaBean(copy);
		r.iteratePresentValues(new RecordAttributeIterator() {

			@Override
			public void handleAttribute(Attribute attribute, Record record) {
				copyRecord.setAttributeValue(attribute.getName(), record.getAttributeValue(attribute.getName()));
			}
		});
		return copy;
	}

	private Record createRecordIdReference(Record input) {
		return RecordReferenceUtil.createRecordIdReference(input, schemaBeanFactory);
	}
}
