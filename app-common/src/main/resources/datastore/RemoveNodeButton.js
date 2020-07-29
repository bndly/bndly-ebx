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
define([
	"Class",
	"jquery"
], function(
	Class,
	$
) {
	var RemoveNodeButton = Class.extend("RemoveNodeButton", {
		construct: function(config) {
			this.config = config;
			this.configByType = {};
		},
		bindTo: function(document) {
			$(document).on("click", ".js-removenode", function(event){
				var modal = $(".js-removemodal").first();
				modal.show();
				event.preventDefault();
			});
			$(document).on("click", ".js-removemodal .js-dismiss", function(event){
				$(event.target).closest(".js-removemodal").hide();
				event.preventDefault();
			});
		}
	});
	return RemoveNodeButton;
});
