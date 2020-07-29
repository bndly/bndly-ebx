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

import org.bndly.common.mapper.MapperFactory;
import org.bndly.ebx.resources.strategy.DelegatingTypeDeletionStrategy;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinks;
import org.bndly.rest.atomlink.api.annotation.Parameter;
import org.bndly.rest.beans.ebx.ExternalObjectListAssocationListRestBean;
import org.bndly.rest.common.beans.ListRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.PUT;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Path("batch")
@Component(service = BatchResource.class, immediate = true)
public class BatchResource {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference(target = "(service.pid=org.bndly.common.mapper.MapperFactory.ebx)")
	private MapperFactory mapperFactory;
	@Reference
	private DelegatingTypeDeletionStrategy deletionStrategy;
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

	private static interface BatchHandler {

		void handleActiveRecord(ActiveRecord ar, Transaction transaction);
	}

	private Response processPayload(final @PathParam("typeName") String typeName, ListRestBean payload, BatchHandler handler) {
		Class<?> schemaBeanType = schemaBeanFactory.getTypeBindingForType(typeName);
		Transaction tx = schemaBeanFactory.getEngine().getQueryRunner().createTransaction();
		for (Object object : payload) {
			Object schemaBean = mapperFactory.buildContext().map(object, schemaBeanType);
			handler.handleActiveRecord(((ActiveRecord) schemaBean), tx);
		}
		tx.commit();
		return Response.NO_CONTENT;
	}

	@POST
	@Path("create/{typeName}")
	@AtomLinks({
		@AtomLink(rel = "batchCreate", target = ExternalObjectListAssocationListRestBean.class, parameters = {
			@Parameter(name = "typeName", expression = "ExternalObjectListAssocation")
		})
	})
	public Response batchCreate(final @PathParam("typeName") String typeName, ListRestBean payload) {
		return processPayload(typeName, payload, new BatchHandler() {
			@Override
			public void handleActiveRecord(ActiveRecord ar, Transaction transaction) {
				ar.persist(transaction);
			}
		});
	}

	@PUT
	@Path("update/{typeName}")
	@AtomLinks({
		@AtomLink(rel = "batchUpdate", target = ExternalObjectListAssocationListRestBean.class, parameters = {
			@Parameter(name = "typeName", expression = "ExternalObjectListAssocation")
		})
	})
	public Response batchUpdate(final @PathParam("typeName") String typeName, ListRestBean payload) {
		return processPayload(typeName, payload, new BatchHandler() {

			@Override
			public void handleActiveRecord(ActiveRecord ar, Transaction transaction) {
				ar.update(transaction);
			}
		});
	}

	@POST
	@Path("delete/{typeName}")
	@AtomLinks({
		@AtomLink(rel = "batchDelete", target = ExternalObjectListAssocationListRestBean.class, parameters = {
			@Parameter(name = "typeName", expression = "ExternalObjectListAssocation")
		})
	})
	public Response batchDelete(final @PathParam("typeName") String typeName, ListRestBean payload) {
		return processPayload(typeName, payload, new BatchHandler() {
			@Override
			public void handleActiveRecord(ActiveRecord ar, Transaction transaction) {
				Record record = schemaBeanFactory.getRecordFromSchemaBean(ar);
				deletionStrategy.delete(record, transaction);
			}
		});
	}

	public void setMapperFactory(MapperFactory mapperFactory) {
		this.mapperFactory = mapperFactory;
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setDeletionStrategy(DelegatingTypeDeletionStrategy deletionStrategy) {
		this.deletionStrategy = deletionStrategy;
	}

}
