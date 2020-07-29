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

import org.bndly.ebx.searchengine.export.api.CSVExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportEntity;
import org.bndly.ebx.searchengine.export.api.ExportEntityCollector;
import org.bndly.ebx.searchengine.export.api.ExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportOutputTransformer;
import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.ebx.searchengine.export.api.ExportServiceException;
import org.bndly.ebx.searchengine.export.api.Exporter;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: borismatosevic
 * Date: 13.05.13
 * Time: 11:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractExporter implements Exporter {
    protected final ExportFormat exportFormat;
	
	public AbstractExporter(ExportFormat exportFormat) {
		this.exportFormat = exportFormat;
		if(exportFormat == null) {
			throw new IllegalArgumentException("exportFormat is required");
		}
	}
	
	protected static CSVExportFormat.Column createColumnDefinition(final String headerName, final String entityPath, final String defaultValue) { 
		return new CSVExportFormat.Column() {

			@Override
			public String getEntityPath() {
				return entityPath;
			}

			@Override
			public String getHeaderName() {
				return headerName;
			}

			@Override
			public String getDefault() {
				return defaultValue;
			}
		};
	}

	@Override
	public void export(final Writer writer) throws ExportServiceException {
		ExportEntityCollector collector = getProductExportProvider().getExportEntityCollectorByEntity(getExportEntityClass());
		if(collector == null) {
			throw new ExportServiceException("could not find entity collector for "+getExportEntityClass().getName());
		}
		final ExportOutputTransformer outputTransformer = getProductExportProvider().getExportOutputTransformer(getExportFormat(), writer);
		if(outputTransformer == null) {
			throw new ExportServiceException("could not find output transformer");
		}
		outputTransformer.beforeEntities();
		collector.collect(new ExportEntityCollector.ExportEntityHandler<ExportEntity>() {

			@Override
			public void handleExportEntity(ExportEntity entity) throws ExportServiceException {
				// now do the export transformation to CSV or what ever
				outputTransformer.dealWithExportEntity(entity);
			}
		});
		outputTransformer.afterEntities();
	}

    @Override
    public final ExportFormat getExportFormat() {
        return exportFormat;
    }

	public abstract ExportProvider getProductExportProvider();

    public abstract void setProductExportProvider(ExportProvider productExportProvider);

	protected abstract Class<? extends ExportEntity> getExportEntityClass();
}
