package org.bndly.rest.beans.jcr;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-resource-beans
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

@XmlRootElement(name = "jcrImporterStatus")
@XmlAccessorType(XmlAccessType.NONE)
public class JCRImporterStatusRestBean extends RestBean {

	@XmlElement
	private Boolean enabled;
	@XmlElement
	private Boolean running;
	@XmlElement
	private Boolean connected;
	@XmlElement
	private Double done;
	@XmlElement
	private String repositoryUrl;

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public Boolean getConnected() {
		return connected;
	}

	public void setConnected(Boolean connected) {
		this.connected = connected;
	}

	public Double getDone() {
		return done;
	}

	public void setDone(Double done) {
		this.done = done;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean isRunning() {
		return running;
	}

	public void setRunning(Boolean running) {
		this.running = running;
	}

}
