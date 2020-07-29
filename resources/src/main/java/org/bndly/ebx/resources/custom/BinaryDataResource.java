package org.bndly.ebx.resources.custom;

/*-
 * #%L
 * org.bndly.ebx.resources
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

import org.bndly.common.data.api.DataStore;
import org.bndly.ebx.dataprovider.BinaryDataToDataConverter;
import org.bndly.ebx.dataprovider.BinarySimpleData;
import org.bndly.ebx.model.BinaryData;
import org.bndly.rest.api.Context;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.beans.ebx.BinaryDataRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.rest.data.resources.DataResource;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Path("ebx/BinaryData")
@Component(service = BinaryDataResource.class, immediate = true)
public class BinaryDataResource {

	@Reference
	private DataResource dataResource;
	@Reference(target = "(service.pid=org.bndly.common.data.api.DataStore.ebx)")
	private DataStore dataStore;
	@Reference
	private BinaryDataToDataConverter binaryDataToDataConverter;
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;

	@Activate
	public void activate() {
		controllerResourceRegistry.deploy(this);
	}

	@Deactivate
	public void deactivate() {
		controllerResourceRegistry.undeploy(this);
	}

	@POST
	@Path("{id}")
	@AtomLink(rel = "upload", target = BinaryDataRestBean.class)
	public Response upload(final @PathParam("id") long id, @Meta Context context) {
		RecordContext recordContext = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		BinaryData d = schemaBeanFactory.getSchemaBean(BinaryData.class, recordContext.create(BinaryData.class.getSimpleName(), id));
		BinarySimpleData data = binaryDataToDataConverter.convertBinaryDataToData(d);
		return dataResource.upload(data, dataStore, context);
	}

	@GET
	@Path("{id}/download")
	@AtomLink(rel = "download", target = BinaryDataRestBean.class)
	public Response download(final @PathParam("id") long id) {
		RecordContext recordContext = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		BinaryData d = schemaBeanFactory.getSchemaBean(BinaryData.class, recordContext.create(BinaryData.class.getSimpleName(), id));
		BinarySimpleData data = binaryDataToDataConverter.convertBinaryDataToData(d);
		return dataResource.download(data);
	}

	public void setDataResource(DataResource dataResource) {
		this.dataResource = dataResource;
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
