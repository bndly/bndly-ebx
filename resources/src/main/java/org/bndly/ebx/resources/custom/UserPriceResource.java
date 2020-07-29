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
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.User;
import org.bndly.rest.api.ContentType;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.api.StatusWriter;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.beans.ebx.PriceRequestListRestBean;
import org.bndly.rest.beans.ebx.PriceRequestRestBean;
import org.bndly.rest.common.beans.error.ErrorRestBean;
import org.bndly.rest.controller.api.CacheControl;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Date;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = UserPriceResource.class, immediate = true)
@Path("ebx")
public class UserPriceResource {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference(target = "(service.pid=org.bndly.common.mapper.MapperFactory.ebx)")
	private MapperFactory mapperFactory;
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
	@AtomLink(rel = "createCacheable", target = PriceRequestListRestBean.class)
	@Path("PriceRequest/create")
	public Response redirectToCacheableResource(PriceRequestRestBean priceRequestRestBean, @Meta Context context) {
		ContentType dct = context.getDesiredContentType();
		if (dct == null || dct.getExtension() == null) {
			// we need the content type extension, because we encode some parameters as selectors in the url in order to achieve cacheability
			dct = ContentType.XML; // fall back to xml
		}
		PriceRequest pr = mapperFactory.buildContext().map(priceRequestRestBean, PriceRequest.class);
		Currency currency = pr.getCurrency();
		if (currency == null) {
			return badRequestResponse("missingParameter", "no currency was provided");
		}
		Long currencyId = getId(currency);
		if (currencyId == null) {
			return badRequestResponse("missingParameter", "no existing currency was provided");
		}
		User user = pr.getUser();
		if (user == null) {
			return badRequestResponse("missingParameter", "no user was provided");
		}
		Long userId = getId(user);
		if (userId == null) {
			return badRequestResponse("missingParameter", "no existing user was provided");
		}
		String sku = pr.getSku();
		if (sku == null) {
			return badRequestResponse("missingParameter", "no sku was provided");
		}
		Long quantity = pr.getQuantity();
		if (quantity == null) {
			quantity = 1L;
		}
		ResourceURI uri = context.createURIBuilder()
				.pathElement("ebx")
				.pathElement("UserPrice")
				.pathElement(userId.toString())
				.pathElement(sku)
				.pathElement("price")
				.selector(currencyId.toString())
				.selector(quantity.toString())
				.extension(dct.getExtension())
				.build();
		return Response.seeOther(uri.asString());
	}

	@GET
	@Path("UserPrice/{id}/{sku}/price")
	@CacheControl(maxAge = 60) // cache the response for 1 minute
	public Response getPrice(@PathParam("id") long userId, @PathParam("sku") String sku, @Meta Context context) {
		ResourceURI uri = context.getURI();
		List<ResourceURI.Selector> selectors = uri.getSelectors();
		if (selectors == null) {
			return badRequestResponse("missingParameter", "no selectors found to pick parameters from");
		}
		Long currencyId = null;
		Long quantity = null;
		for (int i = 0; i < selectors.size(); i++) {
			ResourceURI.Selector selector = selectors.get(i);
			if (i == 0) {
				try {
					currencyId = Long.valueOf(selector.getName());
				} catch (NumberFormatException e) {
					// swallow the exception here
				}
			} else if (i == 1) {
				try {
					quantity = Long.valueOf(selector.getName());
				} catch (NumberFormatException e) {
					// swallow the exception here
				}
			}
		}
		if (sku == null || currencyId == null || quantity == null) {
			return badRequestResponse("missingParameter", "either sku, quantity or currency is missing");
		}
		RecordContext recordContext = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		PriceRequest priceRequest = schemaBeanFactory.getSchemaBean(PriceRequest.class, recordContext.create(PriceRequest.class.getSimpleName()));
		priceRequest.setSku(sku);
		priceRequest.setQuantity(quantity);
		Currency currency = schemaBeanFactory.getSchemaBean(Currency.class, recordContext.create(Currency.class.getSimpleName(), currencyId));
		priceRequest.setCurrency(currency);
		User user = schemaBeanFactory.getSchemaBean(User.class, recordContext.create(User.class.getSimpleName(), userId));
		priceRequest.setUser(user);
		priceRequest.setCreatedOn(new Date());
		((ActiveRecord) priceRequest).persist();
		PriceRequestRestBean restBean = mapperFactory.buildContext().map(priceRequest, PriceRequestRestBean.class);
		return Response.ok(restBean);
	}

	private Response badRequestResponse(String code, String message) {
		ErrorRestBean errorRestBean = new ErrorRestBean();
		errorRestBean.setMessage(message);
		errorRestBean.setName(code);
		return Response.status(StatusWriter.Code.BAD_REQUEST.getHttpCode()).entity(errorRestBean);
	}

	private Long getId(Object activeRecord) {
		if (!schemaBeanFactory.isSchemaBean(activeRecord)) {
			return null;
		}
		Record rec = schemaBeanFactory.getRecordFromSchemaBean(activeRecord);
		return rec.getId();
	}
}
