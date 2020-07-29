/*
 * Copyright (c) 2013, cyber:con GmbH, Bonn.
 *
 * All rights reserved. This source file is provided to you for
 * documentation purposes only. No part of this file may be
 * reproduced or copied in any form without the written
 * permission of cyber:con GmbH. No liability can be accepted
 * for errors in the program or in the documentation or for damages
 * which arise through using the program. If an error is discovered,
 * cyber:con GmbH will endeavour to correct it as quickly as possible.
 * The use of the program occurs exclusively under the conditions
 * of the licence contract with cyber:con GmbH.
 */

package org.bndly.ebx.searchengine.export.impl.shared;

/*-
 * #%L
 * org.bndly.ebx.search-engine-exporter
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

import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.Purchasable;
import org.bndly.ebx.model.User;
import org.bndly.ebx.searchengine.export.api.ExportEntity;
import org.bndly.ebx.searchengine.export.api.ExportEntityCollector;
import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.ebx.searchengine.export.api.ExportServiceException;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Date;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: borismatosevic
 * Date: 15.05.13
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractExportEntityCollector<MODEL extends ExportEntity> implements ExportEntityCollector<MODEL> {
    protected final Class<MODEL> modelType;
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractExportEntityCollector.class);
    
    public AbstractExportEntityCollector(Class<MODEL> modelType) {
		if(modelType == null) {
			throw new IllegalArgumentException("modelType should be passed via the default constructor");
		}
		this.modelType=modelType;
    }

	protected abstract ExportProvider getExportProvider();

	protected abstract SchemaBeanFactory getSchemaBeanFactory();

	protected final PriceRequest determinePrice(Purchasable purchasable, Currency desiredCurrency, User defaultUser) {
		// determine the price
		RecordContext recordContext = getSchemaBeanFactory().getRecordFromSchemaBean(purchasable).getContext();
		PriceRequest pr = getSchemaBeanFactory().getSchemaBean(PriceRequest.class, recordContext.create(PriceRequest.class.getSimpleName()));
		pr.setCreatedOn(new Date());
		pr.setCurrency(desiredCurrency);
		pr.setQuantity(1L);
		pr.setUser(defaultUser);
		pr.setSku(purchasable.getSku());
		((ActiveRecord)pr).persist();
		((ActiveRecord)pr).reload();
		return pr;
	}
	
	@Override
	public final void collect(ExportEntityHandler<MODEL> handler) throws ExportServiceException{
		Accessor accessor = getSchemaBeanFactory().getEngine().getAccessor();
		
		Iterator<Record> cRec = accessor.query("PICK Currency c IF c.code=? LIMIT ?", "EUR", 1);
		if(!cRec.hasNext()) {
			LOG.error("could not collect entities for export, because currency EUR was not available.");
			return;
		}
		
		Iterator<Record> uRec = accessor.query("PICK User u IF u.identifier=? LIMIT ?", "EXPORTUSER", 1);
		if(!uRec.hasNext()) {
			LOG.error("could not collect entities for export, because user EXPORTUSER was not available.");
			return;
		}
		
		Currency desiredCurrency = getSchemaBeanFactory().getSchemaBean(Currency.class, cRec.next());
		User defaultUser = getSchemaBeanFactory().getSchemaBean(User.class, uRec.next());
		
		Long total = accessor.count("COUNT "+Purchasable.class.getSimpleName()+" p IF p.sku!=?", (Object)null);
		final long batchSize = 20;
		long batches = total / batchSize + (total % batchSize != 0 ? 1: 0);
		for (long i = 0; i < batches; i++) {
			Iterator<Record> purchasables = accessor.query("PICK "+Purchasable.class.getSimpleName()+" p IF p.sku!=? LIMIT ? OFFSET ?", null, batchSize, i*batchSize);
			if(purchasables != null) {
				while (purchasables.hasNext()) {
					Purchasable purchasable = getSchemaBeanFactory().getSchemaBean(Purchasable.class, purchasables.next());
					String sku = purchasable.getSku();
					if(sku != null && !sku.isEmpty()) {
						handler.handleExportEntity(mapToEntity(purchasable, desiredCurrency, defaultUser));
					}
				}
			}
		}
	}
	
	protected abstract MODEL mapToEntity(Purchasable purchasable, Currency desiredCurrency, User defaultUser);
	
}
