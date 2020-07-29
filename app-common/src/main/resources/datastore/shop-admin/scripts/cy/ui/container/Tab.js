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
define(["cy/ui/container/ViewContainer"], function (ViewContainer) {

	var Tab = ViewContainer.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			this.callSuper(arguments, config);
		},
		renderTo: function (renderTarget) {
			this.view = $(renderTarget).append("<div></div>").children().last().hide();
			$(this.view).attr("class", this.name);
			this.renderTarget = this.view; // this is used for nested items. value of super implementation will be ignored
			this.invokeSuper(arguments);
		},
		renderItem: function (component) {
			this.invokeSuper(arguments);
		},
		show: function () {
			if (this.navItem) {
				$(this.navItem).addClass("active");
			}
			if (this.view) {
				this.view.show();
			}
		},
		hide: function () {
			if (this.navItem) {
				$(this.navItem).removeClass("active");
			}
			if (this.view) {
				$(this.view).hide();
			}
		}
	});
	return Tab;
});
