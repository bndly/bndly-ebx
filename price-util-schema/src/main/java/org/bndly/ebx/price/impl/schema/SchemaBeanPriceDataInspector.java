package org.bndly.ebx.price.impl.schema;

/*-
 * #%L
 * org.bndly.ebx.price-util-schema
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

import org.bndly.ebx.model.Price;
import org.bndly.ebx.price.api.PriceDataInspector;
import org.bndly.schema.api.Record;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = PriceDataInspector.class)
public class SchemaBeanPriceDataInspector implements PriceDataInspector {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Override
	public String getKey(Price price) {
		if (!schemaBeanFactory.isSchemaBean(price)) {
			return null;
		}
		Record record = schemaBeanFactory.getRecordFromSchemaBean(price);
		return record.getType().getName();
	}

	void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}
	
}
