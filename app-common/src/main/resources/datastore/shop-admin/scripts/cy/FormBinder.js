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
    "cy/HTMLUtil",
    "cy/LabelFunctions",
    "cy/RestBeans",
    "cy/ui/container/Form",
    "cy/FieldSet",
    "cy/EntityCollection",
    "cy/form/Forms",
    "cy/CopyUtil",
    "cy/ui/input/InputTypeRegistry",
    "cy/ui/Text"
], function(
	ViewComponent,
	HTMLUtil,
	labelFunctions,
	RestBeans,
	Form,
	FieldSet,
	EntityCollection,
	Forms,
	CopyUtil,
	InputTypeRegistry,
	Text
) {
    var FormBinder = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if (!config.formBindings) {
                config.formBindings = {};
            }
            config.configured = {};

            if (config.sections) {
                for (var i in config.sections) {
                    var section = config.sections[i];
                    if (section) {
                        if (section.items) {
                            for (var j in section.items) {
                                var item = section.items[j];
                                if (item.member) {
                                    config.configured[item.member] = true;
                                    config.formBindings[item.member] = section.items[j];
                                } else if (item.key) {
                                    config.configured[item.key] = true;
                                    config.formBindings[item.key] = section.items[j];
                                    section.items[j].useKey = true;
                                }
                            }
                        }
                        if (section.member) {
                            config.configured[section.member] = true;
                        }
                    }
                }
            }

            this.callSuper(arguments, config);
        },
        buildForm: function(keyedInputs) {
            var items = [];
            keyedInputs = keyedInputs ? keyedInputs : {};
            this.renderMembersToForm(this.entity, items, keyedInputs);
            var form = new Form({
                items: items
            });
            return form;
        },
        renderFormElementsTo: function(renderTarget) {
            var _this = this, keyedInputs = {};
            if (_this.help) {
				var helpBlock = new Text({value: _this.help, cls: "help-block"});
				helpBlock.renderTo($(renderTarget));
            }
            renderMembersToForm(_this.entity, renderTarget, keyedInputs);
        },
        renderMemberToForm: function(entity, memberOrKey, formItems, keyedInputs) {
            if (this.ignoredMembers && this.ignoredMembers[memberOrKey]) {
                return false;
            }

            if (!this.formBindings[memberOrKey]) {
                this.formBindings[memberOrKey] = {};
            }
            var binding = this.formBindings[memberOrKey];
            var
                    input,
                    labelText = (!binding.labelText) ? memberOrKey : binding.labelText,
                    isMandatory = binding.mandatory;
            if (isMandatory) {
                labelText += "*";
            }

            var type = entity.memberType(memberOrKey);
            if (!type && binding.member) {
                console.warn("unsupported member '" + memberOrKey + "' in type '" + entity.clazzName() + "'");
                return false;
            }

			if (binding.constructorFn) {
                if (typeof (binding.constructorFn) === "string") {
                    binding.constructorFn = InputTypeRegistry.getInputConstructor(binding.constructorFn);
                }
			}

            if (binding.key) {
                console.log("creating keyed form item");
                if (binding.constructorFn) {
                    input = new binding.constructorFn(CopyUtil.copyConfig(binding.inputConfig, {
                        disabled: binding.generated,
                        entity: entity,
                        key: memberOrKey,
                        label: labelText
                    }));
                    formItems.push(input);
                }
            } else {
                var isSimpleType = type === "String" || type === "Number" || type === "Date" || type === "Boolean";
                var isCollection = entity.memberIsCollection(memberOrKey);
                var isReferenceable = false;

                var formElementId = entity.clazzName() + "_" + memberOrKey;
                binding.id = formElementId;

                var deRefed;
                if (!isSimpleType) {
                    if (!isCollection) {
                        var selectable = new RestBeans[type]();
                        deRefed = selectable.deRef();
                        isReferenceable = deRefed !== undefined && deRefed.clazzName() !== selectable.clazzName();
                        // isReferenceable = deRefed.extends("AbstractEntityReferenceRestBean");
                    }
                }

                if (isSimpleType) {
                    var inputType = "text", constructor;
                    if (binding.constructorFn) {
                        constructor = binding.constructorFn;
                    } else {
                        constructor = InputTypeRegistry.getInputConstructor("TextInput");
                        if (type === "Number") {
                            inputType = "number";
                        } else if (type === "Date") {
                            constructor = InputTypeRegistry.getInputConstructor("DateInput");
                        } else if (type === "Boolean") {
                            constructor = InputTypeRegistry.getInputConstructor("BooleanInput");
                        }

                    }
                    var cfg = CopyUtil.copyConfig(binding.inputConfig, {
                        type: inputType,
                        disabled: binding.generated,
                        entity: entity,
                        member: memberOrKey,
                        label: labelText
                    });
                    input = new constructor(cfg);
                    formItems.push(input);
                } else {
                    if (!isCollection) {
                        if (isReferenceable && !binding.embedded) {
                            var entryLabelFn = undefined;
                            if (binding.inputConfig)
                                entryLabelFn = binding.inputConfig.labelFn;
                            if (binding.typeahead) {
//                                console.log("found a typeahead");
                                var cfg = CopyUtil.copyConfig(binding.inputConfig, {
                                    label: labelText,
                                    entity: entity,
                                    member: memberOrKey,
                                    disabled: binding.generated,
                                    entryLabelFn: entryLabelFn
                                });
                                input = InputTypeRegistry.construct("TypeAheadInput", cfg);
                                formItems.push(input);

                            } else {
                                // since the object can be referenced, we can render a selection
                                var cfg = CopyUtil.copyConfig(binding.inputConfig, {
                                    label: labelText,
                                    entity: entity,
                                    member: memberOrKey,
                                    entryValueFn: function(entry) {
                                        if (!entry) {
                                            console.warn("getting value from empty entry");
                                            return undefined;
                                        }
                                        var entryLink = entry.hasLink("self");
                                        if (!entryLink) {
                                            return undefined;
                                        }
                                        var entryValue = entryLink.getHref();
                                        return entryValue;
                                    },
                                    entryLabelFn: entryLabelFn,
                                    items: new EntityCollection({
                                        proto: deRefed
                                    })
                                });
                                cfg.items.load();
                                if (!isMandatory) {
                                    cfg.allowEmpty = true;
                                }
                                input = InputTypeRegistry.construct("SelectInput", cfg);
                                formItems.push(input);
                            }

                        } else {
                            // embedded because the object can not be referenced or explicitly embedded
                            if (!binding.labelText) {
                                labelText = undefined;
                            }
                            var fsItems = [], fs = new FieldSet({
                                legend: labelText,
                                items: fsItems
                            });
                            formItems.push(fs);
                            var subCfg = binding.config;
                            if (!subCfg) {
                                subCfg = {};
                            }
                            subCfg.ignoredMembers = this.ignoredMembers;
                            var subEntity = this.entity.get(memberOrKey);
                            if (!subEntity) {
                                var t = type;
                                if (deRefed) {
                                    t = deRefed.clazzName();
                                }
                                subEntity = new RestBeans[t]();
                                this.entity.set(memberOrKey, subEntity, true);
                            }
                            subCfg.entity = subEntity;
                            var subBinder = new FormBinder(subCfg);
                            keyedInputs[memberOrKey] = {};
                            subBinder.renderMembersToForm(subEntity, fsItems, keyedInputs[memberOrKey]);
                        }
                    } else {
                        var entryClazzName = new RestBeans[type]().deRef().clazzName();
                        var input = InputTypeRegistry.construct("CollectionInput", {
                            label: labelText,
                            disabled: binding.generated,
                            entryFactoryFn: function() {
                                return new RestBeans[entryClazzName]();
                            },
                            entity: entity,
                            member: memberOrKey
                        });
                        formItems.push(input);
                    }
                }
            }

            if (input) {
                keyedInputs[memberOrKey] = input;
                if (binding.init) {
                    binding.init(input, entity);
                }
            }
        },
        renderMembersToForm: function(entity, formItems, keyedInputs) {
            if (!this.keyedInputs) {
                this.keyedInputs = keyedInputs;
            }
            if (this.sections) {
                for (var i in this.sections) {
                    var section = this.sections[i];
                    if (section) {
                        if (section.member) {
                            var cfg = {};
                            for (var k in section) {
                                if (k !== "member") {
                                    cfg[k] = section[k];
                                }
                            }

                            var subEntity = this.entity.get(section.member);
                            var type = this.entity.memberType(section.member);
                            if (!type) {
                                throw new Error("could not determine type of member '" + section.member + "' in " + entity.clazzName());
                            }
                            if (!subEntity) {
                                subEntity = new RestBeans[type]();
                                this.entity.set(section.member, subEntity, true);
                            }
                            var subBinder = new FormBinder({
                                entity: subEntity,
                                sections: [
                                    cfg
                                ],
                                ignoredMembers: this.ignoredMembers
                            });
                            keyedInputs[section.member] = {};
                            subBinder.renderMembersToForm(subEntity, formItems, keyedInputs[section.member]);
                        } else {
                            var fsItems = [], fs = new FieldSet({
                                legend: section.name,
                                labelPosition: section.labelPosition,
                                items: fsItems
                            });
                            formItems.push(fs);

                            if (section.autoRenderMembers) {
                                // TODO: this is usefull when keeping stuff unconfigured
                                entity.members().each(function(member) {
                                    if (this.ignoredMembers && this.ignoredMembers[member]) {
                                        // console.log(member+" is ignored.")
                                        return;
                                    }
                                    if (!this.configured[member]) {
                                        this.renderMemberToForm(entity, member, fsItems, keyedInputs);
                                    } else {
                                        // console.log(member+" is configured.")
                                    }
                                }, this);
                            } else {
                                if (section.items) {
                                    for (var j in section.items) {
                                        var item = section.items[j];
                                        if (item.member || item.key) {
                                            this.renderMemberToForm(entity, item.member ? item.member : item.key, fsItems, keyedInputs);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                entity.members().each(function(member) {
                    if (this.ignoredMembers && this.ignoredMembers[member]) {
                        return;
                    }
                    this.renderMemberToForm(entity, member, formItems, keyedInputs);
                }, this);
            }
        },
        renderFormActions: function(renderTarget) {
            if (this.actions) {
                this.actionButtonGroup = $(renderTarget).append("<div class=\"btn-group\">").children().last();
                this.actionButtonsByEvent = {};
                for (var i in this.actions) {
                    var action = this.actions[i];
                    if (action.event) {
                        var label = action.labelText;
                        if (!label) {
                            label = action.event;
                        }
                        var btn = $(this.actionButtonGroup).append("<button class=\"btn\"></button>").children().last();
                        if (action.icon) {
                            var icon = $(btn).append("<i></i>").children().last();
							$(icon).attr("class", action.icon);
                            label = " " + label;
                        }
                        $(btn).append(label);
                        var _this = this;

                        var createCB = function(event, _this, btn) {
                            return function() {
                                _this.fireEvent(event, _this, btn);
                                return false;
                            };
                        };

                        $(btn).click(createCB(action.event, _this, btn));
                        this.actionButtonsByEvent[action.event] = btn;
                    }
                }
            }
        },
        renderTo: function(renderTarget, keyedInputs) {
			if(!keyedInputs) {
				keyedInputs = this.keyedInputs;
			}
            this.buildForm(keyedInputs).renderTo(renderTarget);
        }
    });
    return FormBinder;
});
