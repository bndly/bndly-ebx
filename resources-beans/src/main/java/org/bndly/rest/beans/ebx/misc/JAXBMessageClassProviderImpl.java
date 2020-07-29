package org.bndly.rest.beans.ebx.misc;

/*-
 * #%L
 * org.bndly.ebx.resources-beans
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

import org.bndly.rest.atomlink.api.JAXBMessageClassProvider;
import java.util.Collection;
import java.util.HashSet;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = JAXBMessageClassProvider.class, immediate = true)
public class JAXBMessageClassProviderImpl implements JAXBMessageClassProvider {
	private final static Collection<Class<?>> classes;
	static {
		classes = new HashSet<>();
		classes.add(WishListMailRestBean.class);
		classes.add(UserTrackingRestBean.class);
		classes.add(ExternalObjectTrackingRestBean.class);
	}

	@Override
	public Collection<Class<?>> getJAXBMessageClasses() {
		return classes;
	}
}
