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
define(["cy/WebSocketLogger", "cy/ui/container/ViewContainer", "cy/ui/ViewComponent"], function(WebSocketLogger, ViewContainer, ViewComponent) {
    var EventView = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.titleText = "Events";
            config.container = new ViewContainer({
                items: [
                ]
            });
            this.callSuper(arguments, config);
			WebSocketLogger.instance.addListener("onmessage", this.onmessage, this);
        },
        destroy: function() {
			WebSocketLogger.instance.removeListener("onmessage", this.onmessage, this);
			this.container.destroy();
            this.invokeSuper(arguments);
			
        },
        renderTo: function(renderTarget) {
			this.renderTarget=renderTarget;
			this.pre = $(renderTarget).append("<pre></pre>").children().last();
			this.container.renderTo(renderTarget);
            this.invokeSuper(arguments);
        },
		onmessage: function(ws, event) {
			if(this.pre) {
				var d, i, entity, message, incrementBadge = 0, _this = this;
				try {
					d = JSON.parse(event.data);
				}catch (error) {
					// fail silently
					return;
				}
				if(d.event) {
					if(d.event.name) {
						if(d.data) {
							if(d.event.name === "entityModification" && d.data.entities && d.data.entities.length > 0) {
								for(i=0;i<d.data.entities.length; i++) {
									entity = d.data.entities[i];
									message = d.event.name+": "+entity.modification+": "+entity.type+" (id="+entity.id+")";
									this.pre.prepend(message+"\n");
									incrementBadge++;
								}
							} else if(d.event.name === "flushed") {
								if(d.data.path) {
									message = "flushed cache: "+d.data.path+" "+(d.data.recursive ? "recursive" : "non-recursive");
								} else if(d.data.complete) {
									message = "flushed cache: entire cache flushed";
								}
								if(message) {
									this.pre.prepend(message+"\n");
									incrementBadge++;
								}
							}
						}
					}
				}
				if(incrementBadge) {
					if(!this.totalBadgeValue) {
						this.totalBadgeValue = incrementBadge;
					} else {
						this.totalBadgeValue += incrementBadge;
					}
					this.fireEvent("badge", this, this.totalBadgeValue, function() {
						_this.totalBadgeValue = undefined;
					});
				}
			}
		}
    });
    return EventView;

});
