package org.bndly.business.task;

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

import org.bndly.common.bpm.api.ServiceTaskClassProvider;
import java.util.HashMap;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = ServiceTaskClassProvider.class, immediate = true)
public class ServiceTaskClassProviderImpl implements ServiceTaskClassProvider {

	private final Map<String, Class<?>> map = new HashMap<>();

	@Activate
	public void activate() {
		registerClass(AdjustPersonDataAfterCheckoutTaskImpl.class);
		registerClass(AdjustProductsBoughtInConjunctionListsTaskImpl.class);
		registerClass(AdjustWishListsAfterCheckoutTaskImpl.class);
		registerClass(BillingFailureTaskImpl.class);
		registerClass(CancelOrderTaskImpl.class);
		registerClass(CreateShipmentOfferTaskImpl.class);
		registerClass(ExtractUserGroupTaskImpl.class);
		registerClass(FindProductByNumberTaskImpl.class);
		registerClass(FindStockItemForStockRequestTaskImpl.class);
		registerClass(GenerateInvoiceTaskImpl.class);
		registerClass(GeneratePaymentServiceLinkTaskImpl.class);
		registerClass(GenerateShipmentTaskImpl.class);
		registerClass(IsUserPremiumTaskImpl.class);
		registerClass(NotifyCustomerOfOrderTaskImpl.class);
		registerClass(OrderPaidTaskImpl.class);
		registerClass(SaveTaskImpl.class);
		registerClass(ShipmentModeIteratorTaskImpl.class);
		registerClass(WriteProductPriceToPriceRequestTaskImpl.class);
	}

	@Deactivate
	public void deactivate() {
		map.clear();
	}

	private void registerClass(Class<?> type) {
		map.put(type.getName(), type);
	}

	@Override
	public Class<?> getServiceTaskClassByName(String className) {
		return map.get(className);
	}

}
