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

import java.beans.PropertyChangeEvent;
import org.apache.felix.upnp.extra.util.UPnPEventNotifier;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class VariableBuilder {

	private String name;
	private Object defaultValue;
	private Class javaType;
	private Object currentValue;
	private String[] allowedValues;
	private Number minimum;
	private Number maximum;
	private Number step;
	private boolean sendsEvents;
	private String type;

	private VariableBuilder() {
	}
	
	public VariableBuilder name(String name) {
		this.name = name;
		return this;
	}
	
	public VariableBuilder currentValue(Object currentValue) {
		this.currentValue = currentValue;
		return this;
	}
	
	public VariableBuilder defaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public VariableBuilder upnpType(String type) {
		this.type = type;
		return this;
	}
	
	public VariableBuilder javaType(Class javaType) {
		this.javaType = javaType;
		return this;
	}

	public VariableBuilder allowedValues(String[] allowedValues) {
		this.allowedValues = allowedValues;
		return this;
	}
	
	public VariableBuilder minimum(Number minimum) {
		this.minimum = minimum;
		return this;
	}
	
	public VariableBuilder maximum(Number maximum) {
		this.maximum = maximum;
		return this;
	}
	
	public VariableBuilder step(Number step) {
		this.step = step;
		return this;
	}
	
	public VariableBuilder sendsEvents() {
		this.sendsEvents = true;
		return this;
	}
	
	public static VariableBuilder newInstance() {
		return new VariableBuilder();
	}
	
	public WriteableUPnPLocalStateVariable build() {
		final String fName = name;
		final Class fJavaType = javaType;
		final String fType = type;
		final Object fDefaultValue = defaultValue;
		final String[] fAllowedValues = allowedValues;
		final Number fMinimum = minimum;
		final Number fMaximum = maximum;
		final Number fStep = step;
		final boolean fSendsEvents = sendsEvents;
		return new WriteableUPnPLocalStateVariable() {
			private Object value = currentValue;
			private UPnPEventNotifier notifier;
			
			@Override
			public Object getCurrentValue() {
				return value;
			}

			@Override
			public String getName() {
				return fName;
			}

			@Override
			public Class getJavaDataType() {
				return fJavaType;
			}

			@Override
			public String getUPnPDataType() {
				return fType;
			}

			@Override
			public Object getDefaultValue() {
				return fDefaultValue;
			}

			@Override
			public String[] getAllowedValues() {
				return fAllowedValues;
			}

			@Override
			public Number getMinimum() {
				return fMinimum;
			}

			@Override
			public Number getMaximum() {
				return fMaximum;
			}

			@Override
			public Number getStep() {
				return fStep;
			}

			@Override
			public boolean sendsEvents() {
				return fSendsEvents;
			}

			@Override
			public void setValue(Object v) {
				if (v == null) {
					throw new IllegalArgumentException("null values are not supported!");
				}
				if (!v.equals(value)) {
					Object oldValue = value;
					value = v;
					if (notifier != null) {
						notifier.propertyChange(new PropertyChangeEvent(this, getName(), oldValue, v));
					}
				}
			}

			@Override
			public void setNotifier(UPnPEventNotifier notifier) {
				this.notifier = notifier;
			}
			
		};
	}
}
