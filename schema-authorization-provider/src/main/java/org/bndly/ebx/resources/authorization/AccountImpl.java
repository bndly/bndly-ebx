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

import org.bndly.common.crypto.api.CryptoException;
import org.bndly.common.crypto.api.SaltedHashResult;
import org.bndly.ebx.model.BackendAccount;
import org.bndly.ebx.model.BackendAccountActivation;
import org.bndly.rest.api.account.Account;
import org.bndly.rest.api.account.AccountActivationException;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Date;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
abstract class AccountImpl implements Account {

	private static final Logger LOG = LoggerFactory.getLogger(AccountImpl.class);
	private final BackendAccount backendAccount;

	public AccountImpl(BackendAccount backendAccount) {
		if (backendAccount == null) {
			throw new IllegalArgumentException("backendAccount is not allowed to be null");
		}
		this.backendAccount = backendAccount;
	}

	@Override
	public final boolean lock() {
		backendAccount.setLocked(Boolean.TRUE);
		try {
			((ActiveRecord)backendAccount).update();
			return true;
		} catch (Exception e) {
			LOG.error("couldn't lock account", e);
			return false;
		}
	}

	@Override
	public final boolean unlock() {
		backendAccount.setLocked(Boolean.FALSE);
		try {
			((ActiveRecord)backendAccount).update();
			return true;
		} catch (Exception e) {
			LOG.error("couldn't unlock account", e);
			return false;
		}
	}

	@Override
	public boolean checkPassword(String password) {
		if (password == null) {
			return false;
		}
		String secret = backendAccount.getSecret();
		if (secret == null) {
			return false;
		}
		String salt = backendAccount.getSalt();
		if (salt == null) {
			return false;
		}
		SaltedHashResult hashResult = saltAndHash(password, salt);
		return secret.equals(hashResult.getHashBase64());
	}

	@Override
	public boolean deactivate() throws AccountActivationException {
		Boolean active = backendAccount.getActive();
		active = active != null && active;
		if (active) {
			// set inactive
			// explicitly set secret and salt to null
			backendAccount.setActive(Boolean.FALSE);
			backendAccount.setSalt(null);
			backendAccount.setSecret(null);
			try {
				((ActiveRecord) backendAccount).update();
				return true;
			} catch (Exception e) {
				throw new AccountActivationException("could not deactivate account");
			}
		} else {
			// test if the account has a secret and salt.
			// if yes, then set those to null and persist the changes
			String salt = backendAccount.getSalt();
			String secret = backendAccount.getSecret();
			if (salt != null || secret != null) {
				backendAccount.setSalt(null);
				backendAccount.setSecret(null);
				try {
					((ActiveRecord) backendAccount).update();
					return true;
				} catch (Exception e) {
					throw new AccountActivationException("could not deactivate account");
				}
			}
			return false;
		}
	}

	@Override
	public final boolean activate(String token) throws AccountActivationException {
		Boolean active = backendAccount.getActive();
		if (active == null || !active) {
			// activation required
			// the token has to be decrypted with a public key (so we know, that we created the token)
			// the provided token contains a hash of the account name and the salt, that was used for hashing
			// re-hash the account name
			String decrypted;
			try {
				decrypted = decrypt(token);
			} catch (CryptoException e) {
				throw new AccountActivationException("token was tampered", e);
			}
			int i = decrypted.indexOf("|");
			if (i < 0) {
				throw new AccountActivationException("could not activate account, because the token is invalid");
			}
			String hashFromToken = decrypted.substring(0, i);
			String saltFromToken = decrypted.substring(i + 1);
			SaltedHashResult saltedHash = saltAndHash(backendAccount.getName(), saltFromToken);
			if (hashFromToken.equals(saltedHash.getHashBase64())) {
				// the token seems to be valid. now let's load the activation tokens and check, if the token exists.
				Iterator<Record> res = getSchemaBeanFactory().getEngine().getAccessor().query(
						"PICK " + BackendAccountActivation.class.getSimpleName() + " a IF a.backendAccount.name=?", backendAccount.getName()
				);
				while (res.hasNext()) {
					BackendAccountActivation activation = getSchemaBeanFactory().getSchemaBean(BackendAccountActivation.class, res.next());
					if (token.equals(activation.getToken())) {
						// we can activate the account
						backendAccount.setActive(Boolean.TRUE);
						Transaction tx = getSchemaBeanFactory().getEngine().getQueryRunner().createTransaction();
						((ActiveRecord) backendAccount).update(tx);
						((ActiveRecord) activation).delete(tx);
						try {
							tx.commit();
							return true;
						} catch (Exception e) {
							backendAccount.setActive(Boolean.FALSE);
							throw new AccountActivationException("could not activate account due to persistence issues", e);
						}
					}
				}
				throw new AccountActivationException("could not activate account because the token was unknown");
			} else {
				// we can not activate the account, because the token seems invalid
				throw new AccountActivationException("could not activate account, because the token is invalid");
			}
		} else {
			// no activation required
			return false;
		}
	}

	@Override
	public final boolean changePassword(String newPassword) {
		if (newPassword == null) {
			return false;
		}
		SaltedHashResult newSaltedPassowrd = saltAndHash(newPassword);
		backendAccount.setSalt(newSaltedPassowrd.getSaltBase64());
		backendAccount.setSecret(newSaltedPassowrd.getHashBase64());
		try {
			((ActiveRecord)backendAccount).update();
			return true;
		} catch (Exception e) {
			LOG.error("couldn't change account password", e);
			return false;
		}
	}

	@Override
	public final boolean delete() {
		try {
			((ActiveRecord)backendAccount).delete();
			return true;
		} catch (Exception e) {
			LOG.error("couldn't delete account", e);
			return false;
		}
	}

	@Override
	public String createActivationToken() throws AccountActivationException {
		SaltedHashResult saltAndHash = saltAndHash(backendAccount.getName());
		String tokenPlain = saltAndHash.getHashBase64() + "|" + saltAndHash.getSaltBase64();
		String token;
		try {
			token = encrypt(tokenPlain);
		} catch (CryptoException e) {
			throw new AccountActivationException("could not create activation token", e);
		}
		BackendAccountActivation activation = getSchemaBeanFactory().getSchemaBean(
				BackendAccountActivation.class, 
				getSchemaBeanFactory().getRecordFromSchemaBean(backendAccount).getContext().create(BackendAccountActivation.class.getSimpleName())
		);
		activation.setCreatedOn(new Date());
		activation.setBackendAccount(backendAccount);
		activation.setToken(token);
		try {
			((ActiveRecord)activation).persist();
			return token;
		} catch (Exception e) {
			throw new AccountActivationException("could not create activation token", e);
		}
	}

	@Override
	public boolean isActive() {
		Boolean active = backendAccount.getActive();
		return active == null ? false : active;
	}

	@Override
	public boolean isLocked() {
		Boolean locked = backendAccount.getLocked();
		return locked == null ? false : locked;
	}
	
	protected abstract SaltedHashResult saltAndHash(String input, String saltBase64);
	protected abstract SaltedHashResult saltAndHash(String input);
	protected abstract String encrypt(String input) throws CryptoException;
	protected abstract String decrypt(String input) throws CryptoException;
	protected abstract SchemaBeanFactory getSchemaBeanFactory();
	
}
