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

import org.bndly.common.converter.api.ConversionException;
import org.bndly.common.converter.api.Converter;
import org.bndly.common.converter.api.ConverterRegistry;
import org.bndly.common.reflection.PathResolver;
import org.bndly.common.reflection.PathResolverImpl;
import org.bndly.ebx.searchengine.export.api.CSVExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportEntity;
import org.bndly.ebx.searchengine.export.api.ExportOutputTransformer;
import org.bndly.ebx.searchengine.export.api.ExportServiceException;
import org.bndly.shop.common.csv.CSVConfig;
import org.bndly.shop.common.csv.CSVException;
import org.bndly.shop.common.csv.model.Document;
import org.bndly.shop.common.csv.model.DocumentBuilder;
import org.bndly.shop.common.csv.model.Row;
import org.bndly.shop.common.csv.model.Value;
import org.bndly.shop.common.csv.serializing.CSVOutputStreamIterationListener;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class CSVFileExportOutputTransformer implements ExportOutputTransformer {

	private static final Logger LOG = LoggerFactory.getLogger(CSVFileExportOutputTransformer.class);
	private static final PathResolver PATH_RESOLVER = new PathResolverImpl();
	private final ConverterRegistry converterRegistry;
	private final Writer writer;
	private final CSVExportFormat csvFormat;
	private CSVOutputStreamIterationListener listener;
	private CSVConfig config;
	private DocumentBuilder documentBuilder;

	public CSVFileExportOutputTransformer(Writer writer, CSVExportFormat exportFormat, ConverterRegistry converterRegistry) {
		this.writer = writer;
		if (writer == null) {
			throw new IllegalArgumentException("writer is not allowed to be null");
		}
		this.csvFormat = exportFormat;
		this.converterRegistry = converterRegistry;
		if (converterRegistry == null) {
			throw new IllegalArgumentException("converterRegistry is not allowed to be null");
		}
		init();
	}

	private void init() {
		config = new CSVConfig() {

			@Override
			public String getNewLine() {
				return csvFormat.getEndOfLineSymbols();
			}

			@Override
			public String getQuote() {
				return csvFormat.getQuote();
			}

			@Override
			public String getSeparator() {
				return csvFormat.getDelimiter();
			}
		};
		listener = new CSVOutputStreamIterationListener(writer, config);
		documentBuilder = DocumentBuilder.newInstance(config);
	}

	@Override
	public void beforeEntities() throws ExportServiceException {
			if (csvFormat.isHeaderExported()) {
				documentBuilder.row();
				for (int i = 0; i < csvFormat.getColumnDefinitions().size(); i++) {
					final CSVExportFormat.Column col = csvFormat.getColumnDefinitions().get(i);
					documentBuilder.value(col.getHeaderName());
				}
			}
	}

	@Override
	public void afterEntities() throws ExportServiceException {
		try {
			Document doc = documentBuilder.build();
			listener.beforeDocument(doc);
			List<Row> rows = doc.getRows();
			if (rows != null) {
				for (Row row : rows) {
					listener.beforeRow(row);
					List<Value> values = row.getValues();
					if (values != null) {
						for (Value value : values) {
							listener.onColumn(value);
						}
					}
					listener.afterRow(row);
				}
			}
			listener.afterDocument(doc);
		} catch (CSVException e) {
			throw new ExportServiceException("failed to generate csv while transforming export data", e);
		}
	}

	@Override
	public void dealWithExportEntity(ExportEntity exportEntity) throws ExportServiceException {
		documentBuilder.row();
		Iterator<CSVExportFormat.Column> iter = csvFormat.getColumnDefinitions().iterator();
		while (iter.hasNext()) {
			CSVExportFormat.Column col = iter.next();
			String value = "";
			String path = col.getEntityPath();
			if (path != null && !path.isEmpty()) {
				Object val = PATH_RESOLVER.resolve(path, exportEntity);
				if (val != null) {
					Converter<Object, String> converter = converterRegistry.getConverter(val.getClass(), String.class);
					if (converter != null) {
						try {
							value = converter.convert(val);
						} catch (ConversionException ex) {
							LOG.error("could not convert {} to string", val, ex);
						}
					}
				}
			}
			if (value == null) {
				value = "";
			}
			documentBuilder.value(value);
		}
	}

}
