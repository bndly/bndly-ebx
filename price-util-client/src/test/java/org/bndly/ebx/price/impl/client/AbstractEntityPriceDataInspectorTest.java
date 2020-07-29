package org.bndly.ebx.price.impl.client;

/*-
 * #%L
 * org.bndly.ebx.price-util-client
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
import org.bndly.ebx.model.impl.SimplePriceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class AbstractEntityPriceDataInspectorTest {

	@Test
	public void testWrongInstance() {
		AbstractEntityPriceDataInspector abstractEntityPriceDataInspector = new AbstractEntityPriceDataInspector();
		String key = abstractEntityPriceDataInspector.getKey(Mockito.mock(Price.class));
		Assert.assertNull(key);
	}

	@Test
	public void testRightInstance() {
		AbstractEntityPriceDataInspector abstractEntityPriceDataInspector = new AbstractEntityPriceDataInspector();
		String key = abstractEntityPriceDataInspector.getKey(new SimplePriceImpl());
		Assert.assertEquals("SimplePrice", key);
	}
}
