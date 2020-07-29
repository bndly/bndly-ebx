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
define(["cy/Observable", "cy/QuickSort"], function(Observable, QuickSort) {

    var Collection = Observable.extend({
        construct: function(config) {
            this.callSuper(arguments, config);
            this._rawItems = [];

            if (config) {
                if (Array.isArray(config)) {
                    config = {
                        items: config
                    };
                }
                if (Array.isArray(config.items)) {
                    this._rawItems = config.items;
                } else if (config.items) {
                    if (typeof (config.items.each) === "function") {
                        config.items.each(function(item, index) {
                            this._rawItems.push(item);
                        }, this);
                    }
                } else {
                    if (config instanceof Collection) {
                        config.each(function(item, index) {
                            this.add(item);
                        }, this);
                    }
                }
            }
        },
        add: function(item) {
            this.insert(item, this.size());
        },
        addAll: function(collection) {
            collection.each(function(toAdd) {
                this.add(toAdd);
            }, this);
        },
        remove: function(item) {
            var i = this.indexOf(item);
            return this.removeAtIndex(i);
        },
        removeAtIndex: function(i) {
            var removedItem = undefined;
            if (i > -1) {
                removedItem = this._rawItems.splice(i, 1)[0];
                this.fireEvent("removed", removedItem, i);
            }
            return removedItem;
        },
        replace: function(toRemove, toInsert) {
            var i = this.indexOf(toRemove);
            if (i > -1) {
                this.removeAtIndex(i);
                this.insert(toInsert, i);
                this.fireEvent("replaced", toRemove, toInsert);
            }
        },
        insert: function(item, index) {
            if (!this._rawItems[index]) {
                this._rawItems[index] = item;
            } else {
                var rightPart = this._rawItems.splice(index);
                this._rawItems.push(item);
                this._rawItems = this._rawItems.concat(rightPart);
            }
            this.fireEvent("inserted", item, index);
        },
        size: function() {
            return this._rawItems.length;
        },
        isEmpty: function() {
            return !(this.size() > 0);
        },
        each: function(handler, scope) {
            scope = !scope ? this : scope;
            for (var i in this._rawItems) {
                handler.call(scope, this._rawItems[i], i);
            }
        },
        indexOf: function(item) {
            for (var i in this._rawItems) {
                if (this._rawItems[i] === item) {
                    return i;
                }
            }
            return -1;
        },
        getItemAt: function(index) {
            return this._rawItems[index];
        },
        clear: function() {
            while (this.size() > 0) {
                this.removeAtIndex(this.size() - 1);
            }
        },
        sort: function(comparator) {
            // TODO: get a proper implementation
            this._rawItems.sort();
        }

    });
    return Collection;
});
