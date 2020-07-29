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
define(["cy/ui/input/Button", "cy/ui/IFrame", "cy/ui/Link"], function(Button, IFrame, Link) {
    return [
        {
            items: [{
                    member: "name",
                    labelText: "Name",
                    mandatory: true
                }, {
                    member: "contentType",
                    labelText: "Content type",
                    mandatory: true
                }, {
                    member: "createdOn",
                    labelText: "Created on"
                }, {
                    member: "updatedOn",
                    labelText: "Updated on"
                }, {
                    key: "upload",
                    labelText: "Upload",
                    constructorFn: "FileInput",
                    inputConfig: {
                        eager: false
                    },
                    init: function(instance) {
                        instance.addListener("selected", function() {
                            var fd = instance.getFileDescription();
                            if (!instance.entity.getName()) {
                                instance.entity.setName(fd.name);
                            }
                            if (!instance.entity.getContentType()) {
                                instance.entity.setContentType(fd.type);
                            }
                        }, this);
                        instance.entity.addListener("persisted", function() {
                            instance.entity.addSingleInvocationListener("reloaded", function(data) {
                                if (instance.isFileSelected()) {
                                    console.log("should upload now");
                                    var ul = data.hasLink("upload");
                                    if (ul) {
                                        instance.uploadToUrl(ul.getHref());
                                    }
                                } else {
                                    console.log("nothing to upload");
                                }
                            }, this);
                        }, this);
                    }
                }, {
                    key: "download",
                    labelText: "Download",
                    constructorFn: Link,
                    init: function(link, entry) {
                        var applyDownloadLink = function(entity){
                            var dl = entity.hasLink("download");
                            if(dl) {
                                link.setUrl(dl.getHref());
                            } else {
                                link.setUrl();
                            }
                        };
                        entry.addListener("changed", function(entity, member, value, oldValue){
                            applyDownloadLink(entity);
                        }, this);
                        applyDownloadLink(entry);
                    }
                }]
        }
    ];

});
