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
define(["cy/Observable", "cy/Task", "cy/Collection"], function(Observable, Task, Collection) {
    var TaskChain = Observable.extend({
        construct: function(config) {
            if(!config) {
                config = {};
            }
            if(!config.tasks) {
                config.tasks = new Collection();
            } else {
                config.tasks = new Collection(config.tasks);
            }
            this.callSuper(arguments, config);
        },
        addHandler: function(fn, scope) {
            this.tasks.add(new Task({
                fn: fn,
                scope: scope
            }));
        },
        execute: function() {
            this.tasks.each(function(task){
                task._execute();
            }, this);
        }
    });
    return TaskChain;
});
