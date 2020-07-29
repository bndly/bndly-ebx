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

import org.bndly.ebx.resources.bpm.CartBusinessProcesses;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {PriceRequestPersistListener.class, PersistListener.class}, immediate = true)
public class PriceRequestPersistListener implements PersistListener {

	private static final Logger LOG = LoggerFactory.getLogger(PriceRequestPersistListener.class);

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference
	private CartBusinessProcesses businessProcesses;

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
		if (PriceRequest.class.getSimpleName().equals(record.getType().getName())) {
			PriceRequest pr = schemaBeanFactory.getSchemaBean(PriceRequest.class, record);
			businessProcesses.runPriceRequest(pr, record.getContext());
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setBusinessProcesses(CartBusinessProcesses businessProcesses) {
		this.businessProcesses = businessProcesses;
	}

}
