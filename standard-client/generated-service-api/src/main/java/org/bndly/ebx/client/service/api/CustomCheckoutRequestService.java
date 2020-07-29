package org.bndly.ebx.client.service.api;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service-api
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
import org.bndly.rest.client.exception.ClientException;

/**
 * Created by alexp on 17.06.15.
 */
public interface CustomCheckoutRequestService {

    CheckoutRequest assignPaymentConfiguration(CheckoutRequest checkoutRequest, PaymentConfiguration paymentConfiguration) throws ClientException;

    CheckoutRequest assignPaymentSuccess(CheckoutRequest checkout) throws ClientException;

    CheckoutRequest assignPaymentCancelation(CheckoutRequest checkout) throws ClientException;

    CheckoutRequest assignPaymentFailure(CheckoutRequest checkout) throws ClientException;
}
