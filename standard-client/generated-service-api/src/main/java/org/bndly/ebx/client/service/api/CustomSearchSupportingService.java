package org.bndly.ebx.client.service.api;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service-api
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

import org.bndly.rest.client.exception.ClientException;
import java.util.ArrayList;
import java.util.List;

public interface CustomSearchSupportingService<MODEL> {

	static class Pagination {

		private Long pageSize;
		private Long currentPageIndex;
		private Long totalPages;

		public Long getPageSize() {
			return pageSize;
		}

		public void setPageSize(Long pageSize) {
			this.pageSize = pageSize;
		}

		public Long getCurrentPageIndex() {
			return currentPageIndex;
		}

		public void setCurrentPageIndex(Long currentPageIndex) {
			this.currentPageIndex = currentPageIndex;
		}

		public Long getTotalPages() {
			return totalPages;
		}

		public void setTotalPages(Long totalPages) {
			this.totalPages = totalPages;
		}
	}

	static class SearchResult<DOCTYPE> {

		private Pagination pagination;
		private String query;
		private long totalHits;
		private List<DOCTYPE> items = new ArrayList<>();

		public long getTotalHits() {
			return totalHits;
		}

		public void setTotalHits(long totalHits) {
			this.totalHits = totalHits;
		}

		public List<DOCTYPE> getItems() {
			return items;
		}

		public void setItems(List<DOCTYPE> items) {
			this.items = items;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getQuery() {
			return query;
		}

		public Pagination getPagination() {
			return pagination;
		}

		public void setPagination(Pagination pagination) {
			this.pagination = pagination;
		}

	}

	SearchResult<MODEL> search(String q, long start, long hitsPerPage) throws ClientException;
}
