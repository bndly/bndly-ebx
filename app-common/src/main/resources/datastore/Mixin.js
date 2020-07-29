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
define(function(){
	var Mixin = function(template) {
		this.template = template;
	};
	Mixin.prototype.mixInto = function(receiver, asPrivate) {
		var	methodName,
			privateProperty = asPrivate ? Object.create(null) : null,
			template = this.template
		;

		for (methodName in template) {
			if (template.hasOwnProperty(methodName)) {
				receiver[methodName] = template[methodName].bind(asPrivate ? privateProperty : receiver);
			};
		};
		return receiver;
	};
	return Mixin;
});
