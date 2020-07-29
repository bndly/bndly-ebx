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
define(["cy/Class"], function(Class) {
	var Observable = Class.extend({
		construct: function(config) {
			if (!this) {
				throw new Error("can not apply config to " + this);
			}

			this.initialConfig = config;
			this._listenersOnOtherComponents = [];
			if (config) {
				for (var k in config) {
					this[k] = config[k];
				}

				this.listeners = {};
				if (config.listeners) {
					for (var i in config.listeners) {
						var l = config.listeners[i];
						if (typeof(l) === "function") {
							this.addListener(i, l);
						} else if (typeof(l) === "object") {
							this.addListener(i, l.fn, l.scope);
						}
					}
				}
			}
		},
		addListener: function(eventName, callback, scope, maxInvocations) {
			if (!this.listeners) {
				this.listeners = {};
			}

			if (!this.listeners[eventName]) {
				this.listeners[eventName] = [];
			} else if (this.listeners[eventName] instanceof Array) {
				// do nothing
			} else if (this.listeners[eventName] instanceof Object) {
				this.listeners[eventName] = [this.listeners[eventName]];
			} else if (this.listeners[eventName] instanceof Function) {
				this.listeners[eventName] = [{
					fn: this.listeners[eventName]
				}];
			}

			var listener = {
				fn: callback,
				scope: scope
			};
			this.listeners[eventName].push(listener);
			if(maxInvocations) {
				listener.invocations = maxInvocations
			}

		},
		addSingleInvocationListener: function(eventName, callback, scope) {
			this.addListener(eventName, callback, scope, 1);
		},
		removeListener: function(eventName, callback, scope) {
			if (this.listeners) {
				var eventListeners = this.listeners[eventName];
				if (eventListeners) {
					if (eventListeners instanceof Array) {
						for (var i in eventListeners) {
							var listener = eventListeners[i];
							if (listener.fn === callback) {
                                                            if((scope && listener.scope === scope) || !scope) {
                                                                eventListeners = eventListeners.splice(i, i);
								this.listeners[eventName] = eventListeners;
								break;
                                                            }
							}
						}
					} else if (eventListeners instanceof Object) {
						if (eventListeners.fn === callback) {
							this.listeners[eventName] = undefined;
						}
					} else if (eventListeners instanceof Function) {
						if (eventListeners === callback) {
							this.listeners[eventName] = undefined;
						}
					}
				}
			}
		},
		fireEvent: function() {
			if (arguments.length < 1) {
				throw new Error("can not invoke fireEvent without eventName parameter");
			}
			var eventName = arguments[0];
			if (this.listeners) {
				var toInvoke = this.listeners[eventName];
				if (toInvoke) {
					var allHandlers = [];
					if (toInvoke instanceof Array) {
						allHandlers = toInvoke;
					} else if (toInvoke instanceof Object) {
						allHandlers.push(toInvoke);
					} else if (toInvoke instanceof Function) {
						allHandlers.push({
							fn: toInvoke
						});
					}

					for (var i in allHandlers) {
						toInvoke = allHandlers[i];
						if (toInvoke.fn instanceof Function) {
							var s = toInvoke.scope;
							if (!s) {
								s = this;
							}
							var args = [];
							for (var a in arguments) {
								args.push(arguments[a]);
							}
							if(toInvoke.invocations === undefined || toInvoke.invocations > 0) {
								toInvoke.fn.apply(s, args.slice(1));
								if(toInvoke.invocations !== undefined) {
									toInvoke.invocations -= 1;
									// maybe some kind of clean up would be wise...
								}
							}
						}
					}
				}
			}
		},
		listen: function(observable, event, callback, scope) {
                    if(!observable) {
                        console.warn("trying to attach listener to null or undefined");
                        return false;
                    }
			observable.addListener(event, callback, scope);
			this._listenersOnOtherComponents.push({
				observable: observable, 
				event: event, 
				callback: callback, 
				scope: scope
			});
		},
		destroy: function() {
			this.fireEvent("beforeDestroy", this);
			for(var i in this._listenersOnOtherComponents) {
				var descriptor =  this._listenersOnOtherComponents[i];
				descriptor.observable.removeListener(descriptor.event, descriptor.callback);
			}
		}
	});
	return Observable;
});
