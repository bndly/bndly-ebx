package org.bndly.ebx.resources.link;

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

import org.bndly.rest.atomlink.api.annotation.AtomLinkConstraint;
import org.bndly.rest.atomlink.api.annotation.AtomLinkDescription;
import org.bndly.rest.beans.ebx.PurchaseOrderRestBean;
import org.osgi.service.component.annotations.Component;

@Component(service = AtomLinkConstraint.class, immediate = true)
public class PurchaseOrderLinkConstraints implements AtomLinkConstraint {

	@Override
	public String shouldBeInjected(AtomLinkDescription atomLinkDescription) {
		if (atomLinkDescription.getRel().equals("update")) {
			return "${this.getHasCancelations() == null || this.getHasCancelations() == false}";
		}
		return null;
	}

	@Override
	public boolean supportsRestBeanType(Class<?> type) {
		return PurchaseOrderRestBean.class.isAssignableFrom(type);
	}

}
