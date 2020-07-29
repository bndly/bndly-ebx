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
import org.bndly.ebx.model.ShipmentMode;
import org.bndly.ebx.model.ShipmentOffer;
import org.bndly.ebx.model.ShipmentOfferRequest;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class CreateShipmentOfferTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

	@ProcessVariable(ProcessVariable.Access.READ)
	private BigDecimal priceGross;

	@ProcessVariable(ProcessVariable.Access.READ)
	private BigDecimal price;

	@ProcessVariable(ProcessVariable.Access.READ)
	private ShipmentMode shipmentMode;

	@ProcessVariable(ProcessVariable.Access.READ)
	private ShipmentOfferRequest shipmentOfferRequest;

	@ProcessVariable(ProcessVariable.Access.WRITE)
	private ShipmentOffer offer;

	@Override
	public void run() {
		if (shipmentMode != null) {
			RecordContext context = schemaBeanFactory.getRecordFromSchemaBean(shipmentMode).getContext();
			offer = schemaBeanFactory.getSchemaBean(ShipmentOffer.class, context.create(ShipmentOffer.class.getSimpleName()));
			offer.setCreatedOn(new Date());
			offer.setPriceGross(priceGross.setScale(5, RoundingMode.HALF_UP));
			offer.setPrice(price.setScale(5, RoundingMode.HALF_UP));
			offer.setMode(shipmentMode);
			offer.setRequest(shipmentOfferRequest);
			offer.setCurrency(shipmentOfferRequest.getCurrency());

			// persist
			((ActiveRecord) offer).persist();
		}
	}
}
