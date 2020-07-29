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

import org.bndly.ebx.searchengine.export.api.ExportEntity;
import org.bndly.ebx.searchengine.export.api.ExportEntityCollector;
import org.bndly.ebx.searchengine.export.api.ExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportMapper;
import org.bndly.ebx.searchengine.export.api.ExportOutputTransformer;
import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.ebx.searchengine.export.api.Exporter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = ExportProvider.class, immediate = true)
public class ExportProviderImpl implements ExportProvider {

	private final Map<Class<? extends ExportEntity>, Exporter> exportersByExportEntityType = new HashMap<>();
	private final Map<Class<? extends ExportEntity>, ExportMapper> exportMappersByExportEntityType = new HashMap<>();
	private final Map<Class<? extends ExportEntity>, ExportEntityCollector> exportEntityCollectorsByExportEntityType = new HashMap<>();
	
	
	private final List<ExportOutputTransformer.Factory> exportOutputTransformers = new ArrayList<>();
	
	@Override
	public void registerExporterByEntity(Class<? extends ExportEntity> registerForEntity, Exporter exporter) {
		if(registerForEntity != null && exporter != null) {
			exportersByExportEntityType.put(registerForEntity, exporter);
		}
	}

	@Override
	public void registerExportMapperByEntity(Class<? extends ExportEntity> registerForEntity, ExportMapper exportMapper) {
		if(registerForEntity != null && exportMapper != null) {
			exportMappersByExportEntityType.put(registerForEntity, exportMapper);
		}
	}

	@Override
	public void registerExportEntityCollectorByEntity(Class<? extends ExportEntity> registerForEntity, ExportEntityCollector exportEntityCollector) {
		if(registerForEntity != null && exportEntityCollector != null) {
			exportEntityCollectorsByExportEntityType.put(registerForEntity, exportEntityCollector);
		}
	}

	@Override
	public void unregisterExporterByEntity(Class<? extends ExportEntity> registerForEntity) {
		if(registerForEntity != null) {
			exportersByExportEntityType.remove(registerForEntity);
		}
	}

	@Override
	public void unregisterExportMapperByEntity(Class<? extends ExportEntity> registerForEntity) {
		if(registerForEntity != null) {
			exportMappersByExportEntityType.remove(registerForEntity);
		}
	}

	@Override
	public void unregisterExportEntityCollectorByEntity(Class<? extends ExportEntity> registerForEntity) {
		if(registerForEntity != null) {
			exportEntityCollectorsByExportEntityType.remove(registerForEntity);
		}
	}

	@Override
	public <E extends ExportEntity> ExportMapper<E> getExportMapperByEntity(Class<E> registeredEntity) {
		return exportMappersByExportEntityType.get(registeredEntity);
	}

	@Override
	public <E extends ExportEntity> ExportEntityCollector<E> getExportEntityCollectorByEntity(Class<E> registeredEntity) {
		return exportEntityCollectorsByExportEntityType.get(registeredEntity);
	}

	@Reference(
			bind = "registerExportOutputTransformer",
			unbind = "unregisterExportOutputTransformer",
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC,
			service = ExportOutputTransformer.Factory.class
	)
	@Override
	public void registerExportOutputTransformer(ExportOutputTransformer.Factory transformer) {
		if(transformer != null) {
			exportOutputTransformers.add(transformer);
		}
	}

	@Override
	public void unregisterExportOutputTransformer(ExportOutputTransformer.Factory transformer) {
		if(transformer != null) {
			Iterator<ExportOutputTransformer.Factory> it = exportOutputTransformers.iterator();
			while (it.hasNext()) {
				ExportOutputTransformer.Factory next = it.next();
				if(next == transformer) {
					it.remove();
				}
			}
		}
	}

	@Override
	public ExportOutputTransformer getExportOutputTransformer(ExportFormat exportFormat, Writer writer) {
		Iterator<ExportOutputTransformer.Factory> iterator = exportOutputTransformers.iterator();
		while (iterator.hasNext()) {
			ExportOutputTransformer.Factory next = iterator.next();
			if(next.supportsExportFormat(exportFormat)) {
				return next.create(exportFormat, writer);
			}
		}
		return null;
	}

	@Override
	public List<Exporter> getAvailableExporters() {
		List<Exporter> exporters = new ArrayList<>();
		for (Exporter value : exportersByExportEntityType.values()) {
			exporters.add(value);
		}
		return Collections.unmodifiableList(exporters);
	}

	@Override
	public Exporter getExporterByName(String exporterName) {
		for (Map.Entry<Class<? extends ExportEntity>, Exporter> entry : exportersByExportEntityType.entrySet()) {
			if(entry.getValue().getName().equals(exporterName)) {
				return entry.getValue();
			}
		}
		return null;
	}

}
