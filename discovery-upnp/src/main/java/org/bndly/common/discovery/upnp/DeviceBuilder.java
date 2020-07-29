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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPIcon;
import org.osgi.service.upnp.UPnPService;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class DeviceBuilder {

	private final Map<String, UPnPService> servicesByServiceId = new LinkedHashMap<>();
	private boolean exported;
	private String friendlyName;
	private String manufacturer;
	private String manufacturerUrl;
	private String modelDescription;
	private String modelName;
	private String modelVersion;
	private String modelUrl;
	private String serialNumber;
	private String type;
	private String deviceId;
	private String upc;
	
	private DeviceBuilder() {
	}
	
	public static final DeviceBuilder newInstance() {
		return new DeviceBuilder();
	}
	
	public DeviceBuilder service(ServiceBuilder serviceBuilder) {
		return service(serviceBuilder.build());
	}
	
	public DeviceBuilder service(UPnPService service) {
		if (service == null) {
			throw new IllegalArgumentException("can not add null service");
		}
		servicesByServiceId.put(service.getId(), service);
		return this;
	}
	
	public DeviceBuilder export() {
		exported = true;
		return this;
	}

	public DeviceBuilder friendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
		return this;
	}

	public DeviceBuilder manufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
		return this;
	}

	public DeviceBuilder manufacturerUrl(String manufacturerUrl) {
		this.manufacturerUrl = manufacturerUrl;
		return this;
	}

	public DeviceBuilder modelDescription(String modelDescription) {
		this.modelDescription = modelDescription;
		return this;
	}

	public DeviceBuilder modelName(String modelName) {
		this.modelName = modelName;
		return this;
	}

	public DeviceBuilder modelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
		return this;
	}

	public DeviceBuilder modelUrl(String modelUrl) {
		this.modelUrl = modelUrl;
		return this;
	}

	public DeviceBuilder serialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
		return this;
	}

	public DeviceBuilder type(String type) {
		this.type = type;
		return this;
	}

	public DeviceBuilder deviceId(String deviceId) {
		this.deviceId = deviceId;
		return this;
	}

	public DeviceBuilder upc(String upc) {
		this.upc = upc;
		return this;
	}
	
	public UPnPDevice build() {
		final Map<String, UPnPService> fServicesByServiceId = new LinkedHashMap<>(servicesByServiceId);
		final UPnPService[] services = new UPnPService[fServicesByServiceId.size()];
		int i = 0;
		for (UPnPService service : fServicesByServiceId.values()) {
			services[i] = service;
			i++;
		}
		final Properties fProperties = new Properties();
		if (exported) {
			fProperties.put(UPnPDevice.UPNP_EXPORT, "");
		}
		fProperties.put(
				"DEVICE_CATEGORY",
				new String[]{UPnPDevice.DEVICE_CATEGORY}
		);
		if (friendlyName != null) {
			fProperties.put(UPnPDevice.FRIENDLY_NAME, friendlyName);
		}
		if (manufacturer != null) {
			fProperties.put(UPnPDevice.MANUFACTURER, manufacturer);
		}
		if (manufacturerUrl != null) {
			fProperties.put(UPnPDevice.MANUFACTURER_URL, manufacturerUrl);
		}
		if (modelDescription != null) {
			fProperties.put(UPnPDevice.MODEL_DESCRIPTION, modelDescription);
		}
		if (modelName != null) {
			fProperties.put(UPnPDevice.MODEL_NAME, modelName); // required when exporting
		}
		if (modelVersion != null) {
			fProperties.put(UPnPDevice.MODEL_NUMBER, modelVersion);
		}
		if (modelUrl != null) {
			fProperties.put(UPnPDevice.MODEL_URL, modelUrl);
		}
		if (serialNumber != null) {
			fProperties.put(UPnPDevice.SERIAL_NUMBER, serialNumber);
		}
		if (type != null) {
			fProperties.put(UPnPDevice.TYPE, type);
		}
		if (deviceId != null) {
			fProperties.put(UPnPDevice.UDN, deviceId);
		}
		if (upc != null) {
			fProperties.put(UPnPDevice.UPC, upc); // universal product code
		}

		HashSet types = new HashSet(services.length + 5);
		String[] ids = new String[services.length];
		for (i = 0; i < services.length; i++) {
			ids[i] = services[i].getId();
			types.add(services[i].getType());
		}

		fProperties.put(UPnPService.TYPE, types.toArray(new String[]{}));
		fProperties.put(UPnPService.ID, ids);
		return new UPnPDevice() {
			@Override
			public UPnPService getService(String serviceId) {
				return fServicesByServiceId.get(serviceId);
			}

			@Override
			public UPnPService[] getServices() {
				return services;
			}

			@Override
			public UPnPIcon[] getIcons(String string) {
				return null;
			}

			@Override
			public Dictionary getDescriptions(String string) {
				return fProperties;
			}
		};
	}
	
}
