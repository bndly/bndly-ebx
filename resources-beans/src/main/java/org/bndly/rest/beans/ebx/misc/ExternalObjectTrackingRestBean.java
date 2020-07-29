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

import org.bndly.rest.common.beans.RestBean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@XmlRootElement(name="externalObjectTracking")
@XmlAccessorType(XmlAccessType.NONE)
public class ExternalObjectTrackingRestBean extends RestBean {
	@XmlElement
	private String rootExternalObjectIdentifier;
	@XmlElement
	private String externalObjectIdentifier;
	@XmlElement
	private String listTypeName;

	public String getRootExternalObjectIdentifier() {
		return rootExternalObjectIdentifier;
	}

	public void setRootExternalObjectIdentifier(String rootExternalObjectIdentifier) {
		this.rootExternalObjectIdentifier = rootExternalObjectIdentifier;
	}

	public String getExternalObjectIdentifier() {
		return externalObjectIdentifier;
	}

	public void setExternalObjectIdentifier(String externalObjectIdentifier) {
		this.externalObjectIdentifier = externalObjectIdentifier;
	}

	public String getListTypeName() {
		return listTypeName;
	}

	public void setListTypeName(String listTypeName) {
		this.listTypeName = listTypeName;
	}
	
}
