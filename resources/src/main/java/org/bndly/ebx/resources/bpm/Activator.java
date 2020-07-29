package org.bndly.ebx.resources.bpm;

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

import org.bndly.common.bpm.api.ProcessInvokerFactory;
import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = Activator.class, immediate=true)
public class Activator {

	private final Logger LOG = LoggerFactory.getLogger(Activator.class);
	
	@Reference
	private ProcessInvokerFactory invokerFactory;
	private final String engineName="ebx";
	private ComponentContext componentContext;
	private final List<RegisteredInterface> services = new ArrayList<>();
	
	private class RegisteredInterface {
		private final Class<?> type;
		private final ServiceRegistration reg;

		public RegisteredInterface(Class<?> type, ServiceRegistration reg) {
			this.type = type;
			this.reg = reg;
		}

		public ServiceRegistration getReg() {
			return reg;
		}

		public Class<?> getType() {
			return type;
		}
		
	}
	
	@Activate
	public void activate(ComponentContext componentContext) {
		this.componentContext=componentContext;
		createAndRegisterProcess(CartBusinessProcesses.class);
		createAndRegisterProcess(CheckoutBusinessProcesses.class);
		createAndRegisterProcess(ShipmentOfferBusinessProcessses.class);
		createAndRegisterProcess(StockBusinessProcesses.class);
	}
	
	private <E> E createAndRegisterProcess(Class<E> interfaceType) {
		E instance = invokerFactory.create(interfaceType, engineName);
		if(componentContext != null) {
			LOG.info("registering BPM process invoker {}", interfaceType.getName());
			ServiceRegistration<E> reg = componentContext.getBundleContext().registerService(interfaceType, instance, null);
			services.add(new RegisteredInterface(interfaceType, reg));
		}
		return instance;
	}
	
	@Deactivate
	public void deactivate() {
		for (RegisteredInterface reg : services) {
			LOG.info("unregistering BPM process invoker {}", reg.getType().getName());
			reg.getReg().unregister();
		}
		services.clear();
	}
}
