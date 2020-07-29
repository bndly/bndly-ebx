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

import org.bndly.common.service.cache.api.CacheKeyParameter;
import org.bndly.common.service.cache.api.CacheLevel;
import org.bndly.common.service.cache.api.Cached;
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.impl.CurrencyImpl;
import org.bndly.ebx.client.service.api.CustomCurrencyService;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.CurrencyService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

/**
 * Created by alexp on 17.06.15.
 */
public class CustomCurrencyServiceImpl implements CustomCurrencyService, ProxyAware<CurrencyService> {

    private CurrencyService thisProxy;

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public Currency readById(@CacheKeyParameter("id") long id) throws ClientException {
        CurrencyImpl c = new CurrencyImpl();
        c.setId(id);
		try {
			return thisProxy.find(c);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public Currency readByCode(@CacheKeyParameter("code") String code) throws ClientException {
        Currency c = new CurrencyImpl();
        c.setCode(code);
		try {
			return thisProxy.find(c);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public Currency readOrCreateCurrencyByCode(@CacheKeyParameter("code") String code) throws ClientException {
        Currency c = readByCode(code);
        if(c == null) {
            c = new CurrencyImpl();
            c.setCode(code);
            c.setDecimalPlaces(2L);
            c.setSymbol(code);
            c = thisProxy.create(c);
        }
        return c;
    }

    @Override
    @Cached(levels = {CacheLevel.APPLICATION})
    public Currency assertEuroCurrencyExists() throws ClientException {
        Currency euro = new CurrencyImpl();
        euro.setCode("EUR");
		try {
			return thisProxy.find(euro);
		} catch (UnknownResourceClientException e) {
			return readOrCreateCurrencyByCode(euro.getCode());
		}
    }

    @Override
    public void setThisProxy(CurrencyService serviceProxy) {
        thisProxy = serviceProxy;
    }
}
