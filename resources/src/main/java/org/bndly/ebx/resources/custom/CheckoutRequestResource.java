package org.bndly.ebx.resources.custom;

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

import org.bndly.common.mapper.MapperFactory;
import org.bndly.ebx.resources.bpm.CheckoutBusinessProcesses;
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.PaymentConfiguration;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinks;
import org.bndly.rest.atomlink.api.annotation.Parameter;
import org.bndly.rest.beans.ebx.CheckoutRequestRestBean;
import org.bndly.rest.beans.ebx.PaymentConfigurationRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Path("CheckoutRequest")
@Component(service = CheckoutRequestResource.class, immediate = true)
public class CheckoutRequestResource {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference(target = "(service.pid=org.bndly.common.mapper.MapperFactory.ebx)")
	private MapperFactory mapperFactory;
	@Reference
	private CheckoutBusinessProcesses checkoutBusinessProcesses;
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;

	@Activate
	public void activate() {
		controllerResourceRegistry.deploy(this);
	}

	@Deactivate
	public void deactivate() {
		controllerResourceRegistry.undeploy(this);
	}

	@POST
	@Path("{id}/paymentConfiguration")
	@AtomLink(rel = "assignPaymentConfiguration", target = CheckoutRequestRestBean.class)
	public Response assignPaymentConfiguration(final @PathParam("id") long id, PaymentConfigurationRestBean bean) {
		RecordContext context = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		CheckoutRequest cr = schemaBeanFactory.getSchemaBean(CheckoutRequest.class, context.create(CheckoutRequest.class.getSimpleName(), id));
		PaymentConfiguration pc = mapperFactory.buildContext().map(bean, PaymentConfiguration.class);
		((ActiveRecord) pc).persist();
		cr.setPaymentConfiguration(pc);
		((ActiveRecord) cr).update();
		// resume process
		checkoutBusinessProcesses.resumeCheckout(cr.getProcessId(), "paymentConfigurationReceived", pc, context);
		return Response.NO_CONTENT;
	}

	@POST
	@Path("{id}/paymentResult/{result}")
	@AtomLinks({
		@AtomLink(rel = "paymentSuccess", target = CheckoutRequestRestBean.class, parameters = {
			@Parameter(name = "result", expression = "success")
		}),
		@AtomLink(rel = "paymentCanceled", target = CheckoutRequestRestBean.class, parameters = {
			@Parameter(name = "result", expression = "cancelation")
		}),
		@AtomLink(rel = "paymentFailed", target = CheckoutRequestRestBean.class, parameters = {
			@Parameter(name = "result", expression = "failure")
		})
	})
	public Response assignPaymentResult(final @PathParam("id") long id, final @PathParam("result") String paymentResult) {
		RecordContext context = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		CheckoutRequest cr = schemaBeanFactory.getSchemaBean(CheckoutRequest.class, context.create(CheckoutRequest.class.getSimpleName(), id));
		cr.setPaymentResult(paymentResult);
		((ActiveRecord) cr).update();
		// resume process
		checkoutBusinessProcesses.resumeCheckout(cr.getProcessId(), "paymentResultReceived", paymentResult, context);
		return Response.NO_CONTENT;
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setCheckoutBusinessProcesses(CheckoutBusinessProcesses checkoutBusinessProcesses) {
		this.checkoutBusinessProcesses = checkoutBusinessProcesses;
	}

	public void setMapperFactory(MapperFactory mapperFactory) {
		this.mapperFactory = mapperFactory;
	}

}
