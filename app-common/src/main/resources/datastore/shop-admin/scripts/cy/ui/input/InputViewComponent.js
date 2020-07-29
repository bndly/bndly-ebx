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
define(["cy/ui/ViewComponent"], function(ViewComponent) {
    var InputViewComponent = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            this.callSuper(arguments, config);

            if (config.entity && config.member) {
                this.bindTo(config.entity, config.member);
            }
        },
        destroy: function() {
            arguments.callee.$parent.destroy.call(this);
        },
        bindTo: function(entity, member) {
            if (this.entity) {
                this.entity.removeListener("changed", this.entityChanged, this);
            }

            this.entity = entity;
            this.member = member;

            // attach change listeners to entity
            this.listen(entity, "changed", this.entityChanged, this);
        },
        // react on entity changes + getter and setter
        getBoundValue: function() {
            if (this.entity && this.member) {
                return this.entity.get(this.member);
            }
            return undefined;
        },
        setBoundValue: function(value) {
            if (this.entity && this.member) {
                return this.entity.set(this.member, value);
            }
        },
        entityChanged: function(entity, member, newValue, oldValue) {
            if (member === this.member) {
                this.setValue(newValue);
            }
        },
        // react on input changes + getter and setter
        inputChanged: function() {
            if (this.entity && this.member) {
                this.entity.set(this.member, this.getValue());
            }
            this.fireEvent("changed", this, this.getValue());
        },
        getValue: function() {
            throw new Error("you should override this method.");
        },
        setValue: function(value) {
            throw new Error("you should override this method.");
        },
        getName: function() {
            return this.name;
        },
        getLabel: function() {
            return this.label;
        }
    });
    return InputViewComponent;
});
