package org.bndly.ebx.resources.authorization;

/*-
 * #%L
 * org.bndly.ebx.schema-authorization-provider
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

import org.bndly.common.crypto.api.Base64Service;
import org.bndly.common.crypto.api.CryptoException;
import org.bndly.common.crypto.api.HashService;
import org.bndly.common.crypto.api.SaltedHashResult;
import org.bndly.common.crypto.api.SimpleCryptoService;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.model.BackendAccount;
import org.bndly.rest.api.account.Account;
import org.bndly.rest.api.account.AccountCreationException;
import org.bndly.rest.api.account.AccountStore;
import org.bndly.rest.api.account.NoSuchAccountException;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Iterator;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = AccountStore.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SchemaBasedAccountStore.Configuration.class)
public class SchemaBasedAccountStore implements AccountStore {

	@ObjectClassDefinition(
			name = "Schema based account store"
	)
	public @interface Configuration {

		@AttributeDefinition(
				name = "Secure random length",
				description = "The length of the secure random, that will be used for hashing of passwords and tokens"
		)
		int secureRandomLength() default 8;

		@AttributeDefinition(
				name = "Crypto service",
				description = "The target reference for the crypto service to use, e.g. use target=(name=...) to bind to services by name."
		)
		String simpleCryptoService_target();

		@AttributeDefinition(
				name = "Hash service",
				description = "The target reference for the hash service to use, e.g. use target=(name=...) to bind to services by name."
		)
		String hashService_target() default "(name=default)";

	}
	
	private static final Logger LOG = LoggerFactory.getLogger(SchemaBasedAccountStore.class);
	@Reference
	private Base64Service base64Service;
	@Reference(name = "hashService")
	private HashService hashService;
	@Reference(name = "simpleCryptoService")
	private SimpleCryptoService simpleCryptoService;
	private int secureRandomLength = 8;
	
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	
	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties());
		secureRandomLength = dictionaryAdapter.getInteger("secureRandomLength", 8);
	}

	@Override
	public Account createAccount(String accountName) throws AccountCreationException {
		return createAccount(accountName, null, false, false, false);
	}
	
	@Override
	public Account createAccount(String accountName, String password) throws AccountCreationException {
		return createAccount(accountName, password, true, false, false);
	}

	@Override
	public Account createAccountLocked(String accountName, String password) throws AccountCreationException {
		return createAccount(accountName, password, true, false, true);
	}
	
	private Account createAccount(String accountName, String password, boolean requirePassword, boolean active, boolean locked) throws AccountCreationException {
		if ((password == null && requirePassword) || accountName == null) {
			throw new AccountCreationException("account name and password are mandatory");
		}
		BackendAccount backendAccount = schemaBeanFactory.getSchemaBean(
				BackendAccount.class, 
				schemaBeanFactory.getEngine().getAccessor().buildRecordContext().create(BackendAccount.class.getSimpleName())
		);
		backendAccount.setName(accountName);
		if (requirePassword) {
			byte[] salt = hashService.secureRandom(secureRandomLength);
			SaltedHashResult hasedPassword = hashService.hash(password, salt);
			backendAccount.setSalt(hasedPassword.getSaltBase64());
			backendAccount.setSecret(hasedPassword.getHashBase64());
		}
		backendAccount.setActive(active);
		backendAccount.setLocked(locked);
		try {
			Transaction tx = schemaBeanFactory.getEngine().getQueryRunner().createTransaction();
			((ActiveRecord) backendAccount).persist(tx);
			tx.commit();
			return createAccountImpl(backendAccount);
		} catch (Exception e) {
			throw new AccountCreationException("could not create account due to persistence issues", e);
		}
	}

	@Override
	public Account getAccount(String accountName) throws NoSuchAccountException {
		if (accountName == null) {
			throw new NoSuchAccountException("can not retrieve account without account name");
		}
		Iterator<Record> res = schemaBeanFactory.getEngine().getAccessor().query("PICK " + BackendAccount.class.getSimpleName() + " b IF b.name=? LIMIT ?", accountName, 1);
		if (!res.hasNext()) {
			throw new NoSuchAccountException("could not retrieve account");
		}
		BackendAccount backendAccount = schemaBeanFactory.getSchemaBean(BackendAccount.class, res.next());
		return createAccountImpl(backendAccount);
	}
	
	private AccountImpl createAccountImpl(BackendAccount backendAccount) {
		return new AccountImpl(backendAccount) {
			@Override
			protected SaltedHashResult saltAndHash(String input, String saltBase64) {
				return hashService.hash(input, base64Service.base64Decode(saltBase64));
			}
			
			@Override
			protected SaltedHashResult saltAndHash(String input) {
				byte[] salt = hashService.secureRandom(secureRandomLength);
				return hashService.hash(input, salt);
			}

			@Override
			protected SchemaBeanFactory getSchemaBeanFactory() {
				return schemaBeanFactory;
			}

			@Override
			protected String decrypt(String input) throws CryptoException {
				return simpleCryptoService.decode(base64Service.base64Decode(input));
			}

			@Override
			protected String encrypt(String input) throws CryptoException {
				return base64Service.base64Encode(simpleCryptoService.encode(input));
			}
			
		};
	}
	
}
