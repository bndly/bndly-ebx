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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPStateVariable;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class ActionBuilder {

	private Invoker invoker;
	private Map<String, UPnPStateVariable> stateVariables = Collections.EMPTY_MAP;
	private String name;
	private String returnArgumentName;
	private final List<String> inputArgumentNames = new ArrayList<>();
	private final List<String> outputArgumentNames = new ArrayList<>();

	public static interface Invoker {
		Dictionary invoke(Dictionary dctnr, UPnPAction action) throws Exception;
	}
	
	private ActionBuilder() {
	}
	
	public static ActionBuilder newInstance() {
		return new ActionBuilder();
	}
	
	public ActionBuilder inputArgumentName(String inputArgumentName) {
		inputArgumentNames.add(inputArgumentName);
		return this;
	}
	
	public ActionBuilder outputArgumentName(String outputArgumentName) {
		outputArgumentNames.add(outputArgumentName);
		return this;
	}

	public ActionBuilder stateVariables(Map<String, UPnPStateVariable> stateVariables) {
		this.stateVariables = stateVariables;
		return this;
	}

	public ActionBuilder returnArgumentName(String returnArgumentName) {
		this.returnArgumentName = returnArgumentName;
		return this;
	}

	public ActionBuilder name(String name) {
		this.name = name;
		return this;
	}

	public ActionBuilder invoker(Invoker invoker) {
		this.invoker = invoker;
		return this;
	}
	
	public UPnPAction build() {
		final String fName = name;
		final String fReturnArgumentName = returnArgumentName;
		final String[] fInputArgumentNames = new String[inputArgumentNames.size()];
		for (int i = 0; i < inputArgumentNames.size(); i++) {
			fInputArgumentNames[i] = inputArgumentNames.get(i);
		}
		final String[] fOutputArgumentNames = new String[outputArgumentNames.size()];
		for (int i = 0; i < outputArgumentNames.size(); i++) {
			fOutputArgumentNames[i] = outputArgumentNames.get(i);
		}
		final Map<String, UPnPStateVariable> fStateVariables = stateVariables;
		final Invoker fInvoker = invoker;
		return new UPnPAction() {
			@Override
			public String getName() {
				return fName;
			}

			@Override
			public String getReturnArgumentName() {
				return fReturnArgumentName;
			}

			@Override
			public String[] getInputArgumentNames() {
				return fInputArgumentNames;
			}

			@Override
			public String[] getOutputArgumentNames() {
				return fOutputArgumentNames;
			}

			@Override
			public UPnPStateVariable getStateVariable(String string) {
				return fStateVariables.get(string);
			}

			@Override
			public Dictionary invoke(Dictionary dctnr) throws Exception {
				return fInvoker.invoke(dctnr, this);
			}
		};
	}
	
}
