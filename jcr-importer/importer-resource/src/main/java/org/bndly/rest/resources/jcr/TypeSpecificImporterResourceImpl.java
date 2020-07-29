package org.bndly.rest.resources.jcr;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-resource
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

import org.bndly.ebx.jcr.importer.api.JCRImporter;
import org.bndly.rest.api.StatusWriter;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinkDescription;
import org.bndly.rest.controller.api.CacheControl;
import org.bndly.rest.controller.api.Documentation;
import org.bndly.rest.controller.api.DocumentationResponse;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.rest.descriptor.DefaultAtomLinkDescriptor;
import org.bndly.rest.descriptor.DelegatingAtomLinkDescription;
import org.bndly.rest.entity.resources.EntityResource;
import org.bndly.rest.entity.resources.UnknownResourceException;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class TypeSpecificImporterResourceImpl {
	
	private final EntityResource entityResource;
	private final ImporterResourceImpl importerResourceImpl;
	private final String entityTypeName;
	private boolean didActivate;

	public TypeSpecificImporterResourceImpl(EntityResource entityResource, ImporterResourceImpl importerResourceImpl) {
		this.entityResource = entityResource;
		this.importerResourceImpl = importerResourceImpl;
		entityTypeName = entityResource.getType().getName();
	}
	
	public class TypeSpecificImporterResourceImplLinkDescriptor extends DefaultAtomLinkDescriptor {

		@Override
		public AtomLinkDescription getAtomLinkDescription(Object controller, Method method, AtomLink atomLink) {
			AtomLinkDescription desc = super.getAtomLinkDescription(controller, method, atomLink);
			return new DelegatingAtomLinkDescription(desc) {
				@Override
				public Class<?> getLinkedInClass() {
					return entityResource.getRestBeanType();
				}
				
			};
		}

	}
	
	@GET
	@Path("fullSync/{id}")
	@CacheControl(preventCaching = true)
	@AtomLink(rel = "jcrimport", descriptor = TypeSpecificImporterResourceImplLinkDescriptor.class)
	@Documentation(
			authors = "bndly@cybercon.de",
			summary = "Synchronize a single entity to JCR",
			value = "Synchronizes a single entity to JCR, by invoking the synchronization strategies and executing the created jobs.",
			responses = {
				@DocumentationResponse(
						code = StatusWriter.Code.NO_CONTENT,
						description = "if the entity has been synchronized to JCR"
				),
				@DocumentationResponse(
						code = StatusWriter.Code.NOT_FOUND,
						description = "if the entity does not exist anymore and therefore can not be synchronized to the JCR"
				),
				@DocumentationResponse(
						code = StatusWriter.Code.INTERNAL_SERVER_ERROR,
						description = "if the JCR importer is currently disabled"
				)
			}
	)
	public Response fullSyncSingleRecord(@PathParam("id") Long id) {
		JCRImporter jcrImporter = importerResourceImpl.getJcrImporter();
		SchemaBeanFactory schemaBeanFactory = importerResourceImpl.getSchemaBeanFactory();
		
		if (jcrImporter.isEnabled()) {
			RecordContext rc = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
			final Record record = schemaBeanFactory.getEngine().getAccessor().readById(entityTypeName, id, rc);
			if (record == null) {
				throw new UnknownResourceException("could not find " + entityTypeName + " with id " + id);
			}
			jcrImporter.performSynchronization(new Iterable<Record>() {
				@Override
				public Iterator<Record> iterator() {
					return new Iterator<Record>() {
						private boolean didReturn;

						@Override
						public boolean hasNext() {
							return !didReturn;
						}

						@Override
						public Record next() {
							if (didReturn) {
								return null;
							}
							didReturn = true;
							return record;
						}

						@Override
						public void remove() {
							// no-op
						}
						
					};
				}
			});
			return Response.NO_CONTENT;
		} else {
			return Response.status(500).entity("Importer is disabled in configuration.\n");
		}
	}

	void activate() {
		if (!didActivate) {
			didActivate = true;
			importerResourceImpl.getControllerResourceRegistry().deploy(this, "jcr-importer/" + entityTypeName);
		}
	}

	void deactivate() {
		if (didActivate) {
			didActivate = false;
			importerResourceImpl.getControllerResourceRegistry().undeploy(this);
		}
	}
}
