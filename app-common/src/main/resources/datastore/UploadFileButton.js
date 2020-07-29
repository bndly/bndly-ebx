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
	var UploadFileButton = Class.extend("UploadFileButton", {
		construct: function(config) {
			this.config = config;
		},
		bindTo: function(target) {
			var wrapper = $(target),
				progressBarWrapper = wrapper.children(".upload-progress-wrapper"),
				progressBar = progressBarWrapper.children(".upload-progress"),
				label = wrapper.children(".file-upload-label"),
				that = this,
				uploadForm = wrapper.find("form"),
				dataInput = uploadForm.find("input[type='file']"),
				url = uploadForm.attr("action")
			;
			this.progressBarWrapper = progressBarWrapper;
			this.progressBar = progressBar;
			this.label = label;
			this.url = url;
			target.addEventListener("dragenter", function(e) {
				if(!that.isUploading) {
					e.preventDefault();
					return false;
				}
			}, false);
			target.addEventListener("dragover", function(e) {
				if(!that.isUploading) {
					e.preventDefault();
					return false;
				}
			}, false);
			target.addEventListener("drop", function(e) {
				if(that.isFileEvent(e) && !that.isUploading) {
					return that.onDrop(e);
				}
			}, false);
			target.UploadFileButton = this;
		},
		isFileEvent: function(e) {
			var 
				dt = e.dataTransfer,
				files = e.dataTransfer.files,
				file = files ? files[0] : undefined
			;
			return file !== undefined;
		},
		createUploadFunction: function(file, uploadUrlBase, completeCallback) {
			var that = this;
			return function() {
				var 
					
					formData = new FormData(),
					patchedName = file.name,
					uploadUrl = uploadUrlBase+"/"+patchedName+"?nodeType=fs%3Afile"
				;
				formData.append("data@BINARY", file, file.name);
				formData.append("name@STRING", file.name);
				formData.append("contentType@STRING", file.type);

				that.setProgress(0);
				that.hideLabel();
				$.ajax({
					url: uploadUrl,
					type: "POST",
					data: formData,
					processData: false,
					contentType: false,
					xhr: function() {
						var xhr = $.ajaxSettings.xhr();
						xhr.upload.addEventListener("progress", function(evt){
							that.setProgress((evt.loaded*100)/evt.total);
						}, false);
						xhr.upload.addEventListener("load", function(evt) {
							that.showLabel();
							completeCallback();
						}, false);
						xhr.upload.addEventListener("error", function(evt) {
							that.showLabel();
							completeCallback();
						}, false);
						xhr.upload.addEventListener("abort", function(evt) {
							that.showLabel();
							completeCallback();
						}, false);
						return xhr;
					}
				});
			}
		},
		onDrop: function(e) {
			this.isUploading = true;
			e.preventDefault();
			
			var 
				uploads = [],
				i,
				file = e.dataTransfer.files[0],
				uploadUrlBase = this.url,
				namePathRegex = /([a-zA-z0-9\-_]+)/g,
				tmp = true,
				that = this,
				completeCallback = function() {
					if(uploads.length > 0) {
						(uploads.pop())();
					} else {
						that.isUploading = false;
					}
				}
			;
			
			for(i = e.dataTransfer.files.length-1; i >= 0 ; i--) {
				uploads.push(this.createUploadFunction(e.dataTransfer.files[i], uploadUrlBase, completeCallback));
			}
			completeCallback();
			return false;
		},
		setProgress: function(percent) {
			this.progressBar.css({width: percent+"%"});
		},
		setFinished: function() {
			this.progressBar.css({width: "100%"});
		},
		toggleLabel: function() {
			if(this.label.is(".hidden")) {
				this.showLabel();
			} else {
				this.hideLabel();
			}
		},
		showLabel: function() {
			this.progressBarWrapper.addClass("hidden");
			this.label.removeClass("hidden");
		},
		hideLabel: function() {
			this.progressBarWrapper.removeClass("hidden");
			this.label.addClass("hidden");
		}
	});
	return UploadFileButton;
});
