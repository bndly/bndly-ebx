# eBX Schema Authorization Provider

## Introduction
The purpose of the authorization provider is use the eBX entity `BackendAccount` for the HTTP Basic Auth on the REST API. Furthermore this bundle also provides an implementation of an `AccountStore`, that allows to map a typical account lifecycle to the `BackendAccount` entity.

## Average account lifecycle
The lifecycle of an account starts with the account creation. The account remain in an inactive state. This means, that the account can not be used for secured access. In order to activate the account, an activation token has to be requested. This token acts as a One-Time-Password to activate the account. No matter if the account is activated or not, the account can be locked or unlocked. Locked account will also not be able to perform a secured access. The difference between activation and unlocking is, that unlocking does not require a token, while activation always requires a token. The password of the account can be changed in any state of the account. The account may also be deleted in any state.

## Storage of passwords
Passwords of accounts will not be stored as plain text. The password will be converted to a salted hash using a `org.bndly.common.crypto.api.HashService` instance, that needs to be configured in `org.bndly.ebx.resources.authorization.SchemaBasedAccountStore`. The size of the salt is configured also in the `SchemaBasedAccountStore` via the `secureRandomLength` property.

## Verification of passwords
When a secured access is about to be performed, the authorization provider needs to verify the provided plain text password for a given account. Since the password is only stored as a hash, the same `org.bndly.common.crypto.api.HashService` as in `org.bndly.ebx.resources.authorization.SchemaBasedAccountStore` needs to be configured within `org.bndly.ebx.resources.authorization.SchemaBasedAuthorizationProvider`.

## Caching of account
Since there may be lots of secured accesses at a time, eBX allows to cache a set of accounts in memory. This means, that the password verifications do not lead to database access to load the `BackendAccount` over and over again. In order to make use of the caching the `cachedAccounts` and `cacheAccounts` properties need to be set on `org.bndly.ebx.resources.authorization.SchemaBasedAuthorizationProvider`.

Here is an example of the default configuration:

```
cacheAccounts=true
cachedAccounts=ebx,ANONYMOUS
```

The configuration says, that caching is turned on in general. Furthermore the configuration says, that the account `ebx` and the account `ANONYMOUS` shall be cached, if they exist or are created. The accounts will _not_ be automatically created just because they are listed within this property.

## Anonymous access
In order to allow anonymous users/clients to perform a secured access, the account `ANONYMOUS` must exist and needs to be activated. If this is the case, then the REST API may be accessed even without providing Basic Auth information.