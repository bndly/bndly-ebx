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
import org.bndly.common.crypto.api.HashService;
import org.bndly.common.crypto.api.SaltedHashResult;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.common.osgi.util.ServiceRegistrationBuilder;
import org.bndly.ebx.model.Authorization;
import org.bndly.ebx.model.BackendAccount;
import org.bndly.ebx.model.Permission;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.SecurityHandler;
import static org.bndly.rest.api.SecurityHandler.AuthorizationProvider.ANONYMOUS_USERNAME;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.listener.DeleteListener;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.api.listener.SchemaDeploymentListener;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Schema;
import org.bndly.schema.model.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SchemaBasedAuthorizationProvider.Configuration.class)
public class SchemaBasedAuthorizationProvider implements SecurityHandler.AuthorizationProvider, PersistListener, MergeListener, DeleteListener {

	private ServiceRegistration<SecurityHandler.AuthorizationProvider> serviceRegistration;

	@ObjectClassDefinition(
			name = "Schema Based Authorization Provider"
	)
	public @interface Configuration {
		@AttributeDefinition(name = "Create Default Account", description = "If active, a default account will be automatically created, if there is not even a single account available.")
		boolean createDefaultAccount() default false;
		
		@AttributeDefinition(name = "Cache permissions", description = "If true, permissions will be cached. This means not every request will lead to a database hit.")
		boolean cachePermissions() default true;
		
		@AttributeDefinition(name = "Cache accounts", description = "If true, accounts will be cached. This means not every request will lead to a database hit.")
		boolean cacheAccounts() default true;
		
		@AttributeDefinition(name = "Cached accounts", description = "A list of account names, that should be cached, if they exist.")
		String[] cachedAccounts() default {"ebx", "ANONYMOUS"};
		
		@AttributeDefinition(name = "Hash service", description = "The target reference for the hash service to use, e.g. use target=(name=...) to bind to services by name.")
		String hashService_target() default "(name=default)";

		@AttributeDefinition(name = "Skip service registration", description = "Skips the registration of this component as a service. This may be useful, when debugging an application and you fail to access the webconsole.")
		boolean skipServiceRegistration() default false;
		
	}
	
	@Reference
	private Base64Service base64Service;
	@Reference(name = "hashService")
	private HashService hashService;
	
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	// in order to access these fields, please use the writeAccess or readAccess methods
	private BackendAccountImpl _anonymousAccount;
	private final Cache<String, BackendAccountImpl> _backendAccountsByName = new Cache<String, BackendAccountImpl>(){

		@Override
		protected String buildKey(BackendAccountImpl item) {
			return item.getName();
		}

		@Override
		protected void afterReplacement(BackendAccountImpl oldItem, BackendAccountImpl newItem) {
			if (ANONYMOUS_USERNAME.equals(oldItem.getName())) {
				_anonymousAccount = null;
			}
			if (ANONYMOUS_USERNAME.equals(newItem.getName())) {
				_anonymousAccount = newItem;
			}
			_backendAccountsById.put(newItem);
		}

		@Override
		protected void afterRemoval(BackendAccountImpl oldItem) {
			if (ANONYMOUS_USERNAME.equals(oldItem.getName())) {
				_anonymousAccount = null;
			}
			_backendAccountsById.dropByEntry(oldItem);
		}
	};
	private final Cache<Long, BackendAccountImpl> _backendAccountsById = new Cache<Long, BackendAccountImpl>(){

		@Override
		protected Long buildKey(BackendAccountImpl item) {
			return item.getId();
		}

		@Override
		protected void afterReplacement(BackendAccountImpl oldItem, BackendAccountImpl newItem) {
			if (ANONYMOUS_USERNAME.equals(oldItem.getName())) {
				_anonymousAccount = null;
			}
			if (ANONYMOUS_USERNAME.equals(newItem.getName())) {
				_anonymousAccount = newItem;
			}
			_backendAccountsByName.put(newItem);
		}

		@Override
		protected void afterRemoval(BackendAccountImpl oldItem) {
			if (ANONYMOUS_USERNAME.equals(oldItem.getName())) {
				_anonymousAccount = null;
			}
			_backendAccountsByName.dropByEntry(oldItem);
		}
	};
	private final Cache<Long, PermissionImpl> _permissionsById = new Cache<Long, PermissionImpl>(){

		@Override
		protected Long buildKey(PermissionImpl item) {
			return item.getId();
		}

		@Override
		protected void afterReplacement(PermissionImpl oldItem, PermissionImpl newItem) {
			// TODO: find all the authorizations for this permission.
			// TODO: alter the authorizations
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		protected void afterRemoval(PermissionImpl oldItem) {
			// TODO: find all the authorizations for this permission.
			// TODO: drop the authorizations
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	};
	
	private Type backendAccountType;
	private Type permissionType;
	private Type authorizationType;
	private SchemaDeploymentListener deploymentHook;
	private boolean createDefaultAccount;
	
	private Lock readLock;
	private Lock writeLock;
	private boolean cachePermissions;
	private boolean cacheAccounts;
	private Set<String> cachedAccounts;
	
	private class PermissionImpl {
		private final Permission permission;
		private final String name;
		private final Pattern pattern;
		private final long id;

		public PermissionImpl(Permission permission) {
			if (permission == null) {
				throw new IllegalArgumentException("permission is not allowed to be null");
			}
			this.permission = permission;
			this.name = permission.getName();
			Pattern compiled;
			try {
				compiled = Pattern.compile(name);
			} catch (Exception e) {
				// this seems to be a different permission
				compiled = null;
			}
			this.pattern = compiled;
			id = ((ActiveRecord) permission).getId();
		}

		public Permission getPermission() {
			return permission;
		}

		public String getName() {
			return name;
		}

		public long getId() {
			return id;
		}
		
	}
	
	private class AuthorizationImpl {
		private final Authorization authorization;

		public AuthorizationImpl(Authorization authorization) {
			if (authorization == null) {
				throw new IllegalArgumentException("authorization is not allowed to be null");
			}
			this.authorization = authorization;
		}

		public Authorization getAuthorization() {
			return authorization;
		}

	}
	
	private class BackendAccountImpl {
		private final BackendAccount backendAccount;
		private final boolean active;
		private final boolean locked;
		private final byte[] salt;
		private final String hashedSecret;
		private final String name;
		private final long id;

		public BackendAccountImpl(BackendAccount backendAccount) {
			if (backendAccount == null) {
				throw new IllegalArgumentException("backendAccount is not allowed to be null");
			}
			this.backendAccount = backendAccount;
			Boolean activeTmp = backendAccount.getActive();
			if (activeTmp == null) {
				activeTmp = false;
			}
			active = activeTmp;
			Boolean lockedTmp = backendAccount.getLocked();
			if (lockedTmp == null) {
				lockedTmp = false;
			}
			locked = lockedTmp;
			String salt64 = backendAccount.getSalt();
			if (salt64 != null) {
				salt = base64Service.base64Decode(salt64);
			} else {
				salt = null;
			}
			hashedSecret = backendAccount.getSecret();
			name = backendAccount.getName();
			id = ((ActiveRecord) backendAccount).getId();
		}

		public BackendAccount getBackendAccount() {
			return backendAccount;
		}

		private boolean isActive() {
			return active;
		}
		
		private boolean isLocked() {
			return locked;
		}

		private Boolean isPasswordMatching(String password) {
			if (salt == null) {
				return false;
			}
			SaltedHashResult hasedPassword = hashService.hash(password, salt);
			boolean matches = hasedPassword.getHashBase64().equals(hashedSecret);
			return matches;
		}

		private String getName() {
			return name;
		}

		private long getId() {
			return id;
		}

	}
	
	@Activate
	public void activate(ComponentContext componentContext) {
		ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
		this.readLock = reentrantReadWriteLock.readLock();
		this.writeLock = reentrantReadWriteLock.writeLock();
		
		DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties());
		createDefaultAccount = dictionaryAdapter.getBoolean("createDefaultAccount", Boolean.FALSE);
		cachePermissions = dictionaryAdapter.getBoolean("cachePermissions", Boolean.TRUE);
		cacheAccounts = dictionaryAdapter.getBoolean("cacheAccounts", Boolean.TRUE);
		Collection<String> tmp = dictionaryAdapter.getStringCollection("cachedAccounts", "ebx", "ANONYMOUS");
		this.cachedAccounts = tmp == null ? Collections.EMPTY_SET : new HashSet<>(tmp);
		Schema ds = schemaBeanFactory.getEngine().getDeployer().getDeployedSchema();
		if (ds != null) {
			findBackendAccountTypeInSchema(ds);
			writeAccess(new Runnable() {

				@Override
				public void run() {
					runnableLoadAccountsAndPermissionsIntoMemory(schemaBeanFactory.getEngine()).run();
					runnableCreateDefaultAccount().run();
				}
			});
		} else {
			deploymentHook = new SchemaDeploymentListener() {

				@Override
				public void schemaDeployed(Schema deployedSchema, final Engine engine) {
					findBackendAccountTypeInSchema(deployedSchema);
					writeAccess(new Runnable() {

						@Override
						public void run() {
							runnableLoadAccountsAndPermissionsIntoMemory(engine).run();
							runnableCreateDefaultAccount().run();
						}
					});
				}

				@Override
				public void schemaUndeployed(Schema deployedSchema, Engine engine) {
					if ("ebx".equals(deployedSchema.getName())) {
						backendAccountType = null;
						permissionType = null;
						authorizationType = null;
					}
				}
			};
			schemaBeanFactory.getEngine().addListener(deploymentHook);
		}
		schemaBeanFactory.getEngine().addListener(this);

		if(!dictionaryAdapter.getBoolean("skipServiceRegistration", Boolean.FALSE)) {
			serviceRegistration = ServiceRegistrationBuilder.newInstance(SecurityHandler.AuthorizationProvider.class, this)
					.register(componentContext.getBundleContext());
		}
	}
	
	private Runnable runnableCreateDefaultAccount() {
		return new Runnable() {

			@Override
			public void run() {
				if (createDefaultAccount) {
					if (_backendAccountsById.isEmpty()) {
						BackendAccount account = createAccount("ebx", "ebx");
						BackendAccountImpl backendAccountImpl = new BackendAccountImpl(account);
						_backendAccountsById.put(backendAccountImpl);
						_backendAccountsByName.put(backendAccountImpl);
					}
				}
			}
		};
	}
	
	private Runnable runnableLoadAccountsAndPermissionsIntoMemory(final Engine engine) {
		return new Runnable() {

			@Override
			public void run() {
				Accessor accessor = engine.getAccessor();
				RecordContext recordContext = accessor.buildRecordContext();
				if (cacheAccounts) {
					for (String cachedAccount : cachedAccounts) {
						Iterator<Record> backendAccountIter = accessor.query("PICK BackendAccount b IF b.name=? LIMIT ?", recordContext, null, cachedAccount, 1);
						if (backendAccountIter.hasNext()) {
							Record next = backendAccountIter.next();
							BackendAccount backendAccount = schemaBeanFactory.getSchemaBean(BackendAccount.class, next);
							String name = backendAccount.getName();
							if (name != null) {
								BackendAccountImpl backendAccountImpl = new BackendAccountImpl(backendAccount);
								_backendAccountsById.put(backendAccountImpl);
								_backendAccountsByName.put(backendAccountImpl);
							}
						}
					}
				}
				if (cachePermissions) {
					Iterator<Record> permissionIter = accessor.query("PICK Permission", recordContext, null);
					while (permissionIter.hasNext()) {
						Record next = permissionIter.next();
						Permission permission = schemaBeanFactory.getSchemaBean(Permission.class, next);
						String name = permission.getName();
						if (name != null) {
							PermissionImpl permissionImpl = new PermissionImpl(permission);
							_permissionsById.put(permissionImpl);
						}
					}
				}
			}
		};
	}
	
	private void findBackendAccountTypeInSchema(Schema schema) {
		if ("ebx".equals(schema.getName())) {
			List<Type> types = schema.getTypes();
			for (Type type : types) {
				if (BackendAccount.class.getSimpleName().equals(type.getName())) {
					backendAccountType = type;
				} else if (Permission.class.getSimpleName().equals(type.getName())) {
					permissionType = type;
				} else if (Authorization.class.getSimpleName().equals(type.getName())) {
					authorizationType = type;
				}
			}
		}
	}
	
	@Deactivate
	public void deactivate() {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
		if (deploymentHook != null) {
			schemaBeanFactory.getEngine().removeListener(deploymentHook);
		}
		schemaBeanFactory.getEngine().removeListener(this);
		writeAccess(new Runnable() {

			@Override
			public void run() {
				_backendAccountsByName.clear();
				_backendAccountsById.clear();
				_permissionsById.clear();
				_anonymousAccount = null;
			}
		});
	}
	
	private Runnable runnablePutBackendAccount(final BackendAccount backendAccount) {
		return new Runnable() {

			@Override
			public void run() {
				BackendAccountImpl ba = new BackendAccountImpl(backendAccount);
				_backendAccountsByName.put(ba);
				_backendAccountsById.put(ba);
			}
		};
	}
	private Runnable runnablePutPermission(final Permission permission) {
		return new Runnable() {

			@Override
			public void run() {
				PermissionImpl permissionImpl = new PermissionImpl(permission);
				_permissionsById.put(permissionImpl);
			}
		};
	}
	private Runnable runnableRemoveBackendAccountById(final long id) {
		return new Runnable() {

			@Override
			public void run() {
				_backendAccountsById.dropByKey(id);
			}
		};
	}
	private Runnable runnableRemovePermissionById(final long id) {
		return new Runnable() {

			@Override
			public void run() {
				_permissionsById.dropByKey(id);
			}
		};
	}
	
	@Override
	public void onPersist(Record record) {
		if (record.getType() == backendAccountType) {
			BackendAccount ba = schemaBeanFactory.getSchemaBean(BackendAccount.class, record);
			if (isCachedBackendAccount(ba)) {
				writeAccess(runnablePutBackendAccount(ba));
			}
		} else if (record.getType() == permissionType) {
			if (cachePermissions) {
				Permission permission = schemaBeanFactory.getSchemaBean(Permission.class, record);
				writeAccess(runnablePutPermission(permission));
			}
		}
	}

	@Override
	public void onMerge(Record record) {
		if (record.getType() == backendAccountType) {
			BackendAccount ba = schemaBeanFactory.getSchemaBean(BackendAccount.class, record);
			if (isCachedBackendAccount(ba)) {
				writeAccess(runnablePutBackendAccount(ba));
			}
		} else if (record.getType() == permissionType) {
			if (cachePermissions) {
				Permission permission = schemaBeanFactory.getSchemaBean(Permission.class, record);
				writeAccess(runnablePutPermission(permission));
			}
		}
	}
	
	@Override
	public void onDelete(Record record) {
		if (record.getType() == backendAccountType) {
			writeAccess(runnableRemoveBackendAccountById(record.getId()));
		} else if (record.getType() == permissionType) {
			if (cachePermissions) {
				writeAccess(runnableRemovePermissionById(record.getId()));
			}
		}
	}
	
	private boolean isCachedBackendAccount(BackendAccount ba) {
		return cachedAccounts.contains(ba.getName());
	}

	@Override
	public boolean isAnonymousAllowed(Context context) {
		return readAccess(new CallableInternal<Boolean>() {

			@Override
			public Boolean call() {
				if (_anonymousAccount == null) {
					return false;
				}
				return _anonymousAccount.isActive();
			}
		});
	}
	
	@Override
	public boolean isAuthorized(final Context context, final String user, final String password) {
		return readAccess(new CallableInternal<Boolean>() {

			@Override
			public Boolean call() {
				if (cacheAccounts && cachedAccounts.contains(user)) {
					BackendAccountImpl backendAccount = _backendAccountsByName.get(user);
					if (backendAccount != null) {
						if (!backendAccount.isActive() || backendAccount.isLocked()) {
							return false;
						}
						return backendAccount.isPasswordMatching(password);
					} else {
						return false;
					}
				} else {
					Accessor accessor = schemaBeanFactory.getEngine().getAccessor();
					RecordContext recordContext = accessor.buildRecordContext();
					Iterator<Record> res = accessor.query("PICK BackendAccount b IF b.name=? LIMIT ?", recordContext, null, user, 1);
					if (res.hasNext()) {
						BackendAccountImpl backendAccount = new BackendAccountImpl(schemaBeanFactory.getSchemaBean(BackendAccount.class, res.next()));
						if (!backendAccount.isActive() || backendAccount.isLocked()) {
							return false;
						}
						return backendAccount.isPasswordMatching(password);
					}
					return false;
				}
			}
		});
	}
	
	public BackendAccount createAccount(String name, String password) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("name is not allowed to be empty");
		}
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("password is not allowed to be empty");
		}
		BackendAccount backendAccount = schemaBeanFactory.getSchemaBean(BackendAccount.class, schemaBeanFactory.getEngine().getAccessor().buildRecordContext().create(BackendAccount.class.getSimpleName()));
		backendAccount.setActive(Boolean.TRUE);
		backendAccount.setName(name);
		SaltedHashResult hashResult = hashService.hash(password);
		backendAccount.setSalt(hashResult.getSaltBase64());
		backendAccount.setSecret(hashResult.getHashBase64());
		((ActiveRecord)backendAccount).persist();
		return backendAccount;
	}
	
	private static interface CallableInternal<E> extends Callable<E> {

		@Override
		public E call();
		
	}
	
	private <E> E writeAccess(CallableInternal<E> callable) {
		try {
			writeLock.lock();
			return callable.call();
		} finally {
			writeLock.unlock();
		}
	}
	
	private <E> E readAccess(CallableInternal<E> callable) {
		try {
			writeLock.lock();
			return callable.call();
		} finally {
			writeLock.unlock();
		}
	}
	
	private void writeAccess(Runnable runnable) {
		try {
			writeLock.lock();
			runnable.run();
		} finally {
			writeLock.unlock();
		}
	}
	
	private void readAccess(Runnable runnable) {
		try {
			writeLock.lock();
			runnable.run();
		} finally {
			writeLock.unlock();
		}
	}

}
