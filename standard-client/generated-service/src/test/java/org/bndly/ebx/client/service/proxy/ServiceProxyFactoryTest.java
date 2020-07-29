package org.bndly.ebx.client.service.proxy;

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

import org.bndly.common.service.shared.proxy.ServiceProxyFactory;
import org.bndly.ebx.client.service.api.AddressService;
import org.bndly.ebx.client.service.api.CountryService;
import org.bndly.ebx.model.Cart;
import org.bndly.ebx.model.Country;
import org.bndly.ebx.model.impl.CartImpl;
import org.bndly.ebx.model.impl.CountryImpl;
import org.bndly.ebx.client.service.api.CartService;
import org.bndly.ebx.client.service.api.CustomCartService;
import org.bndly.ebx.client.service.api.CustomCountryService;
import org.bndly.ebx.client.service.api.DefaultCountryService;
import org.bndly.ebx.client.service.api.DefaultCreditCardBrandService;
import org.bndly.ebx.client.service.impl.DefaultCartServiceImpl;
import org.bndly.ebx.client.service.impl.DefaultCountryServiceImpl;
import org.bndly.ebx.client.service.impl.DefaultCreditCardBrandServiceImpl;
import org.bndly.ebx.client.service.api.DefaultCartService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class ServiceProxyFactoryTest {

	ServiceProxyFactory proxyFactory = null;

	DefaultCartService defaultCartService = null;
	CustomCartService customCartService = null;

	DefaultCountryService defaultCountryService = null;
	CustomCountryService customCountryService = null;

	DefaultCreditCardBrandService defaultCreditCardBrandService = null;
	DefaultCreditCardBrandService customCreditCardBrandService = null;

	@BeforeMethod
	public void instantiateTestObjects() {
		proxyFactory = new ServiceProxyFactory();

		defaultCartService = new DefaultCartServiceImpl();
		customCartService = new CustomCartServiceImpl();

		defaultCountryService = new DefaultCountryServiceImpl();
		customCountryService = new CustomCountryServiceImpl();

		defaultCreditCardBrandService = new DefaultCreditCardBrandServiceImpl();
	//	customCreditCardBrandService = new CustomCreditCardBrandServiceImpl();
	}

	@Test
	public void testProxyFactory() {
		try {
			Assert.assertTrue(proxyFactory.getInstance(CartService.class, defaultCartService, customCartService) instanceof CartService);
			Assert.assertFalse(((Object)proxyFactory.getInstance(CartService.class, defaultCartService, customCartService)) instanceof AddressService);

			CartService cartServiceOne = (CartService) proxyFactory.getInstance(CartService.class, defaultCartService, customCartService);
			CartService cartServiceTwo = (CartService) proxyFactory.getInstance(CartService.class, defaultCartService, customCartService);

			System.out.println(cartServiceOne.hashCode());
			System.out.println(cartServiceTwo.hashCode());


			Assert.assertNotSame(cartServiceOne, cartServiceTwo);
//			Assert.assertTrue();
//			Assert.assertEquals(cartServiceOne, cartServiceTwo);
		}
		catch (Exception ex)
		{
			Assert.fail("Instantiation of CartService fails" + ex.getMessage(), ex);
		}
	}

	@Test
	public void testFullAPIProxy() {
		try {
			// Test cartService
			Cart cartTwo = new CartImpl();
			cartTwo.setTotalItemCount(3l);
			cartTwo.setUserIdentifier("meinUser");

			CartService cartService = (CartService) proxyFactory.getInstance(CartService.class, defaultCartService, customCartService);
			Assert.assertTrue(cartService.getDefaultServiceName().toLowerCase().contains("cartservice"));

			Cart cartOne = cartService.readCartOfCurrentUser();
			Assert.assertTrue(cartOne.getTotalItemCount() == 5l);
			Assert.assertEquals(cartOne.getUserIdentifier(), "cartUser");
		}
		catch (Exception ex)
		{
			Assert.fail("Instantiation of CartService fails" + ex.getMessage(), ex);
		}

		try {
			// Test countryService
			CountryService countryService = (CountryService) proxyFactory.getInstance(CountryService.class, defaultCountryService, customCountryService);
			Assert.assertTrue(countryService.getDefaultServiceName().toLowerCase().contains("countryservice"));

			Country myCountry = countryService.readById(123l);

			Country c = new CountryImpl();
			c.setLabel("meinLabel");
			Assert.assertNotEquals(c.getLabel(), myCountry.getLabel());

//			countryService.find(c);
//			Assert.assertEquals(c.getLabel(), myCountry.getLabel());
		}
		catch (Exception ex)
		{
			Assert.fail("Instantiation of CountryService fails" + ex.getMessage(), ex);
		}
	}
}
