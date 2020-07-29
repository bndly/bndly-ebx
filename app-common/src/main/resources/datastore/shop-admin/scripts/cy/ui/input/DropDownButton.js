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
define(["cy/ui/input/Button", "cy/Collection", "cy/ui/Text"], function (Button, Collection, Text) {
	var DropDownButton = Button.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			if (!config.items) {
				config.items = new Collection();
			} else if (!(config.items instanceof Collection)) {
				config.items = new Collection(config.items);
			}

			this.callSuper(arguments, config);
		},
		destroy: function () {
			this.invokeSuper(arguments);
			if (this.wrapper) {
				$(this.wrapper).remove();
			}
		},
		renderTo: function (renderTarget) {
			this.wrapper = $(renderTarget).append("<div class=\"btn-group\"></div>").children().last();
			this.element = $(this.wrapper).append("<button class=\"btn dropdown-toggle\" data-toggle=\"dropdown\">" + this.label + " <span class=\"caret\"></span></button>").children().last();
			this.dropDownMenu = $(this.wrapper).append("<ul class=\"dropdown-menu\"></div>").children().last();
			this.items.each(function (item) {
				var label = item.label;
				if (!label) {
					label = item.name;
				}
				item.view = $(this.dropDownMenu).append("<li></li>").children().last();
				var text = new Text({value: label});
				item.button = $(item.view).append("<a href=\"#\"></a>").children().last();
				text.renderTo($(item.button));
				$(item.view).click(this._createClickListener(item));
			}, this);
		},
		_createClickListener: function (item) {
			var _this = this;
			return function () {
				_this.fireEvent("clicked", _this, item);
			};
		}
	});
	return DropDownButton;
});
