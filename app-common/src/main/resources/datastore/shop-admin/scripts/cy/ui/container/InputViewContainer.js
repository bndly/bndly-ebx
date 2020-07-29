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
define([
	"cy/ui/ViewComponent", 
	"cy/ui/input/InputViewComponent", 
	"cy/Collection", 
	"cy/ui/container/ViewContainer", 
	"cy/ui/input/Button", 
	"cy/ui/Link",
	"cy/ui/Text"
], function(
	ViewComponent, 
	InputViewComponent, 
	Collection, 
	ViewContainer, 
	Button, 
	Link,
	Text
) {
    var InputViewContainer = ViewContainer.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }

            if(!config.labelPosition) {
                config.labelPosition = "left";
            }
            config.componentConfigs = new Collection();

            this.callSuper(arguments, config);
        },
        destroy: function() {
            this.invokeSuper(arguments);
            this.componentConfigs.each(function(componentConfig) {
                if (componentConfig.controlGroup) {
                    $(componentConfig.controlGroup).remove();
                }
                if (componentConfig.component instanceof ViewComponent) {
                    componentConfig.component.destroy();
                }
            }, this);
        },
        renderTo: function(renderTarget) {
            if (!this.element) {
                this.element = $(renderTarget);
            }
            this.invokeSuper(arguments);
            if(this.hidden) {
                this.hide();
            }
        },
        renderItem: function(component) {
            if (this.element) {
                var isInputComponent = component instanceof InputViewComponent;
                var isButton = component instanceof Button;
                var isLink = component instanceof Link;
                if (isInputComponent || isButton || isLink || (component instanceof InputViewContainer)) {
                    var componentConfig = {};
                    componentConfig.component = component;
                    if (isInputComponent) {
                        if(this.labelPosition === "left") {
                            componentConfig.controlGroup = $(this.element).append("<div class=\"control-group\"></div>").children().last();
                            componentConfig.labelText = new Text({value: component.getLabel()});
							componentConfig.label = $(componentConfig.controlGroup).append("<label class=\"control-label\"></label>").children().last();
							$(componentConfig.label).attr("for", component.getName());
							componentConfig.labelText.renderTo($(componentConfig.label));
                            componentConfig.controls = $(componentConfig.controlGroup).append("<div class=\"controls\"></div>").children().last();
                            component.renderTo($(componentConfig.controls));
                        } else {
                            componentConfig.controlGroup = $(this.element).append("<div></div>").children().last();
							componentConfig.labelText = new Text({value: component.getLabel()});
                            componentConfig.label = $(componentConfig.controlGroup).append("<label></label>").children().last();
							$(componentConfig.label).attr("for", component.getName());
							componentConfig.labelText.renderTo($(componentConfig.label));
                            component.renderTo($(componentConfig.controlGroup));
                        }
                    } else {
                        component.renderTo($(this.element));
                    }

                    this.componentConfigs.add(componentConfig);
                } else {
                    console.warn("ignoring non-input component in inputViewContainer");
                }
            }
        },
        removeItem: function(component) {
            component.destroy();
            this.componentConfigs.each(function(c) {
                if (c.component === component) {
                    if (c.controlGroup) {
                        c.controlGroup.remove();
                    }
                    if (c.component instanceof ViewComponent) {
                        c.component.destroy();
                    }
                }
            }, this);
        },
        hide:function(){
            this.items.each(function(item){
                if(item.hide) {
                    item.hide();
                }
            }, this);
            
            this.componentConfigs.each(function(componentConfig){
                if(componentConfig.controlGroup) {
                    $(componentConfig.controlGroup).hide();
                }
            }, this);
        },
        show:function(){
            this.items.each(function(item){
                if(item.show) {
                    item.show();
                }
            }, this);
            
            this.componentConfigs.each(function(componentConfig){
                if(componentConfig.controlGroup) {
                    $(componentConfig.controlGroup).show();
                }
            }, this);
        }
    });
    return InputViewContainer;
});
