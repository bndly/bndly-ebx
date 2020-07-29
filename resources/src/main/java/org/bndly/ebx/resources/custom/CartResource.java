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
import org.bndly.ebx.resources.bpm.CartBusinessProcesses;
import org.bndly.ebx.model.Cart;
import org.bndly.ebx.model.CartItem;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.UserPrice;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.beans.ebx.CartRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Date;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Path("Cart")
@Component(service = CartResource.class, immediate = true)
public class CartResource {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference(target = "(service.pid=org.bndly.common.mapper.MapperFactory.ebx)")
	private MapperFactory mapperFactory;
	@Reference
	private CartBusinessProcesses cartBusinessProcesses;
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
	@Path("{id}")
	@AtomLink(rel = "refresh", target = CartRestBean.class)
	public Response refresh(@PathParam("id") long id, CartRestBean bean) {
		Cart cart = mapperFactory.buildContext().map(bean, Cart.class);
		RecordContext ctx = schemaBeanFactory.getRecordFromSchemaBean(cart).getContext();
		Record user = schemaBeanFactory.getEngine().getAccessor()
				.queryByExample(User.class.getSimpleName(), ctx)
				.attribute("identifier", cart.getUserIdentifier())
				.single();
		Transaction tx = schemaBeanFactory.getEngine().getQueryRunner().createTransaction();
		for (CartItem cartItem : cart.getCartItems()) {
			PriceRequest pr = schemaBeanFactory.getSchemaBean(PriceRequest.class, ctx.create(PriceRequest.class.getSimpleName()));
			pr.setCreatedOn(new Date());
			pr.setSku(cartItem.getSku());
			pr.setUser(schemaBeanFactory.getSchemaBean(User.class, user));
			pr.setQuantity(cartItem.getQuantity());
			((ActiveRecord) pr).persist();
			pr = cartBusinessProcesses.runPriceRequest(pr, ctx);
			((ActiveRecord) pr).reload();
			cartItem.setPriceBeforeRefresh(cartItem.getPriceNow());
			cartItem.setPriceGrossBeforeRefresh(cartItem.getPriceGrossNow());
			UserPrice up = pr.getPrice();
			if (up != null) {
				cartItem.setPriceNow(up.getDiscountedNetValue());
				cartItem.setPriceGrossNow(up.getDiscountedGrossValue());
			}
		}
		((ActiveRecord) cart).updateCascaded(tx);
		tx.commit();
		return Response.NO_CONTENT;
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setMapperFactory(MapperFactory mapperFactory) {
		this.mapperFactory = mapperFactory;
	}

	public void setCartBusinessProcesses(CartBusinessProcesses cartBusinessProcesses) {
		this.cartBusinessProcesses = cartBusinessProcesses;
	}

}
