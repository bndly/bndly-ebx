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

import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPService;
import org.osgi.service.upnp.UPnPStateVariable;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class ServiceBuilder {

	private final Map<String, UPnPAction> actionsByName = new LinkedHashMap<>();
	private final Map<String, UPnPStateVariable> variablesByName = new LinkedHashMap<>();
	
	private String id;
	private String type;
	private String version;

	private ServiceBuilder() {
	}
	
	public static final ServiceBuilder newInstance() {
		return new ServiceBuilder();
	}
	
	public ServiceBuilder id(String id) {
		this.id = id;
		return this;
	}
	public ServiceBuilder type(String type) {
		this.type = type;
		return this;
	}
	public ServiceBuilder version(String version) {
		this.version = version;
		return this;
	}
	
	public ServiceBuilder action(UPnPAction action) {
		actionsByName.put(action.getName(), action);
		return this;
	}
	
	public ServiceBuilder action(ActionBuilder action) {
		UPnPAction actionInstance = action.stateVariables(variablesByName).build();
		actionsByName.put(actionInstance.getName(), actionInstance);
		return this;
	}
	
	public ServiceBuilder variable(UPnPStateVariable variable) {
		variablesByName.put(variable.getName(), variable);
		return this;
	}
	
	public UPnPService build() {
		final Map<String, UPnPAction> fActionsByName = new LinkedHashMap<>(actionsByName);
		final UPnPAction[] actions = new UPnPAction[fActionsByName.size()];
		int i = 0;
		for (UPnPAction service : fActionsByName.values()) {
			actions[i] = service;
			i++;
		}
		final Map<String, UPnPStateVariable> fVariablesByName = new LinkedHashMap<>(variablesByName);
		final UPnPStateVariable[] variables = new UPnPStateVariable[fVariablesByName.size()];
		i = 0;
		for (UPnPStateVariable var : fVariablesByName.values()) {
			variables[i] = var;
			i++;
		}
		
		final String fId = id;
		final String fType = type;
		final String fVersion = version;
		return new UPnPService() {
			@Override
			public String getId() {
				return fId;
			}

			@Override
			public String getType() {
				return fType;
			}

			@Override
			public String getVersion() {
				return fVersion;
			}

			@Override
			public UPnPAction getAction(String name) {
				return fActionsByName.get(name);
			}

			@Override
			public UPnPAction[] getActions() {
				return actions;
			}

			@Override
			public UPnPStateVariable[] getStateVariables() {
				return variables;
			}

			@Override
			public UPnPStateVariable getStateVariable(String name) {
				return fVariablesByName.get(name);
			}
		};
	}
}
