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

import org.bndly.common.service.model.api.AbstractEntity;
import org.bndly.ebx.model.Price;
import org.bndly.ebx.price.api.PriceDataInspector;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = PriceDataInspector.class)
public class AbstractEntityPriceDataInspector implements PriceDataInspector {

	private static final String CLASS_SUFFIX = "Impl";
	
	@Override
	public String getKey(Price price) {
		if (!AbstractEntity.class.isInstance(price)) {
			return null;
		}
		// the naming convention is {TYPENAME}Impl
		String className = price.getClass().getSimpleName();
		if (!className.endsWith(CLASS_SUFFIX)) {
			return null;
		}

		return className.substring(0, className.length() - CLASS_SUFFIX.length());
	}
	
}
