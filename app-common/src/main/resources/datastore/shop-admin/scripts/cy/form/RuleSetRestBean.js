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
define(["cy/RestBeans", "cy/Collection"], function(RestBeans, Collection) {
    return [
        {
            items: [{
                    member: "name",
                    labelText: "Name",
                    mandatory: true,
                    constructorFn: "SelectInput",
                    inputConfig: {
                        entryValueFn: function(e) {
                            return e;
                        },
                        entryLabelFn: function(e) {
                            return e;
                        }
                    },
                    init: function(instance, entity) {
                        RestBeans.root.follow({
                            rel: "schema",
                            cb: function(schema) {
                                var c = new Collection();
                                schema.getTypes().each(function(type) {
                                    c.add(type.getName()+"RestBean");
                                }, this);
                                c.sort();
                                c.each(function(typeName) {
                                    instance.items.add(typeName);
                                }, this);
                            },
                            scope: this
                        });
                    }
                }]
        }
    ];

});
