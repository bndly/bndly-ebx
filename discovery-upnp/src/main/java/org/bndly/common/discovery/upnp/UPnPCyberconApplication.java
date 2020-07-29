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

import static org.bndly.common.discovery.upnp.Activator.GET_STATUS_ACTION;
import static org.bndly.common.discovery.upnp.Activator.STATUS_SERVICE_ID;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class UPnPCyberconApplication implements CyberconApplication {

	private static final Logger LOG = LoggerFactory.getLogger(UPnPCyberconApplication.class);

	static final Pattern URL_PATTERN = Pattern.compile(".*v4\\[\\/((\\d{1,3}\\.){3}\\d{1,3})\\].*\\:(\\d+)$");
	private final UPnPDevice device;

	public UPnPCyberconApplication(UPnPDevice device) {
		if (device == null) {
			throw new IllegalArgumentException("device is not allowed to be null");
		}
		this.device = device;
	}

	public UPnPDevice getDevice() {
		return device;
	}

//	@Override
//	public Inet4Address getIPv4Address() {
//		UPnPService service = device.getService(STATUS_SERVICE_ID);
//		if (service == null) {
//			LOG.error("UPnP device had no {} service", STATUS_SERVICE_ID);
//			return null;
//		}
//		UPnPAction action = service.getAction(GET_STATUS_ACTION);
//		if (action != null) {
//			try {
//				Dictionary result = action.invoke(new Properties());
//				Object addressRaw = result.get("Status");
//				Object portRaw = result.get("Port");
////				String returnArgumentName = action.getReturnArgumentName();
////				if (returnArgumentName == null) {
////					String[] outputArgumentNames = action.getOutputArgumentNames();
////					if (outputArgumentNames == null || outputArgumentNames.length == 0) {
////						LOG.error("status action had no return or output arguments");
////						return null;
////					}
////					returnArgumentName = outputArgumentNames[0];
////				}
////				Object addressRaw = result.get(returnArgumentName);
//				return ipv4FromRawValue(addressRaw);
//			} catch (Exception ex) {
//				LOG.error("error while getting status", ex);
//				return null;
//			}
//		}
//		UPnPStateVariable variable = service.getStateVariable(STATUS_VARIABLE_NAME);
//		if (variable == null) {
//			LOG.error("UPnP service {} had no {} variable", STATUS_SERVICE_ID, STATUS_VARIABLE_NAME);
//			return null;
//		}
//		// how to get the value from the variable?
//		if (UPnPLocalStateVariable.class.isInstance(variable)) {
//			Object val = ((UPnPLocalStateVariable) variable).getCurrentValue();
//			return ipv4FromRawValue(val);
//		} else {
//			LOG.error("UPnP {} variable was not a local variable", STATUS_VARIABLE_NAME);
//		}
//		return null;
//	}

	@Override
	public URL getURL() {
		UPnPService service = device.getService(STATUS_SERVICE_ID);
		if (service == null) {
			LOG.error("UPnP device had no {} service", STATUS_SERVICE_ID);
			return null;
		}
		UPnPAction action = service.getAction(GET_STATUS_ACTION);
		if (action != null) {
			try {
				Dictionary result = action.invoke(new Properties());
				if(result == null) {
					LOG.error("did not get a result from GetStatus");
					return null;
				}
				Object addressRaw = result.get("Status");
				return urlFromRawValue(addressRaw);
			} catch (Exception ex) {
				LOG.error("error while getting status", ex);
				return null;
			}
		}
		return null;
	}
	

	private URL urlFromRawValue(Object val) {
		if (!String.class.isInstance(val)) {
			return null;
		}
		Matcher matcher = URL_PATTERN.matcher((String) val);
		if (matcher.matches()) {
			String ip = matcher.group(1);
			String port = matcher.group(3);
			try {
				return new URL("http://" + ip + ":" + port);
			} catch (MalformedURLException ex) {
				LOG.error("could not create URL", ex);
				return null;
			}
		} else {
			LOG.error("Cybercon Application had no IPv4 address: {}", (String) val);
			return null;
		}
	}
}
