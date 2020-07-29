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
define(["cy/ui/input/CollectionInput", "cy/ui/input/InputTypeRegistry", "cy/RestBeans", "cy/EntityCollection"], function(CollectionInput, InputTypeRegistry, RestBeans, EntityCollection) {
    var InverseCollectionInput = CollectionInput.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if (!config.entryFactoryFn) {
                if (!config.childType) {
                    throw new Error("missing childType for generated entryFactoryFn");
                }
                if (!config.childProperty) {
                    throw new Error("missing childType for generated entryFactoryFn");
                }
                config.entryFactoryFn = function(entity) {
                    var t = new RestBeans[config.childType]();
                    t.set(config.childProperty, entity);
                    return t;
                };
            }
            if (!config.value) {
                config.value = new EntityCollection();
            }
            this.callSuper(arguments, config);

            this.initCollection();
        },
        destroy: function() {
            arguments.callee.$parent.destroy.call(this);
        },
        initCollection: function() {
            this.getValue().clear();

            // make sure that there is consistency
            this.getValue().addListener("inserted", function(item) {
                if (this.childProperty) {
                    item.set(this.childProperty, this.entity);
                }
            }, this);

            // load entries if the parent is already persisted
            if (this.entity.hasLink("self")) {
                this.loadChildren();
            }

            this.entity.addListener("persisted", function(persistedEntry) {
                this.getValue().each(function(item) {
                    item.persist();
                }, this);
            }, this);
        },
        loadChildren: function() {
            if (this.childType) {
                var proto = new RestBeans[this.childType]();
                if (this.childProperty) {
                    proto.set(this.childProperty, this.entity);
                }
                this.getValue().setProto(proto).load();
            }
        }
    });
    InputTypeRegistry.register("InverseCollectionInput", InverseCollectionInput);
    return InverseCollectionInput;
});
