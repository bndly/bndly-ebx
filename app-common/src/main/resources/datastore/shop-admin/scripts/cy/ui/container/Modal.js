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
define(["cy/ui/ViewComponent", "cy/HTMLUtil"], function (ViewComponent, HTMLUtil) {

	var Modal = ViewComponent.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			this.callSuper(arguments, config);
		},
		renderTo: function (renderTarget) {
			var bonusAttributes = {
				class: "modal hide fade"
			};
			if (this.large) {
				bonusAttributes.class += " large";
			}

			var alertClass;
			if (this.isWarning) {
				alertClass = "alert-block";
			} else if (this.isError) {
				alertClass = "alert-error";
			} else if (this.isInfo) {
				alertClass = "alert-info";
			} else if (this.isSuccess) {
				alertClass = "alert-success";
			}

			if (alertClass) {
				bonusAttributes.class += " alert " + alertClass;
				bonusAttributes.style = "padding: inherit;";
			}

			this.modal = $(renderTarget)
					.append("<div " + HTMLUtil.serializeBonusAttributes(bonusAttributes) + "></div>")
					.children()
					.last();

			var _this = this;
			$(this.modal)
					.on('hidden', function () {
						if (_this.destroyOnClose) {
							_this.destroy();
						}
					});


			this.header = $(this.modal)
					.append("<div class=\"modal-header\">")
					.children()
					.last();
			var style = "";
			if (alertClass) {
				style = "style=\"right: inherit;\"";
			}
			this.closeButton = $(this.header)
					.append("<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" " + style + ">&times;</button>")
					.children()
					.last();
			this.headerTextWrapper = $(this.header)
					.append("<h3></h3>")
					.children()
					.last();
			if (this.headerText) {
				$(this.headerTextWrapper)
						.append(this.headerText)
						.children()
						.last();
			}

			this.body = $(this.modal)
					.append("<div class=\"modal-body\"></div>")
					.children()
					.last();
			this.footer = $(this.modal)
					.append("<div class=\"modal-footer\"></div>")
					.children()
					.last();
			if (this.actions) {
				var createClickCallback = function (modal, btn, event, action) {
					return function () {
						modal.fireEvent(event, btn, action);
					};
				};
				for (var i in this.actions) {
					var action = this.actions[i];
					if (action.event) {
						var label = action.labelText;
						if (!label) {
							label = action.event;
						}
						var btn = $(this.footer)
								.append("<a href=\"#\" class=\"btn\">" + label + "</a>")
								.children()
								.last();
						$(btn)
								.click(createClickCallback(this, btn, action.event, action));
					}
				}
			}
		},
		show: function () {
			$(this.modal).modal('show');
		},
		hide: function () {
			$(this.modal).modal('hide');
		},
		destroy: function () {
			$(this.modal).remove();
		}
	});
	return Modal;
});
