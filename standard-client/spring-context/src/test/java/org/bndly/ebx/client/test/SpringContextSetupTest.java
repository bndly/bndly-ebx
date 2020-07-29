package org.bndly.ebx.client.test;

/*-
 * #%L
 * org.bndly.ebx.client.spring-context
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

import org.bndly.common.service.setup.ClientSetup;
import org.bndly.common.service.setup.SchemaReference;
import org.bndly.common.service.setup.SchemaServiceConstructionGuide;
import org.bndly.common.service.setup.SchemaServiceStub;
import org.bndly.common.service.setup.ServiceReference;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.client.impl.TestUserIDProviderImpl;
import org.bndly.ebx.client.service.api.CartItemService;
import org.bndly.ebx.client.service.api.CountryService;
import org.bndly.ebx.client.service.api.CartService;
import org.bndly.ebx.client.service.impl.CustomCartItemServiceImpl;
import org.bndly.ebx.client.service.impl.CustomCartServiceImpl;
import org.bndly.ebx.client.service.impl.CustomCountryServiceImpl;
import org.bndly.ebx.client.service.impl.DefaultCartItemServiceImpl;
import org.bndly.ebx.client.service.impl.DefaultCartServiceImpl;
import org.bndly.ebx.client.service.impl.DefaultCountryServiceImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SpringContextSetupTest {
    
	@Test
	public void testSetup() throws ClassNotFoundException {
		SchemaServiceStub countrySchemaServiceStub = new SchemaServiceStub();
		countrySchemaServiceStub.setSchemaName("ebx");
		countrySchemaServiceStub.setCustomServiceClassName(CustomCountryServiceImpl.class.getName());
		countrySchemaServiceStub.setGenericServiceClassName(DefaultCountryServiceImpl.class.getName());
		countrySchemaServiceStub.setFullApiClassName(CountryService.class.getName());
		
		SchemaServiceStub cartItemschemaServiceStub = new SchemaServiceStub();
		cartItemschemaServiceStub.setSchemaName("ebx");
		cartItemschemaServiceStub.setCustomServiceClassName(CustomCartItemServiceImpl.class.getName());
		cartItemschemaServiceStub.setGenericServiceClassName(DefaultCartItemServiceImpl.class.getName());
		cartItemschemaServiceStub.setFullApiClassName(CartItemService.class.getName());
		
		SchemaServiceStub cartSchemaServiceStub = new SchemaServiceStub();
		cartSchemaServiceStub.setSchemaName("ebx");
		cartSchemaServiceStub.setCustomServiceClassName(CustomCartServiceImpl.class.getName());
		cartSchemaServiceStub.setGenericServiceClassName(DefaultCartServiceImpl.class.getName());
		cartSchemaServiceStub.setFullApiClassName(CartService.class.getName());
		
		SchemaReference sr = new SchemaReference("ebx", "org.bndly.rest.beans.ebx");
		sr.setSchemaBeanPackage("org.bndly.ebx.model");
		sr.setModelImplPackage("org.bndly.ebx.model.impl");
		ClientSetup clientSetup = new ClientSetup()
				.setHostUrl("http://localhost:8081/bndly/")
				.setDefaultLanguage("en")
				.addSchemaReference(sr)
				.addSchemaServiceConstructionGuide(new SchemaServiceConstructionGuide("ebx", "org.bndly.shop.client.service.impl", "org.bndly.shop.client.service.api"))
				.addSchemaServiceStub(countrySchemaServiceStub)
				.addSchemaServiceStub(cartItemschemaServiceStub)
				.addSchemaServiceStub(cartSchemaServiceStub)
				.addServiceReference(ServiceReference.buildByType(UserIDProvider.class, new TestUserIDProviderImpl()))
				.addJAXBMessageClassProvider(new org.bndly.rest.common.beans.JAXBMessageClassProviderImpl())
				.addJAXBMessageClassProvider(new org.bndly.rest.schema.beans.JAXBMessageClassProviderImpl())
				.addJAXBMessageClassProvider(new org.bndly.rest.beans.ebx.JAXBMessageClassProviderImpl())
				.init();
		CountryService countryService = (CountryService) countrySchemaServiceStub.getFullApi();
		Assert.assertNotNull(countryService);
//		Collection<Country> all = countryService.listAll();
//		Assert.assertNotNull(all);
//		UserService userService = clientSetup.getServiceByType(UserService.class);
//		Assert.assertNotNull(userService);
	}
	
//    @Test
//    public void checkStuff() {
//		ClientSetup setup = new ClientSetup()
//				.setHost("http://localhost:8081/bndly")
//				.setDefaultLanguage("en")
//				.setCustomService(EventBus.class, new EventBusImpl())
//				.setCustomService(ContentService.class, new TestContentServiceImpl())
//				.setCustomService(UserIDProvider.class, new TestUserIDProviderImpl());
//		setup.activate();
//        Assert.assertNotNull(setup.getService(CountryMapper.class));
//        Assert.assertNotNull(setup.getService(IShopServicePool.class));
//        Assert.assertNotNull(setup.getService(KeyStoreAccessProvider.class));
//        Assert.assertNotNull(setup.getService(LanguageProvider.class));
//		IShopServicePool sp = setup.getService(IShopServicePool.class);
//		Assert.assertNotNull(sp.getService(Country.class));
//		GenericResourceService<Country> countryService = sp.getService(Country.class);
////		Collection<Country> allCountries = countryService.listAll();
//    }
}
