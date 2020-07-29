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
define(["cy/ui/container/InputViewContainer", "cy/Collection", "cy/ui/Text"], function (InputViewContainer, Collection, Text) {
	var FieldSet = InputViewContainer.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}

			if (!config.legend) {
				config.legend = "";
			}
			this.callSuper(arguments, config);
		},
		destroy: function () {
			this.invokeSuper(arguments);
			if (this.element) {
				$(this.element).remove();
			}
		},
		renderTo: function (renderTarget) {
			this.element = $(renderTarget).append("<fielset></fielset>").children().last();
			if (this.legend && this.legend.trim() !== "") {
				this.legendElement = new Text({value: this.legend, tag: "legend"});
				this.legendElement.renderTo($(this.element));
			}
			this.renderTarget = this.element;
			this.invokeSuper(arguments);
			if (this.hidden) {
				this.hide();
			}
		},
		hide: function () {
			if (this.element) {
				$(this.element).hide();
			}
		},
		show: function () {
			if (this.element) {
				$(this.element).show();
			}
		}
	});
	return FieldSet;
});
