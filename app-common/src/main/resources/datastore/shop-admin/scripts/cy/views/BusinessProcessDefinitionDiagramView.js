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
define(["cy/ui/container/ViewContainer", "cy/ui/Image"], function(ViewContainer, Image) {
    var BusinessProcessDefinitionDiagramView = ViewContainer.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.image = new Image({
                emptyText: "There is no diagram data available."
            });
            config.items = [
                config.image
            ];
            this.callSuper(arguments, config);
            this.listen(this.parent, "persisted", this.didPersistDefinition, this);
        },
        renderTo: function(renderTarget) {
            this.invokeSuper(arguments);
            this.showDiagram(this.parent);
        },
        didPersistDefinition: function(def) {
            def.addSingleInvocationListener("reloaded", this.didReloadDefinition, this);
        },
        didReloadDefinition: function(def) {
            this.showDiagram(def);
        },
        showDiagram: function(def) {
            var l = def.hasLink("diagramData");
            if (l) {
                def.follow({
                    link: l,
                    cb: function(data) {
                        var dl = data.hasLink("download");
                        if (dl) {
                            this.image.setSource(dl.getHref());
                        }
                    },
                    scope: this
                });
            }
        }
    });
    return BusinessProcessDefinitionDiagramView;
});
