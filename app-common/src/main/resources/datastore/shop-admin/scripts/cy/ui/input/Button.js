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
define(["cy/ui/ViewComponent", "cy/ui/Text"], function (ViewComponent, Text) {
	var Button = ViewComponent.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			this.callSuper(arguments, config);
		},
		destroy: function () {
			this.invokeSuper(arguments);
			if (this.element) {
				$(this.element).remove();
			}
		},
		enable: function () {
			this.disabled = false;
			if (this.element) {
				$(this.element).removeClass("disabled");
			}
		},
		disable: function () {
			this.disabled = true;
			if (this.element) {
				$(this.element).addClass("disabled");
			}
		},
		renderTo: function (renderTarget) {
			var href = this.href;
			if (!href) {
				href = "#";
			}
			this.element = $(renderTarget).append("<a class=\"btn\"></a>").children().last();
			$(this.element).attr("href", href);
			this.labelText = new Text({value: this.label});
			this.labelText.renderTo(this.element);
			$(this.element).click(this._createClickListener());
			if (this.disabled) {
				this.disable();
			}
		},
		setLabel: function(label) {
			this.label = label;
			if(this.labelText) {
				this.labelText.value.set(this.label);
			}
		},
		_createClickListener: function () {
			var _this = this;
			return function () {
				if (!this.disabled) {
					_this.fireEvent("clicked", _this);
				}
			};
		}
	});
	return Button;
});
