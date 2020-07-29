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

import org.bndly.ebx.model.AbstractProduct;
import org.bndly.ebx.model.Purchasable;
import org.bndly.ebx.model.UserPrice;
import org.bndly.ebx.searchengine.export.api.ExportEntity;
import org.bndly.ebx.searchengine.export.api.ExportMapper;
import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.schema.api.Record;
import org.bndly.schema.beans.SchemaBeanFactory;

/**
 * Created by IntelliJ IDEA.
 * User: borismatosevic
 * Date: 14.05.13
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractExportMapper <EXPORTMODEL extends ExportEntity> implements ExportMapper<EXPORTMODEL> {

    protected final Class<EXPORTMODEL> exportModelType;
    protected LinkGenerator linkGenerator;
	
    public AbstractExportMapper(Class<EXPORTMODEL> exportModelType) {
		if(exportModelType == null) {
			throw new IllegalArgumentException("exportModelType is not allowed to be null");
		}
		this.exportModelType = exportModelType;

        //Dummy implementation!
        linkGenerator = new LinkGenerator() {
            
			@Override
            public String generateDeepLink(Purchasable purchasable) {
				return "http://democybershop.de/sku/" + purchasable.getSku();
            }

			@Override
			public String generateImageLink(Purchasable purchasable) {
				return "http://democybershop.de/sku/" + purchasable.getSku()+".preview.png";
			}
			
			
        };
    }

	@Override
    public EXPORTMODEL toModel(Purchasable domainModel, UserPrice userPrice) {
        EXPORTMODEL model;
		try {
			model = exportModelType.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new IllegalStateException("export model type could not be instantiated via the default constructor", ex);
		}
		model.setSku(domainModel.getSku());
		model.setPrice(userPrice);
		
		if(AbstractProduct.class.isInstance(domainModel)) {
			AbstractProduct abstractProduct = (AbstractProduct)domainModel;
			model.setGtin(abstractProduct.getGtin());
			model.setArticleName(abstractProduct.getName());
		}
		
		if(getSchemaBeanFactory().isSchemaBean(domainModel)) {
			Record record = getSchemaBeanFactory().getRecordFromSchemaBean(domainModel);
			if(record != null) {
				model.setId(record.getId());
			}
		}
		
		String deepLink = linkGenerator.generateDeepLink(domainModel);
		model.setDeepLink(deepLink);
		return model;
    }

    public static interface LinkGenerator {
        String generateDeepLink(Purchasable purchasable);
        String generateImageLink(Purchasable purchasable);
    }

	public abstract ExportProvider getExportProvider();
	public abstract SchemaBeanFactory getSchemaBeanFactory();
	

}
