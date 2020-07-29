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
    var EventWebSocket = Observable.extend({
        construct: function(config) {
            if(!config) {
                config = {};
            }
            this.callSuper(arguments, config);
			if(EventWebSocket.instance) {
				throw new Error("there is already a websocket");
			}
        },
        connect: function(host){
			var _this = this;
			this.ws = new WebSocket(host);
			this.ws.onopen = function(event){
				_this.onopen(event);
			};
			this.ws.onmessage = function(event){
				_this.onmessage(event);
			};
			this.ws.onerror = function(event){
				_this.onerror(event);
			};
			this.ws.onclose = function(event){
				_this.onclose(event);
			};
        },
		onopen: function(event) {
			this.fireEvent("onopen", this, event);
			this.sendMessage("hello from shop admin.");
		},
		onmessage: function(event) {
			this.fireEvent("onmessage", this, event);
		},
		onerror: function(event) {
			this.fireEvent("onerror", this, event);
		},
		onclose: function(event) {
			this.fireEvent("onclose", this, event);
		},
		sendMessage: function(value) {
			if (this.ws.readyState !== WebSocket.OPEN) {
				console.warn("could not send message, because ready state was not open.");
				return;
			}
			this.ws.send(value);
		}
    });
	EventWebSocket.instance = new EventWebSocket();
    return EventWebSocket;
});
