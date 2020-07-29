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
	var FormCreateChild = Class.extend("FormCreateChild", {
		construct: function() {

		},
		bindTo: function(document) {
			if(!document.FormCreateChild) {
				$(document).on("submit", ".js-createchild", function(e) {
					var
						$form = $(e.target),
						$nameInput = $form.find("input[name='n']").first(),
						$typeInput = $form.find("select[name='nt']").first(),
						action = $form.attr("action"),
						newaction = action+"/"+$nameInput.val()+"?"+$.param({nodeType:$typeInput.val()})
					;
					e.preventDefault();
					$.ajax({
						url: newaction,
						data: {},
						type: "POST",
						success: function() {
							window.location = window.location;
						},
						error: function() {

						}
					});
				});
				document.FormCreateChild = this;
			}
		}
	});
	return FormCreateChild;
});
