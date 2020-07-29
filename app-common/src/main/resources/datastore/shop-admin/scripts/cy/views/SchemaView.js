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
define(["cy/RestBeans", "cy/ui/input/Button", "cy/ui/container/ViewContainer", "cy/ui/ViewComponent", "cy/ui/Text"], function(RestBeans, Button, ViewContainer, ViewComponent, Text) {
    var SchemaView = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.titleText = new Text({value: "Deployed Schema Status", tag: "h1"});
            config.container = new ViewContainer({
                items: [
                ]
            });
            this.callSuper(arguments, config);
            RestBeans.root.follow({
                rel: "schema",
                cb: this.schemaLoaded,
                scope: this
            });
        },
        destroy: function() {
            this.invokeSuper(arguments);
			this.titleText.destroy();
        },
        renderTo: function(renderTarget) {
            this.renderTitle(renderTarget);
            this.container.renderTo(renderTarget);
            this.schemaRenderTarget = $(renderTarget).append("<div id=\"schemaRenderTarget\"></div>").children().last();
            this.renderSchema();
            this.invokeSuper(arguments);
        },
        renderTitle: function(renderTarget) {
            if (this.titleText) {
                this.titleText.renderTo(renderTarget);
            }
        },
        schemaLoaded: function(schema) {
            this.schema = schema;
            if(schema.getItems()) {
                schema.getItems().each(function(item){
                    if(item.getName() === "ebx") {
                        this.schema=item;
                    }
                }, this);
            }
        },
        renderSchema: function() {
            if (!this.schema) {
                return false;
            }
            if (!this.schemaRenderTarget) {
                return false;
            }
            // render

            var rootNode = {
                id: "rootNode",
                name: "ActiveRecord",
                data: {},
                children: []
            };
            var types = {};
            var json = {
                nodes: [],
                links: []
            };
            var i = 0;
            var assertNodeExists = function(attributeHolder) {
                var n = attributeHolder.getName();
                if (!types[n]) {
                    types[n] = {
                        title: n,
                        name: n,
                        group: i
                    };
                    i++;
                    json.nodes.push(types[n]);
                }
                return types[n];
            };
            // create the raw nodes
            this.schema.getTypes().each(function(type) {
                assertNodeExists(type);
            }, this);

            this.schema.getMixins().each(function(mixin) {
                assertNodeExists(mixin);
            }, this);

            //rebuild the hierarchy
            this.schema.getTypes().each(function(type) {
                var n = type.getName();
                type.getAttributes().getItems().each(function(att) {
                    var attHolder;
                    if ((att instanceof RestBeans["TypeAttributeBean"])) {
                        attHolder = att.get("typeBean");
                    } else if ((att instanceof RestBeans["MixinAttributeBean"])) {
                        attHolder = att.get("mixinBean");
                    } else if ((att instanceof RestBeans["JSONAttributeBean"])) {
                        attHolder = att.get("namedAttributeHolder");
                    }
                    if (attHolder) {
//                        console.log("create link from "+type.getName()+" to "+attHolder.getName());
                        json.links.push({
                            source: types[n].group,
                            target: types[attHolder.getName()].group,
                            value: 1
                        });
                    }
                }, this);

                if (type.getParentTypeName()) {
                    json.links.push({
                        source: types[n].group,
                        target: types[type.getParentTypeName()].group,
                        value: 2
                    });
                    
                } else {
                    rootNode.children.push(assertNodeExists(type));
                }
                var tmpMixins = type.getMixins();
                if(tmpMixins) {
                    tmpMixins.each(function(mixin){
                        json.links.push({
                            source: types[n].group,
                            target: types[mixin].group,
                            value: 2
                        });
                    }, this);
                }
                
            }, this);

            var width = 1200;
            var height = 800;
            var svg = $(this.schemaRenderTarget).append("<svg id=\"cloud\" width=\""+width+"\" height=\""+height+"\"><defs><marker id=\"arrow\" viewbox=\"0 -5 10 10\" refX=\"18\" refY=\"0\"markerWidth=\"6\" markerHeight=\"6\" orient=\"auto\"><path d=\"M0,-5L10,0L0,5Z\"></marker></defs></svg>").children().last();
            var color = d3.scale.category10();

            var force = d3.layout.force()
                    .charge(-180)
                    .linkDistance(70)
                    .size([width, height]);

            var svg = d3.select("#cloud");

            force
                    .nodes(json.nodes)
                    .links(json.links)
                    .start();

            var link = svg.selectAll(".link")
                    .data(json.links)
                    .enter().append("line")
                    .attr("marker-end", function(d) {
                        if(d.value === 2) {
                            return "url(#arrow)";
                        }
                        return undefined;
                    })
                    .attr("class", "link");

            var node = svg.selectAll(".node")
                    .data(json.nodes)
                    .enter().append("g")
                    .attr("class", "node")
                    .call(force.drag);

            node.append("image")
                    .attr("xlink:href", "https://github.com/favicon.ico")
                    .attr("x", -8)
                    .attr("y", -8)
                    .attr("width", 16)
                    .attr("height", 16);

            node.append("circle")
                    .attr("r", 8)
                    .style("fill", function(d) {
                        return color(d.group);
                    });

            node.append("text")
                    .attr("dx", 12)
                    .attr("dy", ".35em")
                    .text(function(d) {
                        return d.name;
                    });

            force.on("tick", function() {
                link.attr("x1", function(d) {
                    return d.source.x;
                })
                        .attr("y1", function(d) {
                            return d.source.y;
                        })
                        .attr("x2", function(d) {
                            return d.target.x;
                        })
                        .attr("y2", function(d) {
                            return d.target.y;
                        });

                node.attr("transform", function(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
            });



            return true;
        }
    });
    return SchemaView;

});
