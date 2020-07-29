package org.bndly.ebx.client.service.impl;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service
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

import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.PaymentConfiguration;
import org.bndly.rest.beans.ebx.CheckoutRequestRestBean;
import org.bndly.rest.beans.ebx.PaymentConfigurationRestBean;
import org.bndly.ebx.client.service.api.CustomCheckoutRequestService;
import org.bndly.common.service.shared.api.ProxyAware;

import org.bndly.ebx.client.service.api.CheckoutRequestService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 17.06.15.
 */
public class CustomCheckoutRequestServiceImpl implements CustomCheckoutRequestService, ProxyAware<CheckoutRequestService> {

    private CheckoutRequestService thisProxy;

    @Override
    public CheckoutRequest assignPaymentConfiguration(CheckoutRequest checkout, PaymentConfiguration paymentConfiguration) throws ClientException {
        CheckoutRequestRestBean bean = (CheckoutRequestRestBean) thisProxy.toRestModel(checkout);
        PaymentConfigurationRestBean paymentconfigurationRestBean = thisProxy.getMapperFactory().buildContext().map(paymentConfiguration, PaymentConfigurationRestBean.class);
        thisProxy.createClient(bean).follow("assignPaymentConfiguration").execute(paymentconfigurationRestBean);

		try {
			return thisProxy.read(checkout);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public CheckoutRequest assignPaymentSuccess(CheckoutRequest checkout) throws ClientException {
        CheckoutRequestRestBean configuredCheckout = (CheckoutRequestRestBean) thisProxy.toRestModel(checkout);
        thisProxy.createClient(configuredCheckout).follow("paymentSuccess").execute();

		try {
			return thisProxy.read(checkout);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public CheckoutRequest assignPaymentCancelation(CheckoutRequest checkout) throws ClientException {
        CheckoutRequestRestBean configuredCheckout = (CheckoutRequestRestBean) thisProxy.toRestModel(checkout);
        thisProxy.createClient(configuredCheckout).follow("paymentCanceled").execute();

		try {
			return thisProxy.read(checkout);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public CheckoutRequest assignPaymentFailure(CheckoutRequest checkout) throws ClientException {
        CheckoutRequestRestBean configuredCheckout = (CheckoutRequestRestBean) thisProxy.toRestModel(checkout);
        thisProxy.createClient(configuredCheckout).follow("paymentFailed").execute();

		try {
			return thisProxy.read(checkout);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public void setThisProxy(CheckoutRequestService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
