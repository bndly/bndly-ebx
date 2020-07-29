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
define(["cy/RestBeans", "cy/EntityCollection", "cy/ui/Link"], function(RestBeans, EntityCollection, Link) {
    return [
        {
            items: [{
                    member: "name",
                    labelText: "Code",
                    mandatory: true
                }, {
                    key: "download",
                    labelText: "Download as Java Properties",
                    constructorFn: Link,
                    init: function(link, entry) {
                        var applyDownloadLink = function(entity){
                            var dl = entity.hasLink("properties");
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
