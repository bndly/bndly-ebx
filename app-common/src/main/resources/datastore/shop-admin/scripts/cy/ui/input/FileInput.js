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
define(["cy/ui/input/InputViewComponent", "cy/RestBeans", "cy/ui/input/InputTypeRegistry"], function(InputViewComponent, RestBeans, InputTypeRegistry) {
    var FileInput = InputViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if (config.eager === undefined || config.eager === null) {
                config.eager = true;
            }
            this.callSuper(arguments, config);
        },
        destroy: function() {
            this.invokeSuper(arguments);
        },
        renderTo: function(renderTarget) {
            this.wrapperForm = $(renderTarget).append("<form/>").children().last().hide();
            if (FileInput.prototype.__counter === undefined) {
                FileInput.prototype.__counter = 0;
            } else {
                FileInput.prototype.__counter += 1;
            }
            this.wrapperForm.attr("id", "fileUploadForm" + FileInput.prototype.__counter);
            this.input = $(this.wrapperForm).append("<input type=\"file\" />").children().last();
            this.input.attr("id", "fileToUpload" + FileInput.prototype.__counter);
            $(this.input).hide();
            if (this.id) {
                $(this.input).attr("id", this.id);
            }
            if (this.name) {
                $(this.input).attr("name", this.name);
            }
            this.wrapperForm.attr("enctype", "multipart/form-data");
            this.wrapperForm.attr("method", "post");

            this.button = $(renderTarget).append("<a class=\"btn btn-small btn-primary\" href=\"#\"><i class=\"icon-upload icon-white\"></i> select</a>").children().last();
            var _input = this.input;
            $(this.button).click(function() {
                $(_input).click();
                return false;
            });

            var _this = this;
            $(this.input).change(function() {
                _this.fileSelected();
                return false;
            });
        },
        isFileSelected: function() {
            return this._fileSelected ? true : false;
        },
        fileSelected: function() {
            this._fileSelected = true;
            this.fireEvent("selected", this);
            // just for testing purpose
            if(this.eager) {
                this.upload();
            }
        },
        getFileDescription: function(){
            var f = document.getElementById(this.wrapperForm.attr("id"));
            var fd;
            if (f.getFormData && typeof (f.getFormData) === "function") {
                fd = f.getFormData();
                console.warn("failed to get file description");
                return undefined;
            } else {
                fd = new FormData();
                var file = f.getElementsByTagName("input")[0].files[0];
                return file;
            }
        },
        upload: function(urlOverride) {
            if (urlOverride) {
                this.uploadToUrl(urlOverride);
            } else {
                var drb = new RestBeans.BinaryDataRestBean();
                var _this = this;

                var xhr = new XMLHttpRequest();
                xhr.withCredentials = true;
                var f = document.getElementById(this.wrapperForm.attr("id"));
                var fd;
                if (f.getFormData && typeof (f.getFormData) === "function") {
                    fd = f.getFormData();
                } else {
                    fd = new FormData();
                    var file = f.getElementsByTagName("input")[0].files[0];
                    drb.setName(file.name);
                    drb.setContentType(file.type);
                    if(!drb.getContentType()) {
                        drb.setContentType(null);
                    }
                    drb.addSingleInvocationListener("notFound", function(data) {
                        drb.persistAndReload();                        
                    }, this);
                    drb.addSingleInvocationListener("reloaded", function(data) {
                        var uploadLink = data.hasLink("upload");
                        if (uploadLink) {
                            console.log("starting upload");
                            this.wrapperForm.attr("action", uploadLink.getHref());
                            xhr.open("POST", uploadLink.getHref());
                            xhr.send(fd);
                        } else {
                            console.log("missing upload link");
                        }
                    }, this);
                    drb.find();
                    fd.append("fileToUpload", file);
                }

                /* event listners */
                xhr.upload.addEventListener("progress", function(evt) {
                    if (evt.lengthComputable) {
                        var percentComplete = Math.round(evt.loaded * 100 / evt.total);
                        console.log("upload progress " + percentComplete.toString() + "%");
                    }
                }, false);

                xhr.addEventListener("load", function() {
                    console.log("upload completed");
                    _this.fireEvent("uploaded", this, drb);
                }, false);
                //xhr.addEventListener("error", uploadFailed, false);
                //xhr.addEventListener("abort", uploadCanceled, false);
                /* Be sure to change the url below to the url of your upload server side script */


                //this.wrapperForm.submit();
            }
        },
        uploadToUrl: function(url) {
            var _this = this;

                var xhr = new XMLHttpRequest();
                xhr.withCredentials = true;
                var f = document.getElementById(this.wrapperForm.attr("id"));
                var fd;
                if (f.getFormData && typeof (f.getFormData) === "function") {
                    fd = f.getFormData();
                } else {
                    fd = new FormData();
                    var file = f.getElementsByTagName("input")[0].files[0];
                    fd.append("fileToUpload", file);
                    if (url) {
                        console.log("starting upload");
                        this.wrapperForm.attr("action", url);
                        xhr.open("POST", url);
                        xhr.send(fd);
                    } else {
                        console.log("missing upload link");
                    }
                }

                /* event listners */
                xhr.upload.addEventListener("progress", function(evt) {
                    if (evt.lengthComputable) {
                        var percentComplete = Math.round(evt.loaded * 100 / evt.total);
                        console.log("upload progress " + percentComplete.toString() + "%");
                    }
                }, false);

                xhr.addEventListener("load", function() {
                    console.log("upload completed");
                    _this.fireEvent("uploaded", this);
                }, false);
                //xhr.addEventListener("error", uploadFailed, false);
                //xhr.addEventListener("abort", uploadCanceled, false);
                /* Be sure to change the url below to the url of your upload server side script */


                //this.wrapperForm.submit();
        },
        getValue: function() {
            return undefined;
        },
        setValue: function(value) {
        }
    });
    InputTypeRegistry.register("FileInput", FileInput);
    return FileInput;
});
