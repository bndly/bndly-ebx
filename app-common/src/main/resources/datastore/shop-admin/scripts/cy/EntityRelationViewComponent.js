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
define(["cy/ui/container/ViewContainer"], function(ViewContainer) {

	var EntityRelationViewComponent = ViewContainer.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			this.callSuper(arguments, config);
		},
		renderTo: function(renderTarget) {
			this.invokeSuper(arguments);
		},
		loadPageItemsToCollection: function(proto, page, callback, collection) {
			// load the next page
			var next = page.hasLink("next");
			if (next) {
				page.follow({
					link: next,
					cb: function(nextPage) {
						callback(proto, nextPage);
					},
					scope: this
				});
			};

			// render the already known attributes
			collection.addAll(page.getItems());
		}
	});
	return EntityRelationViewComponent;
});
