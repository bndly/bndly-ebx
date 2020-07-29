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
define(["cy/Collection"], function(Collection) {

	var EntityCollection = Collection.extend({
		construct: function(config) {
			if(!config) {
				config = {};
			}
			this.callSuper(arguments, config);
                        if(this.proto) {
                            this.listen(this.proto, "foundSimilar", this.loadPageItemsToCollection, this);
                        }
		},
		load: function(){
			this.proto.findAll();
		},
		loadPageItemsToCollection: function(proto, page, callback) {
			// load the next page
			var next = page.hasLink("next"),
			hasMore = undefined;
			if (next) {
				hasMore = true;
				page.follow({
					link: next,
					cb: function(nextPage) {
						this.loadPageItemsToCollection(proto, nextPage);
					},
					scope: this
				});
			} else {
				hasMore = false;
			}

			// render the already known attributes
			this.addAll(page.getItems());
			if(!hasMore) {
				this.fireEvent("allLoaded", this);
			}
		},
                setProto: function(proto){
                    if(!proto) {
                        throw new Error("setting entity collection prototype illegal value");
                    }
                    if(this.proto) {
                        this.proto.destroy();
                    }
                    this.proto = proto;
                    this.listen(this.proto, "foundSimilar", this.loadPageItemsToCollection, this);
                    return this;
                }
	});
	return EntityCollection;
});
