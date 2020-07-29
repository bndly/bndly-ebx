/*-
 * #%L
 * org.bndly.ebx.app-common
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
define(["cy/EntityTableDataProvider", "cy/RestBeans", "cy/SearchConfiguration"], function(EntityTableDataProvider, RestBeans, SearchConfiguration) {
	var SearchTableDataProvider = EntityTableDataProvider.extend({
		construct: function(config) {
			if(!config) {
				config = {};
			}
			if(!config.searchTerm) {
				config.searchTerm = "";
			}
			config.searchConfig = SearchConfiguration(config.entityType);
                        config.sortField = config.searchConfig.sortBy;
			this.callSuper(arguments, config);
		},
		setSearchTerm: function(searchTerm) {
			this.searchTerm = searchTerm;
		},
		loadedSearchResult: function(searchResult) {
			this.page = searchResult;
			this.fireEvent("load", searchResult.getItems());			
		},
		foundSearchResult: function(searchResultLocation) {
                        if (typeof(searchResultLocation) === "object") {
                            this.loadedSearchResult(searchResultLocation);
                        } else {
                            this.page = new RestBeans[this.searchConfig.listType]();
                            this.page.follow({
                                    url: searchResultLocation,
                                    cb: this.loadedSearchResult,
                                    scope: this
                            });
                        }
		},
		loadedSearchParameters: function (searchRestBean) {
			searchRestBean.getQueryParams().each(function(keyValue) {
				if(keyValue.getKey() === "searchTerm") {
					keyValue.setValue(this.searchTerm);
				} else if(keyValue.getKey() === "pageStart") {
					keyValue.setValue(this.pageOffset);
				} else if(keyValue.getKey() === "pageSize") {
					keyValue.setValue(this.pageSize);
				}
			}, this);
			searchRestBean.follow({
				rel: "query",
				cb: this.foundSearchResult,
				scope: this,
				payload: searchRestBean
			});
		},
		loadedSearches: function(searchInfo) {
			searchInfo.follow({
				rel: this.searchConfig.link,
				cb: this.loadedSearchParameters,
				scope: this
			});
		},
		loadPage: function() {
			if(!this.queryLink) {
				if(!this.searchConfig) {
					console.error("missing a search config for "+this.entityType);
				}
				RestBeans[this.entityType].prototype.followPrimaryResourceLink({
					hint: "add",
					rel: "self",
					cb: this.loadedFirstPage,
					scope: this
				});
//				RestBeans.root.follow({
//					rel: this.searchConfig.rootLink,
//					cb: this.loadedFirstPage,
//					scope: this
//				});
			} else {
				this.submitQuery();
			}
		},
		loadedFirstPage: function(page) {
			this.queryLink = page.hasLink("search");
			this.submitQuery();
		},
		submitQuery: function() {
			var parameters = new RestBeans.SearchParameters();
			var solrQueryString;
			var fieldsToUse = null;
			if(this.searchConfig.defaultFields) {
				for(var field in this.searchConfig.defaultFields) {
					fieldsToUse[field] = this.searchConfig.defaultFields[field];
				}
			}

			if(this.searchConfig.fields) {
				for(var i in this.searchConfig.fields) {
					if(!fieldsToUse) {
						fieldsToUse = {};
					}
					if(!this.searchTerm || this.searchTerm === "") {
						fieldsToUse[this.searchConfig.fields[i]] = "*";
					} else {
						fieldsToUse[this.searchConfig.fields[i]] = this.searchTerm;
					}

				}
			}		
			if(fieldsToUse) {
				for (var field in fieldsToUse) {
					if (!solrQueryString) {
						solrQueryString = "";
					} else {
						solrQueryString += " AND ";
					}
					solrQueryString += field + ":" + fieldsToUse[field];
				}
				parameters.setQuery(solrQueryString);
			} else {
				parameters.setSearchTerm(this.searchTerm);
			}
			parameters.setPage(new RestBeans.PaginationRestBean());
			parameters.getPage().setStart(this.pageOffset);
			parameters.getPage().setSize(this.pageSize);
                        this.applySorting(parameters);
			
			parameters.follow({
				link: this.queryLink,
				cb: this.foundSearchResult,
				scope: this,
				payload: parameters
			});
		}
	});
	return SearchTableDataProvider;
});
