package org.bndly.ebx.client.impl;

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

import org.bndly.ssl.api.KeyStoreAccessProvider;
import org.bndly.ssl.impl.KeyStoreAccessProviderImpl;

public class TestKeyStoreAccessProviderImpl extends KeyStoreAccessProviderImpl implements KeyStoreAccessProvider {

	@Override
	public String getKeyStoreLocation() {
		String loc = super.getKeyStoreLocation();
		if(loc == null) {
			loc = System.getProperty("ssl.keyStoreLocation");
		}
		return loc;
	}

	@Override
	public String getKeyStorePassword() {
		String pwd = super.getKeyStorePassword();
		if(pwd == null) {
			pwd = System.getProperty("ssl.keyStorePassword");
		}
		return pwd;
	}
    
}
