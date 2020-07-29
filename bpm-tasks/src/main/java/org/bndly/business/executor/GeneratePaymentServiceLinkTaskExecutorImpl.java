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
import org.bndly.schema.beans.ActiveRecord;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

public class GeneratePaymentServiceLinkTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor, PaymentProviderDependent {

	@ProcessVariable(ProcessVariable.Access.READ)
	private CheckoutRequest checkoutRequest;

	private String paymentProviderUrl;

	public void run() {
		// http://localhost:9086/payForOrder?o=3&a=22.3&s=http%3A%2F%2Fwww.google.com&f=http%3A%2F%2Fwww.bndly.org&e=http%3A%2F%2Fwww.heise.de
		String orderNumer = checkoutRequest.getOrder().getOrderNumber();
		BigDecimal amount = checkoutRequest.getOrder().getMerchandiseValueGross();
//	BigDecimal amount = checkoutRequest.getOrder().getTotalGross();
		String successLink = checkoutRequest.getPaymentConfiguration().getSuccessLink();
		String failureLink = checkoutRequest.getPaymentConfiguration().getFailureLink();
		String cancelLink = checkoutRequest.getPaymentConfiguration().getCancelLink();

		String serviceURL = paymentProviderUrl + "/payForOrder?o=" + urlEscape(orderNumer) + "&a=" + urlEscape(amount.toString()) + "&s=" + urlEscape(successLink) + "&f=" + urlEscape(failureLink) + "&e=" + urlEscape(cancelLink);
		checkoutRequest.setServiceLink(serviceURL);
		((ActiveRecord) checkoutRequest).update();
	}

	@Override
	public void setPaymentProviderUrl(String paymentProviderUrl) {
		this.paymentProviderUrl = paymentProviderUrl;
	}

	private String urlEscape(String url) {
		try {
			return java.net.URLEncoder.encode(url, "ISO-8859-1");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("could not escape the string '" + url + "' for usage in a URL, because the encoding was not supported");
		}
	}
}
