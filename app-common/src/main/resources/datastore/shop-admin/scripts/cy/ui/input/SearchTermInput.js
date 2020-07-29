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
define(["cy/ui/input/TextInput"], function (TextInput) {
	var SearchTermInput = TextInput.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			this.callSuper(arguments, config);
		},
		destroy: function () {
			this.invokeSuper(arguments);
		},
		renderTo: function (renderTarget) {
			this.form = $(renderTarget).append("<form class=\"form-search\">").children().last();
			$(this.form).submit(function () {
				return false;
			});
			this.wrapper = $(this.form).append("<div></div>").children().last();
			$(this.wrapper).addClass("input-append");
			this.callSuper(arguments, this.wrapper);
			$(this.input).addClass("search-query");
			$(this.input).keyup(function (event) {
				_this.keyup(event);
			});
			this.button = $(this.wrapper).append("<button class=\"btn\" type=\"button\">Search</button>").children().last();
			var _this = this;
			$(this.button).click(function () {
				_this.submitSearch();
				return false;
			});
		},
		submitSearch: function () {
			this.fireEvent("searchTermSubmitted", this.getValue(), this);
			return false;
		},
		keyup: function (event) {
			// Enter key = keycode 13
			if (event.which === 13) {
				this.submitSearch();
			}
			return false;
		}
	});
	return SearchTermInput;
});
