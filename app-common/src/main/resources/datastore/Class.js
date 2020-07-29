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
	var Class = function() {};
	Class.extend = function(subClassName, subClassDefinition) {
		if(typeof(subClassName) === "object") {
			subClassDefinition = subClassName;
			subClassName = "ANONYMOUS";
		}
		var subClass = function() {
			if (arguments[0] !== Class) {
				// call a custom constructor named "construct"
				if(!this.construct) {
					console.error("executing constructor in wrong prototype");
				}
				this.construct.apply(this, arguments);
			}
		};
		// create a new Class prototype
		// this is our subclass stub
		var proto = new this(Class);
		Object.defineProperty(proto, 'CLASSNAME', {
			get: function() {
				return subClassName;
			},
			set: function(name) {
			}
		});

		// if we call extend from a Class object, the Class objects prototype will be the superclass
		var superClass = this.prototype;

		// for all functions declared for the subclass
		for (var n in subClassDefinition) {
			var item = subClassDefinition[n];
			if (typeof(item) === "function") {
				// set the $parent attribute on the function object to the parent superclass
				// this will allow to get from one superclass to another. using "this.$parent" inside a function will lead to invocation circles.
				item.$parent = superClass;
				item.$name = n;
			}
			proto[n] = item;
		}
		// a convenience method to invoke super function implementations
		proto["invokeSuper"] = function(arg) {
			var name = arg.callee.$name;
			if(name) {
				var p = arg.callee.$parent[name];
				if(p) {
					p.apply(this, arg);
				} else {
					console.warn("could not invoke super implementation of '"+name+"', because there was function with that name in the super type.");
				}
			} else {
				console.error("could not invoke super, because function was not passed through Class prototype.");
			}
		};
		proto["callSuper"] = function() {
			var arg = arguments[0], // arguments of the invocation block that calls "callSuper"
				args = []; // the actual arguments

			for(var i=1; i < arguments.length; i++){
				args.push(arguments[i]);
			}
			var name = arg.callee.$name;
			if(name) {
				var p = arg.callee.$parent[name];
				if(p) {
					p.apply(this, args);
				} else {
					console.warn("could not invoke super implementation of '"+name+"', because there was function with that name in the super type.");
				}
			} else {
				console.error("could not invoke super, because function was not passed through Class prototype.");
			}
		};

		// now the created subclass gets his customized prototype
		// remember: the customizing pimped each function with a reference to the super class
		subClass.prototype = proto;

		subClass.extend = this.extend;
		return subClass;
	};
	return Class;
});
