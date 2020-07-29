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
import org.bndly.ebx.model.SimplePrice;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.SchemaBeanProvider;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Schema;
import org.bndly.schema.model.Type;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class SchemaBeanPriceDataInspectorTest {
	private SchemaBeanFactory schemaBeanFactory = new SchemaBeanFactory(new SchemaBeanProvider() {
		@Override
		public String getSchemaName() {
			return "ebx";
		}

		@Override
		public String getSchemaBeanPackage() {
			return "org.bndly.ebx.model";
		}

		@Override
		public ClassLoader getSchemaBeanClassLoader() {
			return SchemaBeanPriceDataInspectorTest.class.getClassLoader();
		}
	});
	
	@Test
	public void testWrongInstance() {
		SchemaBeanPriceDataInspector schemaBeanPriceDataInspector = new SchemaBeanPriceDataInspector();
		schemaBeanPriceDataInspector.setSchemaBeanFactory(schemaBeanFactory);
		String key = schemaBeanPriceDataInspector.getKey(mock(Price.class));
		Assert.assertNull(key);
	}

	@Test
	public void testRightInstance() {
		SchemaBeanPriceDataInspector schemaBeanPriceDataInspector = new SchemaBeanPriceDataInspector();
		schemaBeanPriceDataInspector.setSchemaBeanFactory(schemaBeanFactory);
		Record record = mock(Record.class);
		Type type = new Type(new Schema("ebx", "http://localhost"));
		type.setName("SimplePrice");
		when(record.getType()).thenReturn(type);
		schemaBeanFactory.registerTypeBinding("SimplePrice", SimplePrice.class);
		String key = schemaBeanPriceDataInspector.getKey(schemaBeanFactory.getSchemaBean(SimplePrice.class, record));
		Assert.assertEquals("SimplePrice", key);
	}
}
