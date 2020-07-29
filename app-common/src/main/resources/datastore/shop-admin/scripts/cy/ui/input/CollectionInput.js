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
define(["cy/ui/input/InputViewComponent","cy/ui/input/Button","cy/Collection", "cy/LabelFunctions", "cy/ui/container/ViewContainer", "cy/FormBinder", "cy/form/Forms", "cy/ui/input/InputTypeRegistry", "cy/ui/Text"], function(InputViewComponent, Button, Collection, LabelFunctions, ViewContainer, FormBinder, Forms, InputTypeRegistry, Text) {
	var CollectionInput = InputViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
                        config.addButton = new Button({
                            label: "add",
                            listeners: {
                                clicked: {
                                    fn: this.addNewEntry,
                                    scope: this
                                }
                            }
                        });
                        if(!config.entryLabelFn) {
                            config.entryLabelFn = function(entry){ 
                                var label = LabelFunctions(entry); 
                                if(!label) {
                                    label = "&nbsp;";
                                }
                                return label;
                            };
                        }
                        if(!config.entryFactoryFn) {
                            console.error("missing an entryFactoryFn for CollectionInput");
                        }
                        if(!config.entryFormItemsBuilderFn) {
                            config.entryFormItemsBuilderFn = function(entry, items) {
                                new FormBinder({
                                    entity: entry,
                                    sections: Forms[entry.clazzName()]
                                }).renderMembersToForm(entry, items, {});
                            };
                        }
                        config.bulletBindings = [];
			this.callSuper(arguments, config);
                        
                        var val;
                        if(this.member && this.entity) {
                            val = this.getBoundValue();
                        } else {
                            if(!config.value) {
                                val = new Collection();
                            } else {
                                val = config.value;
                            }
                        }
                        if(!val) {
                            val = new Collection();
                            this.setBoundValue(val);
                        }
                        this.value = val;
                        this.listen(val, "inserted", this.entryAdded, this);
			this.listen(val, "removed", this.entryRemoved, this);
			
		},
		destroy: function() {
			arguments.callee.$parent.destroy.call(this);
		},
                buildNewEntry: function(){
                    if(this.entryFactoryFn) {
                        return this.entryFactoryFn(this.entity);
                    }
                    return undefined;
                },
                addNewEntry: function(button) {
                    this.getValue().add(this.buildNewEntry());
                },
                entryRemoved: function(entry) {
                    var binding = this.getBulletBindingForEntry(entry);
                    if(binding) {
                        $(binding.bullet).remove();
                    } else {
                        console.warn("could not find bullet binding for entry");
                    }
                    if(this.value.isEmpty()) {
                        $(this.wrapper).hide();
                    }
                },
                entryAdded: function(entry) {
                    this.renderEntry(entry);
                },
                getBulletBindingForEntry: function(entry) {
                    for(var i in this.bulletBindings) {
                        if(this.bulletBindings[i].entry === entry) {
                            return this.bulletBindings[i];
                        };
                    }
                    return undefined;
                },
                renderEntry: function(entry) {
                    if(this.wrapper) {
                        $(this.wrapper).show();
                        var itemBullet = $(this.wrapper).append("<li></li>").children().last();
                        var itemAnchor = $(itemBullet).append("<a href=\"#\"></a>").children().last();
						var itemLabel = new Text({value: this.entryLabelFn(entry)});
						itemLabel.renderTo(itemAnchor);
                        this.listen(entry, "changed", function(changedEntry){
                            itemLabel.value.set(this.entryLabelFn(changedEntry));
                        }, this);
                        var deleteButton = $(itemAnchor).append("<i class=\"icon-remove pull-right\"></i>").children().last();
                        var _this = this;
                        $(itemAnchor).click(function(){
                            _this.editEntry(entry);
                            return false;
                        });
                        $(deleteButton).click(function(){
                            var index = _this.getValue().indexOf(entry);
                            _this.getValue().removeAtIndex(index);
                            return false;
                        });
                        this.bulletBindings.push({
                            bullet: itemBullet,
                            anchor: itemAnchor,
                            entry: entry
                        });
                        
                    }
                },
                editEntry: function(entry) {
                    var items = [];
                    this.entryFormItemsBuilderFn(entry, items);
                    items.push(new Button({
                        label: "back",
                        listeners: {
                            clicked: {
                                fn: this.entryEdited,
                                scope: this
                            }
                        }
                    }));
                    this.vc = new ViewContainer({
                        items: items
                    });
                    this.vc.renderTo(this.rightDiv);
                    this.slideListToLeft();
                },
                entryEdited: function(){
                    this.vc.destroy();
                    this.slideListToRight();
                },
                slideListToLeft: function(){
                    console.log("should slide left");
                    $(this.slideWrapper).animate({
                        left: -($(this.slideWrapper).width()/2)
                    });
                },
                slideListToRight: function(){
                    console.log("should slide right");
                    $(this.slideWrapper).animate({
                        left: 0
                    });
                },
		renderTo: function(renderTarget) {
                    this.globalWrapper = $(renderTarget).append("<div></div>").children().last();
                    $(this.globalWrapper).css({
                        overflow: "hidden"
                    });
                    this.slideWrapper = $(this.globalWrapper).append("<div></div>").children().last();
                    $(this.slideWrapper).css({
                        position: "relative",
                        left: 0,
                        width: "200%"
                    });
                    this.leftDiv = $(this.slideWrapper).append("<div></div>").children().last();
                    this.wrapper = $(this.leftDiv).append("<ul class=\"nav nav-tabs nav-stacked\">").children().last();
                    
                    if(this.value.isEmpty()) {
                        $(this.wrapper).hide();
                    }
                    $(this.leftDiv).css({
                        float: "left",
                        width: "50%"
                    });
                    this.rightDiv = $(this.slideWrapper).append("<div></div>").children().last();
                    $(this.rightDiv).css({
                        width: "50%",
                        float: "left"
                    });
                    this.getValue().each(function(entry){
                        this.renderEntry(entry);
                    }, this);
                    this.addButton.renderTo(this.leftDiv);
		},
                hide: function(){
                    if(this.leftDiv) {
                        $(this.leftDiv).hide();
                    }
                    this.addButton.hide();
                },
                show: function(){
                    if(this.leftDiv) {
                        $(this.leftDiv).show();
                    }
                    this.addButton.show();
                },
		getValue: function() {
                    return this.value;
		},
		setValue: function(value) {
                    this.value = value;
		}
	});
        InputTypeRegistry.register("CollectionInput", CollectionInput);
	return CollectionInput;
});
