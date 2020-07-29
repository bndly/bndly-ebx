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
define(["cy/Observable", "cy/Collection", "cy/RestBeans"], function(Observable, Collection, RestBeans) {
	var TableDataProvider = Observable.extend({
		construct: function(config) {
			if(!config) {
				config = {};
			}
			if(!config.items) {
				config.items = new Collection();				
			}
			this.callSuper(arguments, config);
		},
		getCurrentPage: function() {
			return this.pageOffset / this.pageSize + 1;
		},
		getTotalPages: function() {
			return 0;
		},
		getPageSize: function() {
			return this.pageSize;
		},
		getPageOffset: function() {
			return this.pageOffset;
		},
		getPageItems: function() {
			return this.items;
		},
		setPageSize: function(size, shouldReload) {
			this.pageSize = size;
			if(shouldReload) {
				this.loadPage();
			}
		},
		setPageOffset: function(offset, shouldReload) {
			this.pageOffset = offset;
			if(shouldReload) {
				this.loadPage();
			}
		},
                setSortField: function(sortField) {
			this.sortField = sortField;
		},
                applySorting: function(parameters){
                    if(this.sortField) {
                        parameters.setSorting(new RestBeans.SortRestBean());
                        parameters.getSorting().setField(this.sortField);
                        parameters.getSorting().setAscending(this.sortAscending);
                    }
                },
                toggleSortDirection: function() {
			if(!this.sortAscending) {
                            this.sortAscending = true;
                        } else {
                            this.sortAscending = false;
                        }
		},
		loadPage: function() {
			throw new Error("implement a loadPage method");
		}
	});
	return TableDataProvider;
});
