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
package org.bndly.ebx.searchengine.export.impl.preisroboter;

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
import org.bndly.ebx.searchengine.export.api.ExportEntityCollector;
import org.bndly.ebx.searchengine.export.api.ExportMapper;
import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.ebx.searchengine.export.impl.shared.AbstractExportEntityCollector;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by IntelliJ IDEA. User: borismatosevic Date: 15.05.13 Time: 17:05 To
 * change this template use File | Settings | File Templates.
 */
@Component(service = ExportEntityCollector.class, immediate = true)
public class PreisRoboterCollector extends AbstractExportEntityCollector<PreisRoboterEntity> {

	@Reference
	private ExportProvider exportProvider;

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	public PreisRoboterCollector() {
		super(PreisRoboterEntity.class);
	}

	@Activate
	public void activate() {
		getExportProvider().registerExportEntityCollectorByEntity(modelType, this);
	}

	@Deactivate
	public void deactivate() {
		getExportProvider().unregisterExportEntityCollectorByEntity(modelType);
	}

	@Override
	protected ExportProvider getExportProvider() {
		return exportProvider;
	}

	@Override
	protected SchemaBeanFactory getSchemaBeanFactory() {
		return schemaBeanFactory;
	}

	@Override
	protected PreisRoboterEntity mapToEntity(Purchasable purchasable, Currency desiredCurrency, User defaultUser) {
		ExportMapper<PreisRoboterEntity> exportMapper = (ExportMapper<PreisRoboterEntity>) getExportProvider().getExportMapperByEntity(modelType);
		if (exportMapper == null) {
			throw new IllegalStateException("could not find mapper");
		}

		PriceRequest pr = determinePrice(purchasable, desiredCurrency, defaultUser);

		PreisRoboterEntity preisRoboterEntity = exportMapper.toModel(purchasable, pr.getPrice());
		preisRoboterEntity.setAvailability("sofort");
		preisRoboterEntity.setDeliveryTime("3-5 Tage");
		return preisRoboterEntity;
	}
}
