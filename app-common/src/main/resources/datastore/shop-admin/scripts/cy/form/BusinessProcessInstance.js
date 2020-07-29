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
define(["cy/RestBeans", "cy/EntityCollection"], function(RestBeans, EntityCollection) {
    return [
        {
            items: [{
                    member: "id",
                    labelText: "ID",
                    generated: true
                }, {
                    member: "startTime",
                    labelText: "Start",
                    generated: true
                }, {
                    member: "endTime",
                    labelText: "End",
                    generated: true
                }, {
                    key: "processName",
                    labelText: "Process name",
                    constructorFn: "SelectInput",
                    mandatory: true,
                    inputConfig: {
                        eager: false,
                        items: new EntityCollection({
                            proto: new RestBeans["BusinessProcessDefinition"]()
                        }),
                        entryValueFn: function(processDefinition) {
                            return processDefinition.getName();
                        },
                        entryLabelFn: function(processDefinition) {
                            return processDefinition.getName();
                        }
                    },
                    init: function(instance, entry) {
                        instance.addListener("changed", function(input, processDefinition){
                            entry.setProcessName(processDefinition.getName());
                        }, this);
                        instance.items.addListener("allLoaded", function(items){
                            if(!entry.getProcessName()) {
                                items.each(function(def){
                                    if(!entry.getProcessName()) {
                                        entry.setProcessName(def.getName());
                                        instance.setValue(def);
                                    } else {
                                        return;
                                    }
                                }, this);
                            } else {
                                items.each(function(def){
                                    if(def.getName() === entry.getProcessName()) {
                                        instance.setValue(def);
                                    } else {
                                        return;
                                    }
                                }, this);
                            }
                        }, this);
                        instance.items.load();
                    }
                }]
        }
    ];

});
