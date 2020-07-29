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
    "cy/TableConfigurations",
    "cy/TableBinder",
    "cy/RestBeans",
    "cy/FormBinder",
    "cy/RelationFormBinder",
    "cy/Browser",
    "cy/ui/container/Modal",
    "cy/form/Forms",
    "cy/CopyUtil",
    "cy/relations/Relations",
    "cy/ui/Text",
	"cy/ui/Badge"
], function(
        ViewComponent,
        tableConfigurations,
        TableBinder,
        RestBeans,
        FormBinder,
        RelationFormBinder,
        Browser,
        Modal,
        Forms,
        CopyUtil,
        Relations,
		Text,
		Badge
) {
    var Navigation = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            this.callSuper(arguments, config);
        },
        renderTo: function(renderTarget) {
            this.list = $(renderTarget)
                    .append("<ul class=\"nav nav-list\">")
                    .children()
                    .last();
            if (this.sections) {
                for (var i in this.sections) {
                    var section = this.sections[i], sectionItem, _this = this;
                    if (section.items) {
						section.element = new Text({value: section.name, tag: "li", cls: "nav-header"});
						section.element.renderTo($(this.list));
                        for (var j in section.items) {
                            sectionItem = section.items[j];
                            sectionItem.element = $(this.list)
                                    .append("<li class=\"nav-bar-item\"></li>")
                                    .children()
                                    .last();
							sectionItem.elementLink = new Text({value: sectionItem.labelText, tag: "a"});
							sectionItem.elementLink.renderTo(sectionItem.element);
							$(sectionItem.elementLink.element).attr("href", "#");
                            $(sectionItem.element).click(this.createSectionItemClickedCallback(this, sectionItem));
							if(sectionItem.view instanceof ViewComponent) {
								sectionItem.badge = new Badge({hidden: true, cls: "pull-right"});
								sectionItem.badge.renderTo(sectionItem.elementLink.element);
								sectionItem.view.addListener("badge", this.createBadgeValueChangedCallback(sectionItem));
							}
                        }
                    }
                }
            }
        },
		createBadgeValueChangedCallback: function(sectionItem) {
			return function(view, badgeValue, resetCallback) {
				if(sectionItem.badge) {
					if(this.activeItem !== sectionItem) {
						sectionItem.badge.value.set(badgeValue);						
						sectionItem.badge.show();
					}
					sectionItem.badgeReset = resetCallback;
				}
			};
		},
        createSectionItemClickedCallback: function(_this, sectionItem) {
            return function() {
                _this.sectionItemClicked(sectionItem);
            };
        },
        sectionItemClicked: function(sectionItem) {
            if (this.activeItem) {
                this.activeItem.element.removeClass("active");
				this.activeItem.detached = $(this.activeItem.renderTarget).detach();
            }
            sectionItem.element.addClass("active");
            this.activeItem = sectionItem;
			if(this.activeItem.badge) {
				this.activeItem.badge.hide();
			}
			if(this.activeItem.badgeReset) {
				this.activeItem.badgeReset();
				this.activeItem.badgeReset = undefined;
			}
			if(!sectionItem.renderTarget) {
				sectionItem.renderTarget = this.contentRenderTarget.append("<div></div>").children().last();
			}
			if(sectionItem.detached) {
				sectionItem.detached.appendTo(this.contentRenderTarget);
			} else if (sectionItem.view) {
                if (sectionItem.view instanceof ViewComponent) {
					if(!sectionItem.rendered) {
	                    sectionItem.view.renderTo(sectionItem.renderTarget);
						sectionItem.rendered = true;
					}
                }
            } else if (sectionItem.entityType) {
                var browser = this.buildBrowser(sectionItem);
                browser.renderTo(sectionItem.renderTarget);
            }

            return false;
        },
        buildTableColumnsForSection: function(sectionItem) {
            var cols = [];
            var configuredColumns = tableConfigurations[sectionItem.entityType];
            if (configuredColumns) {
                for (var i in configuredColumns) {
                    cols.push(configuredColumns[i]);
                }
            } else {
                cols = TableBinder.prototype.generateColumnDefinitions(sectionItem.entityType);
            }

            cols.push({
                actions: [{
                        labelText: "Show",
                        event: "show",
                        link: "self"
                    }, {
                        labelText: "Edit",
                        event: "edit",
                        link: "update"
                    }, {
                        labelText: "Remove",
                        event: "remove",
                        link: "remove"
                    }]
            });
            return cols;
        },
        buildForm: function(entity, readOnly) {
            var relationsForEntity, entityType = entity.clazzName();
            try {
                relationsForEntity = Relations[entityType];
            } catch (error) {
                console.log("no relations for " + entityType);
            }

            if (!entity) {
                entity = new RestBeans[entityType]();
            }
            var view;

            if (!relationsForEntity) {
                // if there are no relations defined, a simple FormBinder will be enough
                var sections;
                try {
                    sections = Forms[entityType];
                } catch (error) {
                    console.log("no sections for " + entityType);
                }
                if (!sections) {
                    sections = {};
                } else {
                    if (sections instanceof Array) {
                        sections = {
                            sections: sections
                        };
                    }
                }
                this.assertPersistIsHandled(sections);
                view = new FormBinder(CopyUtil.copyConfig(sections, {
                    entity: entity,
                    ignoredMembers: {
                        links: true,
                        page: true
                    }
                }));
            } else {
                if (relationsForEntity instanceof Array) {
                    relationsForEntity = {
                        relations: relationsForEntity
                    };
                }
                // if there are relations use a RelationFormBinder that allows switching through the related entities
                var rels = [{
                        link: "self",
                        name: "self",
                        labelText: "Basic"
                    }];
                for (var i in relationsForEntity.relations) {
                    rels.push(relationsForEntity.relations[i]);
                }
                // relationsForEntity.relations = rels;
				var relCopy = {
                    entity: entity,
					relations: rels,
					listeners: relationsForEntity.listeners
                };
				this.assertPersistIsHandled(relCopy);
                view = new RelationFormBinder(relCopy);
            }

            return view;
        },
        assertPersistIsHandled: function(config) {
            if (!config.listeners) {
                config.listeners = {};
            }
            if (!config.listeners.beforePersist) {
                config.listeners.beforePersist = function(entry, inputsByMemberOrKey, cb) {
					console.log("using default persist handler");
                    cb();
                };
            }
        },
        buildBrowser: function(sectionItem) {
            var cols = this.buildTableColumnsForSection(sectionItem);
            return new Browser({
                entityType: sectionItem.entityType,
                titleText: sectionItem.labelText,
                actions: [{
                        labelText: "new",
                        event: "newEntry"
                    }],
                tableBinder: {
                    columns: cols,
                    listeners: {
                        show: {
                            fn: function(entry) {
                                this.editTableEntry(entry, true, sectionItem.modalConfig, true);
                            },
                            scope: this
                        },
                        edit: {
                            fn: function(entry) {
                                this.editTableEntry(entry, true, sectionItem.modalConfig, false);
                            },
                            scope: this
                        },
                        remove: {
                            fn: this.removeTableEntry,
                            scope: this
                        }
                    }
                },
                listeners: {
                    newEntry: {
                        fn: function(browser, action) {
                            this.createNewEntry(browser, action, sectionItem.modalConfig);
                        },
                        scope: this
                    },
                    reindex: {
                        fn: function(browser, action) {
                            new RestBeans[browser.entityType]().primaryResource({
                                cb: function(pr){
                                    var l = pr.hasLink("reindex");
                                    if(l) {
                                        pr.follow({
                                            link: l,
                                            cb: function(){
                                                console.log("reindexed.");
                                            },
                                            fcb: function(){
                                                console.log("reindex failed.");
                                            }
                                        });
                                    }
                                },
                                scope: this
                            });
                        },
                        scope: this
                    }
                }
            });
        },
        editTableEntry: function(entry, reload, modalConfig, readOnly) {
            var prefix = readOnly ? "Show " : "Edit ";
            this.openForm(entry, reload, prefix + entry.clazzName(), modalConfig, readOnly);
        },
        createNewEntry: function(browser, action, modalConfig) {
            this.openForm(new RestBeans[browser.entityType](), false, "New " + browser.entityType, modalConfig, false);
        },
        openForm: function(entry, reload, title, modalConfig, readOnly) {
            if (reload) {
                entry.addSingleInvocationListener("reloaded", function(e) {
                    this.openForm(e, false, title, modalConfig, readOnly);
                }, this);
                entry.reload();
            } else {
                var view = this.buildForm(entry, readOnly);
                var editEntityModal = new Modal(CopyUtil.copyConfig(modalConfig, {
                    headerText: title,
                    destroyOnClose: true,
                    actions: [{
                            labelText: readOnly ? "Close" : "Submit",
                            event: "submit",
                            icon: "icon-ok"
                        }/*, {
                         labelText: "Reset",
                         event: "reset",
                         icon: "icon-trash"
                         }*/],
                    listeners: {
                        submit: {
                            fn: function(form, btn) {
                                if(readOnly) {
                                    editEntityModal.hide();
                                } else {
                                    view.fireEvent("beforePersist", entry, view.keyedInputs, entry.persistCallback(true, false));
                                }
                            },
                            scope: this
                        }/*,
                         reset: {
                         fn: function(form, btn) {
                         console.log("should reset...");
                         },
                         scope: this
                         }*/
                    }
                }));
                editEntityModal.renderTo($("body"));
                view.renderTo($(editEntityModal.body));
                editEntityModal.show();
            }
        },
        removeTableEntry: function(entry) {
            var modal = new Modal({
                isWarning: true,
                headerText: "Warning!",
                destroyOnClose: true,
                actions: [{
                        labelText: "no",
                        event: "dismiss"
                    }, {
                        labelText: "yes",
                        event: "delete"
                    }],
                listeners: {
                    dismiss: {
                        fn: function() {
                            modal.hide();
                        },
                        scope: this
                    },
                    delete: {
                        fn: function() {
                            entry.addListener("removed", function() {
                                modal.hide();
                            }, this);
                            entry.remove();
                        },
                        scope: this
                    }
                }
            });
            modal.renderTo($("body"));
            $(modal.body).append("<p>Do you really want to remove this entry?</p>");
            modal.show();
        }
    });
    return Navigation;
});
