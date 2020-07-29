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
define(["cy/ui/ViewComponent"], function (ViewComponent) {
	var ProgressBar = ViewComponent.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			if (!config.percent) {
				config.percent = 0;
			}

			if (config.percent > 100) {
				config.percent = 100;
			} else {
				config.percent = 0;
			}
			this.callSuper(arguments, config);
		},
		renderTo: function (renderTarget) {
			this.wrapper = $(renderTarget).append("<div class=\"progress\"></div>").children().last();
			if (this.hidden) {
				this.hide();
			}
			this.bar = $(this.wrapper).append("<div class=\"bar\"></div>").children().last();
			this.setPercent(this.percent);
		},
		hide: function () {
			$(this.wrapper).hide();
		},
		show: function () {
			$(this.wrapper).show();
		},
		setPercent: function (percent) {
			this.percent = percent;
			if (this.bar) {
				$(this.bar).width(this.percent + "%");
			}
		},
		none: function(msg) {
			this._status("", msg);
		},
		success: function(msg) {
			this._status("bar-success", msg);
		},
		warn: function(msg) {
			this._status("bar-warning", msg);
		},
		error: function(msg) {
			this._status("bar-danger", msg);
		},
		_status: function (cls, msg) {
			this.bar.attr("class", "bar "+cls);
			if(msg) {
				this.bar.text(msg);
			}
		}
	});
	return ProgressBar;
});
