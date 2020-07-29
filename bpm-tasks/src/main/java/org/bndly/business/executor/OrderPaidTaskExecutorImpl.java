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
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.PurchaseOrderPaymentFulfillment;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import java.util.Date;

public class OrderPaidTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

	@ProcessVariable(ProcessVariable.Access.READ)
	private CheckoutRequest checkoutRequest;

	@ProcessVariable(ProcessVariable.Access.READ)
	private String paymentResult;

	@Override
	public void run() {
		checkoutRequest.setPaymentResult(paymentResult);
		// create a payment success object, that gets assigned to the order
		RecordContext context = schemaBeanFactory.getRecordFromSchemaBean(checkoutRequest).getContext();
		PurchaseOrderPaymentFulfillment paymentFulfillment = schemaBeanFactory.getSchemaBean(PurchaseOrderPaymentFulfillment.class, context.create(PurchaseOrderPaymentFulfillment.class.getSimpleName()));
		paymentFulfillment.setCreatedOn(new Date());
		paymentFulfillment.setPurchaseOrder(checkoutRequest.getOrder());
		paymentFulfillment.setPaymentConfiguration(checkoutRequest.getPaymentConfiguration());
		((ActiveRecord) paymentFulfillment).persist();
	}

}
