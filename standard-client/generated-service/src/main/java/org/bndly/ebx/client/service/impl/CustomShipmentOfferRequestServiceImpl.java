package org.bndly.ebx.client.service.impl;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service
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

import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.client.service.api.CurrencyService;
import org.bndly.ebx.model.Cart;
import org.bndly.ebx.model.ShipmentOfferRequest;
import org.bndly.ebx.model.User;
import org.bndly.ebx.model.impl.ShipmentOfferRequestImpl;
import org.bndly.ebx.model.impl.UserImpl;
import org.bndly.ebx.client.service.api.CartService;
import org.bndly.ebx.client.service.api.CustomShipmentOfferRequestService;
import org.bndly.ebx.client.service.api.ShipmentOfferRequestService;
import org.bndly.ebx.client.service.api.UserService;
import org.bndly.rest.client.exception.ClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomShipmentOfferRequestServiceImpl implements ProxyAware<ShipmentOfferRequestService>, CustomShipmentOfferRequestService {

	private UserService userService;
	private CartService cartService;
	private CurrencyService currencyService;
	private ShipmentOfferRequestService thisProxy;

	@Override
	public void setThisProxy(ShipmentOfferRequestService serviceProxy) {
		thisProxy = serviceProxy;
	}

	@Override
	public ShipmentOfferRequest createShipmentOffersForCurrentUser() throws ClientException {
		return createShipmentOffersForUser(userService.assertCurrentUserExistsAsReference());
	}

	@Override
	public ShipmentOfferRequest createShipmentOffersForUser(User cartOwner) throws ClientException {
		Cart cart = cartService.readCartByUserIdentifier(cartOwner.getIdentifier());
		ShipmentOfferRequest req = new ShipmentOfferRequestImpl();
		try {
			req.setCart(thisProxy.modelAsReferableResource(cart).buildReference());
		} catch (ReferenceBuildingException e) {
			req.setCart(cart);
		}
		req.setUser(cartOwner);
		req.setCurrency(currencyService.assertEuroCurrencyExists());
		return thisProxy.create(req);
	}

	@Override
	public List<ShipmentOfferRequest> findByUserIdentifier(String uid) throws ClientException {
		ShipmentOfferRequest req = new ShipmentOfferRequestImpl();
		User u = new UserImpl();
		u.setIdentifier(uid);
		req.setUser(u);
		return thisProxy.findAllLike(req, ArrayList.class);
	}

	@ServiceReference
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@ServiceReference
	public void setCartService(CartService cartService) {
		this.cartService = cartService;
	}

	@ServiceReference
	public void setCurrencyService(CurrencyService currencyService) {
		this.currencyService = currencyService;
	}
}
