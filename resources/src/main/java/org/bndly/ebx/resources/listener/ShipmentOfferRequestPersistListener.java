package org.bndly.ebx.resources.listener;

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

import org.bndly.ebx.resources.bpm.ShipmentOfferBusinessProcessses;
import org.bndly.ebx.model.ShipmentOfferRequest;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = {ShipmentOfferRequestPersistListener.class, PersistListener.class}, immediate = true)
public class ShipmentOfferRequestPersistListener implements PersistListener {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference
	private ShipmentOfferBusinessProcessses shipmentOfferBusinessProcessses;

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().addListener(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().removeListener(this);
	}

	@Override
	public void onPersist(Record record) {
		if (ShipmentOfferRequest.class.getSimpleName().equals(record.getType().getName())) {
			ShipmentOfferRequest req = schemaBeanFactory.getSchemaBean(ShipmentOfferRequest.class, record);
			record.dropAttribute("shipmentOffers");
			shipmentOfferBusinessProcessses.runListShipmentOffers(req, record.getContext());
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setShipmentOfferBusinessProcessses(ShipmentOfferBusinessProcessses shipmentOfferBusinessProcessses) {
		this.shipmentOfferBusinessProcessses = shipmentOfferBusinessProcessses;
	}
}
