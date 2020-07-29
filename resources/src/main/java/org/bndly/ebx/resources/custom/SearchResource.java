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
import org.bndly.common.json.model.JSArray;
import org.bndly.common.json.model.JSObject;
import org.bndly.common.json.model.JSValue;
import org.bndly.common.json.serializing.JSONSerializer;
import org.bndly.common.mapper.MapperFactory;
import org.bndly.common.reflection.InstantiationUtil;
import org.bndly.common.reflection.SetterBeanPropertyWriter;
import org.bndly.ebx.resources.custom.SearchResourceDeployer.SearchTermToQueryMapper;
import static org.bndly.ebx.resources.custom.SearchResourceDeployer.escapeStringForSolrQuery;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.api.ResourceURIBuilder;
import org.bndly.rest.api.StatusWriter;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.common.beans.ListRestBean;
import org.bndly.rest.common.beans.PaginationRestBean;
import org.bndly.rest.common.beans.SortRestBean;
import org.bndly.rest.controller.api.CacheControl;
import org.bndly.rest.controller.api.Documentation;
import org.bndly.rest.controller.api.DocumentationResponse;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.QueryParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.rest.entity.resources.EntityResource;
import org.bndly.rest.search.beans.SearchParameters;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.json.beans.JSONSchemaBeanFactory;
import org.bndly.schema.json.beans.JSONUtil;
import org.bndly.schema.model.Attribute;
import org.bndly.schema.model.DecimalAttribute;
import org.bndly.schema.model.Mixin;
import org.bndly.schema.model.NamedAttributeHolder;
import org.bndly.schema.model.NamedAttributeHolderAttribute;
import org.bndly.schema.model.StringAttribute;
import org.bndly.schema.model.Type;
import org.bndly.search.api.DocumentMapper;
import org.bndly.search.api.Query;
import org.bndly.search.api.Result;
import org.bndly.search.api.SearchService;
import org.bndly.search.schema.api.SchemaRecordIndexer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Path("")
public class SearchResource {

	private final EntityResource entityResource;
	private final SearchTermToQueryMapper searchTermToQueryMapper;
	private final SearchQueryCustomizer searchQueryCustomizer;
    private Type type;
    private SchemaRecordIndexer schemaRecordIndexer;
    private SearchService searchService;
    private SchemaBeanFactory schemaBeanFactory;
    private JSONSchemaBeanFactory jsonSchemaBeanFactory;
    private MapperFactory mapperFactory;
    private CORSRequestDetector corsRequestDetector;
    private Class<?> listRestBean;
    private Class<?> restBean;
    private List<SearchField> searchFields;

	public SearchResource(EntityResource entityResource, SearchTermToQueryMapper searchTermToQueryMapper, SearchQueryCustomizer searchQueryCustomizer) {
		if (entityResource == null) {
			throw new IllegalArgumentException("entityResource is not allowed to be null");
		}
		this.entityResource = entityResource;
		if (searchTermToQueryMapper == null) {
			throw new IllegalArgumentException("searchTermToQueryMapper is not allowed to be null");
		}
		this.searchTermToQueryMapper = searchTermToQueryMapper;
		if (searchQueryCustomizer == null) {
			throw new IllegalArgumentException("searchQueryCustomizer is not allowed to be null");
		}
		this.searchQueryCustomizer = searchQueryCustomizer;
	}

	public final EntityResource getEntityResource() {
		return entityResource;
	}

    @GET
    @Path("reindex")
    @AtomLink(rel = "reindex", descriptor = SearchListRestBeanLinkDescriptor.class)
	@CacheControl(preventCaching = true)
	@Documentation(
			authors = "bndly@cybercon.de",
			value = "drops all entries of the entity type from the solr index. then all existing instances of the type will be added to the index again.",
			responses = @DocumentationResponse(
					code = StatusWriter.Code.NO_CONTENT,
					description = "if everything is ok and no exception occurs."
			),
			tags = "solr"
	)
    public Response reindex() {
        schemaRecordIndexer.reindex(type);
        return Response.NO_CONTENT;
    }

    @POST
    @Path("search")
    @AtomLink(rel = "search", descriptor = SearchListRestBeanLinkDescriptor.class)
	@Documentation(
			authors = "bndly@cybercon.de",
			value = "redirects to a resource, that executes a solr search with the data of the provided search parameters.",
			responses = @DocumentationResponse(
					code = StatusWriter.Code.FOUND,
					description = "redirects to a resource, where the search result can be retrieved."
			),
			tags = "solr"
	)
    public Response buildSearchUri(SearchParameters searchParameters, @Meta Context context) {
        String typeName = type.getName();
		ResourceURIBuilder builder = context.createURIBuilder();
        builder.pathElement(type.getSchema().getName());
        builder.pathElement(typeName);
        builder.pathElement("search");
        if(searchParameters != null) {
            if (!isEmpty(searchParameters.getQuery())) {
				String q = searchQueryCustomizer.customizeQuery(searchParameters.getQuery(), type);
                builder.parameter("q", q);
            } else {
				String q = isEmpty(searchParameters.getSearchTerm()) ? null : searchTermToQueryMapper.searchTermToQuery(searchParameters.getSearchTerm());
				q = searchQueryCustomizer.customizeQuery(q, type);
				if(q != null) {
					builder.parameter("q", q);
				}
            }
            PaginationRestBean p = searchParameters.getPage();
            if (p != null) {
                if (p.getSize() != null) {
                    builder.parameter(EntityResource.PAGINATION_SIZE, p.getSize().toString());
                }
                if (p.getStart() != null) {
                    builder.parameter(EntityResource.PAGINATION_START, p.getStart().toString());
                }
            }
            SortRestBean s = searchParameters.getSorting();
            if(s != null) {
                if(s.getField() != null) {
                    builder.parameter(EntityResource.SORTING_FIELD, s.getField());
                    if(s.isAscending() != null) {
                        if(s.isAscending()) {
                            builder.parameter(EntityResource.SORTING_DIRECTION, "ASC");
                        } else {
                            builder.parameter(EntityResource.SORTING_DIRECTION, "DESC");
                        }
                    }
                }
            }
        }
		ResourceURI uri = builder.build();
        if (corsRequestDetector != null && corsRequestDetector.isCORSRequest()) {
            return Response.created(uri.asString());
        }
        return Response.status(302).location(uri.asString());
    }

	private boolean isEmpty(String input) {
		return input == null || input.isEmpty();
	}
	
    @GET
    @Path("search")
	@Documentation(
			authors = "bndly@cybercon.de",
			value = "executes a solr search. the result will be a a paginated listrestbean.",
			produces = Documentation.ANY_CONTENT_TYPE,
			responses = @DocumentationResponse(description = "paginated list of found entries in the solr index."),
			tags = "solr"
	)
    public Response search(
			@QueryParam("q") String queryString, 
			@QueryParam(EntityResource.PAGINATION_SIZE) Long pageSize, 
			@QueryParam(EntityResource.PAGINATION_START) Long pageStart, 
			@QueryParam(EntityResource.SORTING_FIELD) String sf, 
			@QueryParam(EntityResource.SORTING_DIRECTION) String sd, 
			@Meta Context context
	) {
        final String typeName = type.getName();
        final Long _pageSize = pageSize == null ? 10L : pageSize;
        final Long _pageStart = pageStart == null ? 0L : pageStart;
        String tmpSortingField = null;
        for (SearchField searchField : searchFields) {
            if(searchField.getAttribute().getName().equals(sf)) {
                tmpSortingField = searchField.getName();
                break;
            }
        }
        final String sortingField = tmpSortingField;
                
        final String sortingDirection = sd;
        final String q;
        if (queryString == null) {
            q = "_type:" + typeName;
        } else {
            q = "_type:" + typeName + " AND " + queryString;
        }
        Query query = new Query() {

            @Override
            public Integer getStart() {
                return _pageStart.intValue();
            }

            @Override
            public Integer getRows() {
                return _pageSize.intValue();
            }

            @Override
            public String getRequestHandler() {
                return "select";
            }

            @Override
            public String getQ() {
                return q;
            }

            @Override
            public String[] getFields() {
                return new String[]{"*"};
            }

            @Override
            public String getSortField() {
                return sortingField;
            }

            @Override
            public boolean isAscending() {
                return "ASC".equals(sortingDirection);
            }
            
        };

		RecordContext recordContext = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
        DocumentMapper<Object> documentMapper = buildDocumentMapper(typeName, recordContext);
        Result<Object> result = searchService.search(query, documentMapper);

        ListRestBean entity = (ListRestBean) InstantiationUtil.instantiateType(listRestBean);
        PaginationRestBean p = new PaginationRestBean();
        p.setSize(pageSize);
        p.setStart(pageStart);
        p.setTotalRecords(result.getNumberOfHits());
        entity.setPage(p);
        for (Object object : result) {
            Object bean = InstantiationUtil.instantiateType(restBean);
            mapperFactory.buildContext().map(object, bean, restBean);
            entity.add(bean);
        }
        return Response.ok(entity);
    }

    @GET
    @Path("typeahead")
    @AtomLink(rel = "typeahead", descriptor = SearchListRestBeanLinkDescriptor.class)
	@Documentation(
			authors = "bndly@cybercon.de",
			value = "is only used to generate an atom link to the query controller method.",
			responses = @DocumentationResponse(description = "empty response"),
			tags = {"ebxadmin", "solr"}
	)
    public Response typeahead() {
        return Response.ok();
    }

    @GET
    @Path("typeahead/{query}.json")
	@Documentation(
			authors = "bndly@cybercon.de",
			value = "generates a search query using the ebx solr. the query will be applied to all possible fields. conversions to numbers happen automatically. the query will be pre- and postpended with wildcards internally.",
			responses = @DocumentationResponse(description = "solr search result as json array."),
			produces = "application/json",
			tags = {"ebxadmin", "solr"}
	)
	@CacheControl(maxAge = 30)
    public Response typeahead(@PathParam("query") String q, @Meta Context context) {
		StringBuffer qBuilder = new StringBuffer(q);
		ResourceURI uri = context.getURI();
		if (uri.getSelectors() != null) {
			for (ResourceURI.Selector selector : uri.getSelectors()) {
				qBuilder.append('.').append(selector.getName());
			}
		}
		q = qBuilder.toString();
        String typeName = type.getName();
        StringBuffer sb = null;
        for (SearchField searchField : searchFields) {
            String value = null;
            Attribute att = searchField.getAttribute();
            if (DecimalAttribute.class.isInstance(att)) {
                DecimalAttribute da = DecimalAttribute.class.cast(att);
                Integer length = da.getLength();
                Integer dp = da.getDecimalPlaces();
                if (dp == null) {
                    dp = 0;
                }
                if (length == null) {
                    if (dp == 0) {
                        try {
                            value = new Long(q).toString();
                        } catch(NumberFormatException e) {
                        }
                    } else {
                        try {
                            value = new Double(q).toString();
                        } catch(NumberFormatException e) {
                        }
                    }
                } else {
                    try {
                        value = new BigDecimal(q).stripTrailingZeros().toPlainString();
                    } catch(NumberFormatException e) {
                    }
                }
            } else if (StringAttribute.class.isInstance(att)) {
                if ("".equals(q)) {
                    value = "*";
                } else {
                    value = "*" + escapeStringForSolrQuery(q)+"*";
                }
            }
            if (value != null) {
                if (sb == null) {
                    sb = new StringBuffer();
                } else {
                    sb.append(" OR ");
                }
                sb.append(searchField.getName());
                sb.append(':');
                sb.append(value);
            }
        }

        final String _q = "_type:" + typeName + " AND (" + sb.toString() + ")";
        Query query = new Query() {

            @Override
            public Integer getStart() {
                return 0;
            }

            @Override
            public Integer getRows() {
                return 10;
            }

            @Override
            public String getRequestHandler() {
                return "select";
            }

            @Override
            public String getQ() {
                return _q;
            }

            @Override
            public String[] getFields() {
                return new String[]{"*"};
            }

            @Override
            public String getSortField() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return false;
            }
            
        };
		RecordContext recordContext = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
        Result<Object> result = searchService.search(query, buildDocumentMapper(typeName, recordContext));
        final JSArray array = new JSArray();
        array.setItems(new ArrayList<JSValue>());
        for (Object bean : result) {
            Object so = jsonSchemaBeanFactory.convertToStreamingObject(bean);
            JSObject jsobject = jsonSchemaBeanFactory.getJSObjectFromSchemaBean(so);
            if (ActiveRecord.class.isInstance(bean)) {
                Long id = ((ActiveRecord) bean).getId();
                if (id != null) {
                    JSONUtil.createNumberMember(jsobject, "id", id);
                }
            }
            array.getItems().add(jsobject);
        }
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			new JSONSerializer().serialize(array, bos);
			bos.flush();
			ByteArrayInputStream is = new ByteArrayInputStream(bos.toByteArray());
			return Response.ok(is);
		} catch (IOException ex) {
			throw new IllegalStateException("could not serialize typeahead result: "+ex.getMessage(), ex);
		}
    }

    public void setSchemaRecordIndexer(SchemaRecordIndexer schemaRecordIndexer) {
        this.schemaRecordIndexer = schemaRecordIndexer;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
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

    private DocumentMapper<Object> buildDocumentMapper(final String typeName, final RecordContext recordContext) {
        final String typeAttributePrefix = typeName + "_";
        return new DocumentMapper<Object>() {

            final SetterBeanPropertyWriter writer = new SetterBeanPropertyWriter();

            @Override
            public Object getInstance() {
                return schemaBeanFactory.getSchemaBean(recordContext.create(typeName));
            }

            @Override
            public void setValue(Object instance, String fieldName, Object value) {
                Record r = schemaBeanFactory.getRecordFromSchemaBean(instance);
                int i = fieldName.indexOf("_");
                if (i > 0) {
                    String declaredIn = fieldName.substring(0, i);
                    if (isNamedAttributeHolderIsInTypeHierarchy(r.getType(), declaredIn)) {
                        String attributeName = fieldName.substring(i + 1);
                        if (attributeName.endsWith("_id")) {
                            attributeName = attributeName.substring(0, attributeName.length() - "_id".length());
                        }
                        try {
                            Attribute attribute = r.getAttributeDefinition(attributeName);
                            if (NamedAttributeHolderAttribute.class.isInstance(attribute)) {
                                NamedAttributeHolderAttribute naha = NamedAttributeHolderAttribute.class.cast(attribute);
                                NamedAttributeHolder holder = naha.getNamedAttributeHolder();
                                if (value != null && Long.class.isInstance(value)) {
                                    try {
										RecordContext context = r.getContext();
                                        value = schemaBeanFactory.getSchemaBean(context.create(holder.getName(), (Long) value));
                                        writer.set(attributeName, value, instance);
                                    } catch (Exception e) {
                                    }
                                }
                            } else {
                                writer.set(attributeName, value, instance);
                            }
                        } catch (Exception e) {
                            // we don't care about unknown attributes
                        }
                    }
                } else if ("id".equals(fieldName)) {
                    long id = new Long(((String) value).substring(typeAttributePrefix.length()));
                    r.setId(id);
                }
                // TODO
            }

            private boolean isNamedAttributeHolderIsInTypeHierarchy(Type type, String declaredIn) {
                if (type == null) {
                    return false;
                } else {
                    if (type.getName().equals(declaredIn)) {
                        return true;
                    }
                    List<Mixin> mixins = type.getMixins();
                    if (mixins != null) {
                        for (Mixin mixin : mixins) {
                            if (mixin.getName().equals(declaredIn)) {
                                return true;
                            }
                        }
                    }
                    return isNamedAttributeHolderIsInTypeHierarchy(type.getSuperType(), declaredIn);
                }
            }
        };
    }

    public void setJsonSchemaBeanFactory(JSONSchemaBeanFactory jsonSchemaBeanFactory) {
        this.jsonSchemaBeanFactory = jsonSchemaBeanFactory;
    }

    public void setType(Type type) {
        this.type = type;
    }

    void setListRestBean(Class<?> listRestBean) {
        this.listRestBean = listRestBean;
    }

    void setRestBean(Class<?> restBean) {
        this.restBean = restBean;
    }

    public void setSearchFields(List<SearchField> searchFields) {
        this.searchFields = searchFields;
    }

}
