package org.bndly.ebx.resource.search.export;

/*-
 * #%L
 * org.bndly.ebx.search-engine-exporter-resource
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

import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.ebx.searchengine.export.api.ExportServiceException;
import org.bndly.ebx.searchengine.export.api.Exporter;
import org.bndly.rest.api.ContentType;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.api.ResourceURIBuilder;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinks;
import org.bndly.rest.beans.search.export.SearchExportOverviewRestBean;
import org.bndly.rest.common.beans.Services;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Path("searchengine")
@Component(service = SearchEngineExportResource.class, immediate = true)
public class SearchEngineExportResource {

	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;
	@Reference
	private ExportProvider exportProvider;
	private static final ContentType CSV = new ContentType() {
		@Override
		public String getName() {
			return "text/csv";
		}

		@Override
		public String getExtension() {
			return "csv";
		}
	};

	@Activate
	public void activate() {
		controllerResourceRegistry.deploy(this);
	}

	@Deactivate
	public void deactivate() {
		controllerResourceRegistry.undeploy(this);
	}

	@GET
	@AtomLinks({
		@AtomLink(rel = "searchengine", target = Services.class)
	})
	public Response listAvailable(@Meta Context context) {
		SearchExportOverviewRestBean result = new SearchExportOverviewRestBean();
		List<Exporter> exporters = exportProvider.getAvailableExporters();
		if (exporters != null) {
			for (Exporter exporter : exporters) {
				String name = exporter.getName();
				ResourceURIBuilder builder = context.createURIBuilder();
				ResourceURI url = builder.pathElement("searchengine").pathElement("export").pathElement(name).extension("csv").build();
				result.setLink("export_" + name, url.asString(), "GET");
			}
		}
		return Response.ok(result);
	}

	@GET
	@Path("export/{exporterName}.csv")
	public Response export(@PathParam("exporterName") String exporterName, @Meta Context context) throws IOException {
		Exporter exporter = exportProvider.getExporterByName(exporterName);
		if (exporter == null) {
			return Response.status(404);
		}
		try {
			OutputStream os = context.getOutputStream();
			context.setOutputContentType(CSV, "UTF-8");
			try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os, "UTF-8")) {
				exporter.export(outputStreamWriter);
				outputStreamWriter.flush();
			}
			os.flush();
		} catch (ExportServiceException ex) {
			return Response.status(500);
		}
		return Response.ok();
	}
}
