package org.bndly.ebx.model.impl;

/*-
 * #%L
 * org.bndly.ebx.client.generated-model
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

import org.bndly.common.service.model.api.AbstractEntity;
import org.bndly.common.service.model.api.ReferableResource;
import org.bndly.common.service.model.api.ReferenceAttribute;
import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.ebx.model.Country;
import org.bndly.ebx.model.Currency;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class ModelReferenceTest {
	public static class Foo extends AbstractEntity<Foo> {
		@ReferenceAttribute
		public String unique;
	}
	
	@Test
	public void testReferences() throws ReferenceBuildingException {
		Foo foo = new Foo();
		foo.unique = "abc";
		
		Assert.assertFalse(foo.isResourceReference());
		Foo fooRef = foo.buildReference();
		Assert.assertNotNull(fooRef);
		Assert.assertTrue(fooRef != foo);

		Assert.assertTrue(fooRef.isReferenceFor(foo));
		Assert.assertTrue(fooRef == fooRef.buildReference());
		
		try {
			new Foo().buildReference();
			Assert.fail("expected a ReferenceBuildingException");
		} catch(ReferenceBuildingException e) {
		}
	}
	
	@Test
	public void testDomainReferences() throws ReferenceBuildingException {
		CountryImpl country = new CountryImpl();
		country.setId(1337L);

		Assert.assertFalse(country.isResourceReference());
		Country ref = country.buildReference();
		Assert.assertNotNull(ref);
		Assert.assertTrue(ref != country);
		
		Assert.assertTrue(((ReferableResource)ref).isReferenceFor(country));
		Assert.assertTrue(ref == ((ReferableResource)ref).buildReference());
		
		try {
			new CountryImpl().buildReference();
			Assert.fail("expected a ReferenceBuildingException");
		} catch(ReferenceBuildingException e) {
		}
	}
	
	@Test
	public void testDomainReferences2() throws ReferenceBuildingException {
		CurrencyImpl currency = new CurrencyImpl();
		currency.setCode("EUR");

		Assert.assertFalse(currency.isResourceReference());
		Currency ref = currency.buildReference();
		Assert.assertNotNull(ref);
		Assert.assertTrue(ref != currency);
		
		Assert.assertTrue(((ReferableResource)ref).isReferenceFor(currency));
		Assert.assertTrue(ref == ((ReferableResource)ref).buildReference());

		Assert.assertEquals(ref.getCode(), "EUR");
	}
}
