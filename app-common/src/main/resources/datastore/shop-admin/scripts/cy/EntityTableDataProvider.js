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
define(["cy/TableDataProvider", "cy/RestBeans"], function(TableDataProvider, RestBeans) {
	var EntityTableDataProvider = TableDataProvider.extend({
		construct: function(config) {
			if(!config) {
				config = {};
			}
			if(config.pageSize === undefined || config.pageSize === null) {
				config.pageSize = 10;
			}
			if(config.pageOffset === undefined || config.pageOffset === null) {
				config.pageOffset = 0;
			}
			if(config.proto) {
				var _this = this;
				config.proto.addListener("foundSimilar", function(proto, page) {
					_this.page = page;
					_this.fireEvent("load", page.getItems());
				}, this) ;
			}
			this.callSuper(arguments, config);
		},
		getTotalPages: function() {
			var count = 0;
			if(this.page) {
				var paginationInfo = this.page.getPage();
				if(paginationInfo) {
					count = paginationInfo.getTotalRecords();
					if(count) {
						var ps = paginationInfo.getSize();
						count = count % ps === 0 ? count / ps : Math.round(count / ps + 0.5);
					}
				}
			}
			return count;
		},
		getCurrentPage: function() {
			return this.pageOffset / this.pageSize + 1;
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
		loadPage: function() {
			var proto;
			if(this.proto) {
				proto = this.proto;
			} else if(this.entityType) {
				proto = new RestBeans[this.entityType]();
				var _this = this;
				proto.addListener("foundSimilar", function(proto, page){
					_this.page = page;
					_this.fireEvent("load", page.getItems());
				}, this) ;
			}
			var p = new RestBeans.PaginationRestBean();
			p.setSize(this.pageSize);
			p.setStart(this.pageOffset);
			proto.setPage(p);
                        this.applySorting(proto);
			proto.findAll();
		}
	});
	return EntityTableDataProvider;
});
