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

import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.CountryService;
import org.bndly.ebx.client.service.api.UserGeoLocationProvider;
import org.bndly.ebx.model.Country;
import org.bndly.ebx.model.impl.CountryImpl;
import org.bndly.ebx.client.service.api.CustomCountryService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 17.06.15.
 */
public class CustomCountryServiceImpl implements CustomCountryService, ProxyAware<CountryService> {

    private UserGeoLocationProvider userGeoLocationProvider;
    private CountryService thisProxy;

    @Override
    public Country readById(long id) throws ClientException {
        CountryImpl c = new CountryImpl();
        c.setId(id);
		try {
			return thisProxy.find(c);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public Country getDefault() throws ClientException {
        String globalDefaultCode = "DE";
        String defaultCode = globalDefaultCode;
        if(userGeoLocationProvider != null) {
            String ucode = userGeoLocationProvider.getCurrentUserGeoLocation();
            if(ucode != null) {
                ucode = ucode.trim().toUpperCase();
                if(ucode.length() == 2) {
                    defaultCode = ucode;
                }
            }
        }
        Country defaultCountry = new CountryImpl();
        defaultCountry.setIsoCode2(defaultCode);
		try {
			return thisProxy.find(defaultCountry);
		} catch (UnknownResourceClientException e) {
			defaultCountry.setIsoCode2(globalDefaultCode);
			try {
				return thisProxy.find(defaultCountry);
			} catch (UnknownResourceClientException e2) {
				return null;
			}
		}
    }

    @Override
    public void setThisProxy(CountryService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
