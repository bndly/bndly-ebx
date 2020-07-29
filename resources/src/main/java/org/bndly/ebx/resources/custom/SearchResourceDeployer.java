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

import org.bndly.common.cors.api.CORSRequestDetector;
import org.bndly.common.mapper.MapperFactory;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.entity.resources.EntityResource;
import org.bndly.rest.entity.resources.EntityResourceDeploymentListener;
import org.bndly.rest.entity.resources.SchemaAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Attribute;
import org.bndly.schema.model.Mixin;
import org.bndly.schema.model.NamedAttributeHolder;
import org.bndly.schema.model.Type;
import org.bndly.search.api.SearchService;
import org.bndly.search.schema.api.SchemaRecordIndexer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = EntityResourceDeploymentListener.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SearchResourceDeployer implements EntityResourceDeploymentListener {

	private static final Logger LOG = LoggerFactory.getLogger(SearchResourceDeployer.class);
	@Reference
	private SchemaRecordIndexer schemaRecordIndexer;
	@Reference
	private SearchService searchService;
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference(target = "(service.pid=org.bndly.common.mapper.MapperFactory.ebx)")
	private MapperFactory mapperFactory;
	@Reference(
			cardinality = ReferenceCardinality.OPTIONAL
	)
	private CORSRequestDetector corsRequestDetector;
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;
	
	private final List<SearchQueryCustomizer> searchQueryCustomizers = new ArrayList<>();
	private final ReadWriteLock searchQueryCustomizersLock = new ReentrantReadWriteLock();
	
	private final List<DeployedSearchResource> deployedResources = new ArrayList<>();
	private final ReadWriteLock deployedResourcesLock = new ReentrantReadWriteLock();
	
	private DictionaryAdapter properties;

	public static interface SearchTermToQueryMapper {

		String searchTermToQuery(String searchTerm);
	}

	private static class DeployedSearchResource {

		private final SearchResource searchResource;
		private final SchemaAdapter schemaAdapter;
		private final EntityResource entityResource;

		public DeployedSearchResource(SearchResource searchResource, SchemaAdapter schemaAdapter, EntityResource entityResource) {
			this.searchResource = searchResource;
			this.schemaAdapter = schemaAdapter;
			this.entityResource = entityResource;
		}

		public EntityResource getEntityResource() {
			return entityResource;
		}

		public SchemaAdapter getSchemaAdapter() {
			return schemaAdapter;
		}

		public SearchResource getSearchResource() {
			return searchResource;
		}

	}

	@Activate
	public void activate(ComponentContext componentContext) {
		LOG.info("activated search resource deployer");
		this.properties = new DictionaryAdapter(componentContext.getProperties());
	}

	@Deactivate
	public void deactivate() {
		LOG.info("deactivating search resource deployer");
		deployedResourcesLock.writeLock().lock();
		try {
			for (DeployedSearchResource deployedSearchResource : deployedResources) {
				controllerResourceRegistry.undeploy(deployedSearchResource.getSearchResource());
			}
			deployedResources.clear();
		} finally {
			deployedResourcesLock.writeLock().unlock();
		}
		LOG.info("deactivated search resource deployer");
	}

	@Reference(
			bind = "registerSearchQueryCustomizer",
			unbind = "unregisterSearchQueryCustomizer",
			policy = ReferencePolicy.DYNAMIC,
			cardinality = ReferenceCardinality.MULTIPLE,
			service = SearchQueryCustomizer.class
	)
	public void registerSearchQueryCustomizer(SearchQueryCustomizer searchQueryCustomizer) {
		if (searchQueryCustomizer != null) {
			searchQueryCustomizersLock.writeLock().lock();
			try {
				searchQueryCustomizers.add(searchQueryCustomizer);
			} finally {
				searchQueryCustomizersLock.writeLock().unlock();
			}
		}
	}

	public void unregisterSearchQueryCustomizer(SearchQueryCustomizer searchQueryCustomizer) {
		if (searchQueryCustomizer != null) {
			searchQueryCustomizersLock.writeLock().lock();
			try {
				Iterator<SearchQueryCustomizer> iterator = searchQueryCustomizers.iterator();
				while (iterator.hasNext()) {
					SearchQueryCustomizer next = iterator.next();
					if (next == searchQueryCustomizer) {
						iterator.remove();
					}
				}
			} finally {
				searchQueryCustomizersLock.writeLock().unlock();
			}
		}
	}

	@Override
	public void deployed(SchemaAdapter schemaAdapter, EntityResource entityResource) {
		deployedResourcesLock.writeLock().lock();
		try {
			SearchResource searchResource = buildSearchResource(entityResource);
			if (searchResource != null) {
				// check if there is already a search resource deployed
				for (DeployedSearchResource deployedResource : deployedResources) {
					if (deployedResource.getEntityResource() == entityResource) {
						// skip the deployment
						return;
					}
				}

				LOG.info("deploying search resource");
				String baseUri = entityResource.getType().getSchema().getName() + "/" + entityResource.getType().getName();
				controllerResourceRegistry.deploy(searchResource, baseUri);
				deployedResources.add(new DeployedSearchResource(searchResource, schemaAdapter, entityResource));
			}
		} finally {
			deployedResourcesLock.writeLock().unlock();
		}
	}

	@Override
	public void undeployed(SchemaAdapter schemaAdapter, EntityResource entityResource) {
		deployedResourcesLock.writeLock().lock();
		try {
			Iterator<DeployedSearchResource> iterator = deployedResources.iterator();
			while (iterator.hasNext()) {
				DeployedSearchResource next = iterator.next();
				if (next.getEntityResource() == entityResource) {
					iterator.remove();
					controllerResourceRegistry.undeploy(next.getSearchResource());
				}
			}
		} finally {
			deployedResourcesLock.writeLock().unlock();
		}
	}

	public SearchResource buildSearchResource(final EntityResource entityResource) {
		Type type = entityResource.getType();
		if (!type.isVirtual()) {
			final String suffix = entityResource.getType().getSchema().getName() + "." + entityResource.getType().getName();
			final String queryTemplate = properties.getString("searchterm.format." + suffix);
			final String toReplace = "\\{searchTerm\\}";
			SearchTermToQueryMapper searchTermToQueryMapper = new SearchTermToQueryMapper() {

				@Override
				public String searchTermToQuery(String searchTerm) {
					if (queryTemplate == null) {
						return null;
					}
					return queryTemplate.replaceAll(toReplace, escapeStringForSolrQuery(searchTerm));
				}
			};
			SearchQueryCustomizer searchQueryCustomizer = new SearchQueryCustomizer() {

				@Override
				public String customizeQuery(String query, Type targetType) {
					searchQueryCustomizersLock.readLock().lock();
					try {
						Iterator<SearchQueryCustomizer> iterator = searchQueryCustomizers.iterator();
						while (iterator.hasNext()) {
							SearchQueryCustomizer next = iterator.next();
							query = next.customizeQuery(query, targetType);
						}
						return query;
					} finally {
						searchQueryCustomizersLock.readLock().unlock();
					}
				}
			};
			SearchResource r = new SearchResource(entityResource, searchTermToQueryMapper, searchQueryCustomizer);
			r.setCorsRequestDetector(corsRequestDetector);
			r.setJsonSchemaBeanFactory(schemaBeanFactory.getJsonSchemaBeanFactory());
			r.setMapperFactory(mapperFactory);
			r.setSchemaBeanFactory(schemaBeanFactory);
			r.setSchemaRecordIndexer(schemaRecordIndexer);
			r.setSearchService(searchService);
			r.setType(type);

			// add a dependency to the entity resource deployer. 
			// the deployer has a schema adapter, that will provide 
			// classloaders and other stuff.
			Class<?> restBean = entityResource.getRestBeanType();
			Class<?> listRestBean = entityResource.getListRestBean();
			List<SearchField> searchFields = new ArrayList<>();
			collectSearchFields(type, searchFields);
			r.setSearchFields(searchFields);
			r.setListRestBean(listRestBean);
			r.setRestBean(restBean);
			return r;
		}
		return null;
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setMapperFactory(MapperFactory mapperFactory) {
		this.mapperFactory = mapperFactory;
	}

	public void setCorsRequestDetector(CORSRequestDetector corsRequestDetector) {
		this.corsRequestDetector = corsRequestDetector;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setSchemaRecordIndexer(SchemaRecordIndexer schemaRecordIndexer) {
		this.schemaRecordIndexer = schemaRecordIndexer;
	}

	public void setSchemaDeploymentListeners(List schemaDeploymentListeners) {
		schemaDeploymentListeners.add(this);
	}

	protected static String escapeStringForSolrQuery(String q) {
		// escape solr special characters
		StringBuffer qsb = new StringBuffer();
		for (int i = 0; i < q.length(); i++) {
			char c = q.charAt(i);
			if (Character.isWhitespace(c)) {
				qsb.append("\\");
				qsb.append(c);
			} else if (c == '\\') {
				qsb.append("\\\\");
			} else if (c == '+') {
				qsb.append("\\+");
			} else if (c == '-') {
				qsb.append("\\-");
			} else if (c == '&') {
				qsb.append("\\&");
			} else if (c == '|') {
				qsb.append("\\|");
			} else if (c == '!') {
				qsb.append("\\!");
			} else if (c == '(') {
				qsb.append("\\(");
			} else if (c == ')') {
				qsb.append("\\)");
			} else if (c == '{') {
				qsb.append("\\{");
			} else if (c == '}') {
				qsb.append("\\}");
			} else if (c == '[') {
				qsb.append("\\[");
			} else if (c == ']') {
				qsb.append("\\]");
			} else if (c == '^') {
				qsb.append("\\^");
			} else if (c == '~') {
				qsb.append("\\~");
			} else if (c == '*') {
				qsb.append("\\*");
			} else if (c == '?') {
				qsb.append("\\?");
			} else if (c == ':') {
				qsb.append("\\:");
			} else if (c == '"') {
				qsb.append("\\\"");
			} else if (c == ';') {
				qsb.append("\\;");
			} else if (c == '/') {
				qsb.append("\\/");
			} else {
				qsb.append(c);
			}
		}
		q = qsb.toString();
		return q;
	}

	private void collectSearchFields(NamedAttributeHolder attributeHolder, List<SearchField> searchFields) {
		if (attributeHolder == null) {
			return;
		}
		List<Attribute> atts = attributeHolder.getAttributes();
		if (atts != null) {
			for (Attribute attribute : atts) {
				SearchField sf = new SearchField(attributeHolder.getName() + "_" + attribute.getName(), attribute);
				searchFields.add(sf);
			}
		}
		if (Type.class.isInstance(attributeHolder)) {
			Type t = Type.class.cast(attributeHolder);
			collectSearchFields(t.getSuperType(), searchFields);
			List<Mixin> mixins = t.getMixins();
			if (mixins != null) {
				for (Mixin mixin : mixins) {
					collectSearchFields(mixin, searchFields);
				}
			}
		}
	}

}
