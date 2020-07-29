package org.bndly.ebx.resources.bpm;

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

import org.bndly.common.bpm.annotation.Context;
import org.bndly.common.bpm.annotation.Event;
import org.bndly.common.bpm.annotation.ProcessID;
import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.PaymentConfiguration;
import org.bndly.schema.api.RecordContext;

public interface CheckoutBusinessProcesses {

	public CheckoutRequest runCheckout(
			@ProcessVariable(name = "checkoutRequest") CheckoutRequest checkoutRequest,
			@Context RecordContext recordContext
	);

	public CheckoutRequest resumeCheckout(
			@ProcessID String processId,
			@Event String eventName,
			@ProcessVariable(name = "paymentConfiguration") PaymentConfiguration paymentConfiguration,
			@Context RecordContext recordContext
	);

	public CheckoutRequest resumeCheckout(
			@ProcessID String processId,
			@Event String eventName,
			@ProcessVariable(name = "paymentResult") String paymentResult,
			@Context RecordContext recordContext
	);
}
