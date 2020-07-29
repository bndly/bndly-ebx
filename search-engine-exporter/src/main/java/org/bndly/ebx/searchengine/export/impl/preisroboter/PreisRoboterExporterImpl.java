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

import org.bndly.ebx.searchengine.export.api.CSVExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportEntity;
import org.bndly.ebx.searchengine.export.api.ExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.ebx.searchengine.export.api.Exporter;
import org.bndly.ebx.searchengine.export.api.FormatType;
import org.bndly.ebx.searchengine.export.impl.shared.AbstractExporter;
import java.util.Arrays;

import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by IntelliJ IDEA.
 * User: borismatosevic
 * Date: 13.05.13
 * Time: 12:02
 * To change this template use File | Settings | File Templates.
 */
@Component(service = Exporter.class, immediate = true)
public class PreisRoboterExporterImpl extends AbstractExporter {

	public static final ExportFormat DEFAULT_FORMAT = new CSVExportFormat() {
		private final List<CSVExportFormat.Column> columnDefinitions = Arrays.asList(
				createColumnDefinition("Artikel-Nr.", "sku", null),
				createColumnDefinition("Artikelname", "articleName", null),
				createColumnDefinition("Preise", "price.discountedGrossValue", null),
				createColumnDefinition("DeepLink", "deepLink", null),
				createColumnDefinition("Bild-url", "pictureLink", null),
				createColumnDefinition("Kurzbeschr.", "articleDescription", null),
				createColumnDefinition("Versandk.", "shipmentPrice", null),
				createColumnDefinition("Verfügbarkeit.", "availability", null),
				createColumnDefinition("EAN.", "gtin", null),
				createColumnDefinition("PZN.", "pzn", null),
				createColumnDefinition("Hersteller.", "manufacturerName", null),
				createColumnDefinition("Hersteller-ArtNr.", "manufacturerArticleNumber", null),
				createColumnDefinition("Kategorie.", "categories", null)
		);

		@Override
		public String getQuote() {
			return "\"";
		}

		@Override
		public String getDelimiter() {
			return ",";
		}

		@Override
		public String getEndOfLineSymbols() {
			return "\n";
		}

		@Override
		public FormatType getType() {
			return FormatType.CSV;
		}

		@Override
		public boolean isHeaderExported() {
			return false;
		}

		@Override
		public List<CSVExportFormat.Column> getColumnDefinitions() {
			return columnDefinitions;
		}
	};
	
	@Reference
	private ExportProvider productExportProvider;

	public PreisRoboterExporterImpl() {
		super(DEFAULT_FORMAT);
	}
	
	@Override
	public final String getName() {
		return "preisroboter";
	}
	
	@Activate
	public void activate() {
		getProductExportProvider().registerExporterByEntity(getExportEntityClass(), this);
	}
	
	@Deactivate
	public void deactivate() {
		getProductExportProvider().unregisterExporterByEntity(getExportEntityClass());
	}
	
	@Override
	protected Class<? extends ExportEntity> getExportEntityClass() {
		return PreisRoboterEntity.class;
	}

	@Override
	public ExportProvider getProductExportProvider() {
		return productExportProvider;
	}

	@Override
	public void setProductExportProvider(ExportProvider productExportProvider) {
		this.productExportProvider = productExportProvider;
	}
	
	
	
}
