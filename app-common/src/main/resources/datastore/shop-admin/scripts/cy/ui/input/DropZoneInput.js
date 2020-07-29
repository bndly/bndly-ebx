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
	"cy/ui/input/InputViewComponent",
	"cy/ui/input/InputTypeRegistry",
	"cy/ProgressBar"
], function(
	InputViewComponent,
	InputTypeRegistry,
	ProgressBar
) {
	var DropZone = InputViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
			config.progressBar = new ProgressBar({hidden: true});
			this.callSuper(arguments, config);
		},
		destroy: function() {
			this.progressBar.destroy();
			this.invokeSuper(arguments);
		},
		renderTo: function(renderTarget) {
			this.element = $(renderTarget).append("<div class=\"well well-small dropZone\"><i class=\"icon-plus-sign\"></i> upload new data</div>").children().last();
			var el = this.element[0],
				_this = this;
			el.ondragover = function() {
				_this.onDragOver();
				return false;
			};
			el.ondragend = function() {
				_this.onDragEnd();
				return false;
			};
			el.ondrop = function(e) {
				_this.onDrop(e);
				return false;
			};
			this.progressBar.renderTo(renderTarget);
		},
		onDragOver: function() {

		},
		onDragEnd: function() {

		},
		onDrop: function(e) {
			e.preventDefault();

			this.progressBar.setPercent(0);
			this.progressBar.show();
			var file = e.dataTransfer.files[0],
			pb = this.progressBar;
			pb.none();
			pb.show();
			$.ajax({
				url: this.uploadLocation+file.name,
				type: "POST",
				data: file,
				processData: false,
				headers: {
					"Content-Type": file.type
				},
				xhr: function() {
					var xhr = $.ajaxSettings.xhr();
					xhr.upload.addEventListener("progress", function(evt){
						pb.setPercent((evt.loaded*100)/evt.total);
						pb.none(parseInt(pb.percent)+"% completed");
					}, false);
					xhr.upload.addEventListener("load", function(evt) {
						pb.success("upload complete");
					}, false);
					xhr.upload.addEventListener("error", function(evt) {
						pb.error("upload failed");
					}, false);
					xhr.upload.addEventListener("abort", function(evt) {
						pb.warn("upload cancelled");
					}, false);
					return xhr;
				}
			});
		},
		getValue: function() {
			return undefined;
		},
		setValue: function(value) {}
	});
	InputTypeRegistry.register("DropZone", DropZone);
	return DropZone;
});
