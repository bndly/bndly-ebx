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
define(["cy/ui/input/Button", "cy/ui/IFrame"], function(Button, IFrame) {
    return {
        listeners: {
            beforePersist: function(entry, inputsByMemberOrKey, cb) {
                console.log("before persist");
                inputsByMemberOrKey.upload.addSingleInvocationListener("uploaded", function(input, data) {
                    entry.setResourceName(data.getName());
                    cb();
                }, this);
                inputsByMemberOrKey.upload.upload();
            }
        },
        sections: [
            {
                items: [{
                        member: "name",
                        labelText: "Name",
                        generated: true
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
                                instance.entity.setResourceName(fd.name);
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
                        member: "category",
                        labelText: "Category",
                        generated: true
                    }, {
                        member: "version",
                        labelText: "Version",
                        generated: true
                    }, {
                        member: "resourceName",
                        labelText: "Resource name",
                        generated: true
                    }, {
                        member: "diagramResourceName",
                        labelText: "Diagram name",
                        generated: true
                    }, {
                        member: "description",
                        labelText: "Description",
                        generated: true
                    }, {
                        member: "deploymentId",
                        labelText: "Deployment ID",
                        generated: true
                    }, {
                        member: "key",
                        labelText: "Key",
                        generated: true
                    }, {
                        member: "id",
                        labelText: "ID",
                        generated: true
                    }]
            }
        ]};

});
