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

import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.common.osgi.util.ServiceRegistrationBuilder;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.apache.felix.upnp.extra.util.UPnPEventNotifier;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;
import org.osgi.service.upnp.UPnPStateVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = Activator.class, immediate = true)
public class Activator {
	
	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration<UPnPDevice> reg;
	private UPnPEventNotifier notifier;
	private UPnPCyberconApplicationTracker bndlyApplicationTracker;
	
	@Activate
	public void activate(final ComponentContext componentContext) throws InvalidSyntaxException {
		WriteableUPnPLocalStateVariable variable = VariableBuilder.newInstance()
					.defaultValue("hello world")
					.name(STATUS_VARIABLE_NAME)
					.currentValue("not hello world")
					.javaType(String.class)
					.upnpType(UPnPStateVariable.TYPE_STRING)
					.sendsEvents()
				.build();
		UPnPService statusService = ServiceBuilder.newInstance()
					.id(STATUS_SERVICE_ID)
					.type("urn:schemas-upnp-org:service:bndlyappstatus:1")
					.version("1")
					.variable(variable)
					.action(ActionBuilder.newInstance()
							.name(GET_STATUS_ACTION)
							.returnArgumentName("Status")
							.outputArgumentName("Status")
							.invoker(new ActionBuilder.Invoker() {
								@Override
								public Dictionary invoke(Dictionary dctnr, UPnPAction action) throws Exception {
									List<String> addresses = new ArrayList<>();
									Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
									while (networkInterfaces.hasMoreElements()) {
										NetworkInterface networkInterface = networkInterfaces.nextElement();
										if (networkInterface.isLoopback()) {
											// skipping loopback
											continue;
										}
										if (!networkInterface.isUp()) {
											// networkinterface is not up
										}
										Enumeration<InetAddress> internetAddresses = networkInterface.getInetAddresses();
										while (internetAddresses.hasMoreElements()) {
											InetAddress inetAddress = internetAddresses.nextElement();
											if (Inet4Address.class.isInstance(inetAddress)) {
												// ipv4
												addresses.add("v4[" + inetAddress.toString() + "]");
											} else if (Inet6Address.class.isInstance(inetAddress)) {
												// ipv6
												addresses.add("v6[" + inetAddress.toString() + "]");
											}
										}
									}

									StringBuilder sb = new StringBuilder();
									for (String address : addresses) {
										sb.append(address);
									}

									ServiceReference<?>[] refs = componentContext.getBundleContext().getServiceReferences("org.eclipse.jetty.server.Server", "(jetty.port=*)");
									if (refs != null && refs.length != 0) {
										Integer port = new DictionaryAdapter(refs[0]).getInteger("jetty.port");
										if (port != null) {
											sb.append(":").append(port.toString());
										}
									}
									Properties properties = new Properties();
									properties.put("Status", sb.toString());
									return properties;
								}
							})
					)
				.build();
		UPnPDevice device = DeviceBuilder.newInstance()
					.friendlyName("Cyber:con Application")
					.manufacturer("Cyber:con")
					.manufacturerUrl("http://www.bndly.org")
					.modelName("Model 1")
					.type(STATUS_DEVICE_TYPE)
					.deviceId("uuid:CyberconApplication+" + Integer.toHexString(new Random(System.currentTimeMillis()).nextInt()))
					.export()
					.service(statusService)
				.build();
		
		try {
			Dictionary result = statusService.getAction(GET_STATUS_ACTION).invoke(new Properties());
			variable.setValue(result.get("Status"));
		} catch (Exception e) {
			LOG.error("could not initialize variable status", e);
		}
		
		notifier = new UPnPEventNotifier(componentContext.getBundleContext(), device, statusService);
		variable.setNotifier(notifier);
		ServiceRegistrationBuilder<UPnPDevice> regBuilder = ServiceRegistrationBuilder
				.newInstance(UPnPDevice.class, device);
		Dictionary desc = device.getDescriptions(null);
		Enumeration keysEnum = desc.keys();
		while (keysEnum.hasMoreElements()) {
			Object key = keysEnum.nextElement();
			regBuilder.property((String) key, desc.get(key));
		}
		reg = regBuilder.register(componentContext.getBundleContext());
		bndlyApplicationTracker = new UPnPCyberconApplicationTracker(componentContext.getBundleContext());
		bndlyApplicationTracker.open();
	}
	public static final String GET_STATUS_ACTION = "GetStatus";
	public static final String STATUS_DEVICE_TYPE = "urn:schemas-upnp-org:device:CyberconApplication:1";
	public static final String STATUS_VARIABLE_NAME = "Status";
	public static final String STATUS_SERVICE_ID = "urn:schemas-upnp-org:serviceId:bndlyappstatus:1";
	
	@Deactivate
	public void deactivate() {
		if (bndlyApplicationTracker != null) {
			bndlyApplicationTracker.close();
		}
		if (notifier != null) {
			notifier.destroy();
		}
		if (reg != null) {
			reg.unregister();
		}
	}
}
