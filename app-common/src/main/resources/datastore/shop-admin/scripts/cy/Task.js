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
define(["cy/Observable"], function(Observable) {
    var Task = Observable.extend({
        construct: function(config) {
            if(!config) {
                config = {};
            }
            this.callSuper(arguments, config);
        },
        setHandler: function(fn, scope) {
            this.fn = fn;
            this.scope = scope;
        },
        _execute: function(){
            if(this.scope) {
                this.fn.call(this.scope);
            } else {
                this.fn();
            }
        },
        cancel: function(){
            this.cancelled = true;
        },
        delay: function(millis){
            var _this = this;
            setTimeout(function(){
                if(!_this.cancelled) {
                    _this._execute();
                } else {
                    _this.cancelled = false;
                }
            }, millis);
        }
    });
    return Task;
});
