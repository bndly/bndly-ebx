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

import org.bndly.common.converter.api.ConverterRegistry;
import org.bndly.ebx.searchengine.export.api.CSVExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportOutputTransformer;
import org.bndly.ebx.searchengine.export.api.FormatType;
import java.io.Writer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = ExportOutputTransformer.Factory.class, immediate = true)
public class CSVExportOutputTransformerFactory implements ExportOutputTransformer.Factory {

	@Reference
	private ConverterRegistry converterRegistry;
	
	@Override
	public boolean supportsExportFormat(ExportFormat exportFormat) {
		return exportFormat.getType() == FormatType.CSV && CSVExportFormat.class.isInstance(exportFormat);
	}

	@Override
	public ExportOutputTransformer create(ExportFormat exportFormat, Writer writer) {
		return new CSVFileExportOutputTransformer(writer, (CSVExportFormat) exportFormat, converterRegistry);
	}

	public void setConverterRegistry(ConverterRegistry converterRegistry) {
		this.converterRegistry = converterRegistry;
	}
	
}
