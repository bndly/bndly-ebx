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
define(["cy/RestBeans", "cy/StringUtil", "cy/ui/container/ViewContainer", "cy/ui/ViewComponent", "cy/EntityTableDataProvider", "cy/ui/Text"], function(RestBeans, StringUtil, ViewContainer, ViewComponent, EntityTableDataProvider, Text) {
    var ResourceInvocationView = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.titleText = new Text({value:"Service Invocations", tag: "h1"});
            config.dataProvider = new EntityTableDataProvider({
                proto: new RestBeans.ResourceMethodInvocationStatusRestBean(),
                sortField: "startDate",
                sortAscending: false,
                pageSize: 100,
                listeners: {
                    load: {
                        fn: this.dataLoaded,
                        scope: this
                    }
                }
            });
            this.callSuper(arguments, config);
        },
        destroy: function() {
            this.invokeSuper(arguments);
			this.titleText.destroy();
        },
        renderTo: function(renderTarget) {
            this.renderTitle(renderTarget);
            this.chartHolder = $(renderTarget).append("<div></div>").children().last();
            this.invokeSuper(arguments);
            this.loadRecentData();
        },
        loadRecentData: function() {
            this.dataProvider.loadPage();
        },
        dataLoaded: function(items) {
            var mappedDuration = {},mappedInvocations = {},printedDates={},sampleCounts={};
            items.each(function(invc) {
                var t = invc.getStartDate().getTime();
                if(!sampleCounts[t]) {
                    sampleCounts[t] = 0;
                }
                sampleCounts[t]++;
                if(!mappedDuration[t]) {
                    mappedDuration[t] = 0;
                }
                if(!mappedInvocations[t]) {
                    mappedInvocations[t] = 0;
                }
                mappedDuration[t] += invc.getAverageDuration();
                mappedInvocations[t] += invc.getInvocationCount();
            }, this);
            
            var data = [], keys = [],maxDuration = 0,maxInvocations = 0, i=0;
            for(var t in mappedDuration) {
                var d = new Date(parseInt(t)), dur = mappedDuration[t]/sampleCounts[t];
                if(i%8 === 0){
                    printedDates[d.getTime()] = true;
                }
                i++;
                if(dur > maxDuration) {
                    maxDuration = dur;
                }
                if(mappedInvocations[t] > maxInvocations) {
                    maxInvocations = mappedInvocations[t];
                }
                
                keys.push(d);
                data.push({
                   date: d,
                   averageDuration: dur,
                   invocations: mappedInvocations[t]
                });
            }
            

            var margin = {top: 20, right: 20, bottom: 30, left: 40},
            width = 960 - margin.left - margin.right,
                    height = 500 - margin.top - margin.bottom;

            var x = d3.scale.ordinal()
                    .rangeRoundBands([0, width], .1);

            var y = d3.scale.linear()
                    .range([height, 0]);

            var xAxis = d3.svg.axis()
                    .scale(x)
                    .orient("bottom")
                    .tickFormat(function(d){
                        if(printedDates[d.getTime()]) {
                            return StringUtil.formatTime(d);
                        } else {
                            return "";
                        }
                    });

            var yAxis = d3.svg.axis()
                    .scale(y)
                    .orient("left")
                    .ticks(10);

            var svg = d3.select($(this.chartHolder).get(0)).append("svg")
                    .attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom)
                    .append("g")
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");



            x.domain(keys);
            y.domain([0, maxDuration]);

            svg.append("g")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0," + height + ")")
                    .call(xAxis);

            svg.append("g")
                    .attr("class", "y axis")
                    .call(yAxis)
                    .append("text")
                    .attr("transform", "rotate(-90)")
                    .attr("y", 6)
                    .attr("dy", ".71em")
                    .style("text-anchor", "end")
                    .text("Average duration");

            svg.selectAll(".bar")
                    .data(data)
                    .enter().append("rect")
                    .attr("class", "bar")
                    .attr("x", function(d) {
                        return x(d.date);
                    })
                    .attr("width", x.rangeBand())
                    .attr("y", function(d) {
                        return y(d.averageDuration);
                    })
                    .attr("height", function(d) {
                        return height - y(d.averageDuration);
                    });
        },
        renderTitle: function(renderTarget) {
            if (this.titleText) {
                this.titleText.renderTo(renderTarget);
            }
        }
    });
    return ResourceInvocationView;

});
