package org.bndly.common.discovery.upnp;

/*-
 * #%L
 * org.bndly.ebx.discovery-upnp
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

import org.apache.felix.upnp.extra.util.UPnPEventNotifier;
import org.osgi.service.upnp.UPnPLocalStateVariable;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public interface WriteableUPnPLocalStateVariable extends UPnPLocalStateVariable {
	void setValue(Object value);
	void setNotifier(UPnPEventNotifier notifier);
}
