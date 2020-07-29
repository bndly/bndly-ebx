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
define(["cy/Observable"], function(Observable){
	var Value = Observable.extend({
		construct: function(config) {
			if (!config) {
                config = {};
            }
            this.callSuper(arguments, config);
			this.set(config.value);
		},
		set: function(newValue, silent) {
			var oldValue = this._v;
			this._v = newValue;
			if(!silent) {
				this.fireEvent("changed", newValue, oldValue, this);
			}
			return this;
		},
		get: function() {
			return this._v;
		}
	});
	return Value;
});
