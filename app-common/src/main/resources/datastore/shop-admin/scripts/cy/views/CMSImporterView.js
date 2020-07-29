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
	"cy/RestBeans", 
	"cy/ui/input/Button", 
	"cy/ui/container/ButtonGroup", 
	"cy/ui/container/ViewContainer", 
	"cy/ui/ViewComponent",
	"cy/ui/input/SelectInput",
	"cy/ui/container/Form",
	"cy/ProgressBar",
	"cy/FormBinder",
	"cy/ui/Text",
	"cy/Value",
	"cy/CopyUtil",
	"cy/form/Forms"
], function(
	RestBeans, 
	Button, 
	ButtonGroup, 
	ViewContainer, 
	ViewComponent,
	SelectInput,
	Form,
	ProgressBar,
	FormBinder,
	Text,
	Value,
	CopyUtil,
	Forms
) {
    var CMSImporterView = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.titleText = new Text({value: "CMS Importer Status", tag: "h1"});
			config.stateLabel = new Text({value: "", tag: "p"});
            config.progressBar = new ProgressBar({
                hidden: true
            });
            config.triggerButton = new Button({
                label: "trigger",
                listeners: {
                    clicked: {
                        fn: this.triggerButtonClicked,
                        scope: this
                    }
                }
            });
            config.fullSyncButton = new Button({
                label: "full sync",
                listeners: {
                    clicked: {
                        fn: this.fullSyncButtonClicked,
                        scope: this
                    }
                }
            });
            config.enableToggleButton = new Button({
                label: "enable",
                listeners: {
                    clicked: {
                        fn: this.enableToggleButtonClicked,
                        scope: this
                    }
                }
            });
            config.reloadConfigButton = new Button({
                label: "reload config",
                listeners: {
                    clicked: {
                        fn: this.reloadConfigButtonClicked,
                        scope: this
                    }
                }
            });
            config.saveConfigButton = new Button({
                label: "save config",
				disabled: true,
                listeners: {
                    clicked: {
                        fn: this.saveConfigButtonClicked,
                        scope: this
                    }
                }
            });
			config.buttonGroup = new ButtonGroup({
				items: [
					config.enableToggleButton,
					config.triggerButton,
					config.fullSyncButton,
					config.reloadConfigButton,
					config.saveConfigButton
				]
			});
			config.configSelection = new SelectInput({
				label: "Available Configurations",
				entryValueFn: function(entry) {
					if(entry) {
						return entry.hasLink("self").getHref();						
					} else {
						return "";
					}
				},
				entryLabelFn: function(entry) {
					if(entry) {
						return entry.getName() + (entry.getActive() ? " (active)" : "");
					} else {
						return "";
					}
				}
			});
            config.container = new ViewContainer({
                items: [
					config.buttonGroup,
					new Form({
						items: [config.configSelection]
					})
				]
            });
            this.callSuper(arguments, config);
			this.currentStatus = new Value({
				listeners: {
					changed: {
						fn: this.statusChanged,
						scope: this
					}
				}
			});
			this.loadActivateConfiguration();
        },
        destroy: function() {
            this.invokeSuper(arguments);
			this.titleText.destroy();
        },
        renderTo: function(renderTarget) {
            this.renderTitle(renderTarget);
            this.stateLabel.renderTo($(renderTarget));
            this.progressBar.renderTo(renderTarget);
            this.container.renderTo(renderTarget);
            this.invokeSuper(arguments);
            this.loadStatus(this.receivedStatus, this);
            this.disableAllButtons();
        },
        loadStatus: function(cb, scope) {
			if(!cb && !scope) {
				cb = this.receivedStatus;
				scope = this;
			}
            RestBeans.root.follow({
                rel:"importer",
                cb: cb,
                scope: scope
            });
        },
        renderTitle: function(renderTarget) {
            if (this.titleText) {
                this.titleText.renderTo(renderTarget);
            }
        },
        renderStateLabel: function(status) {
            var connected = "";
            if(status.getConnected()) {
                connected = "connected and ";
            }
            if(status.getEnabled()) {
				this.enableToggleButton.setLabel("disable");
                if(status.getRunning()) {
					this.stateLabel.value.set("Importer is "+connected+"running.");
                } else {
					this.stateLabel.value.set("Importer is "+connected+"enabled.");
                }
            } else {
				this.enableToggleButton.setLabel("enable");
				this.stateLabel.value.set("Importer is "+connected+"disabled.");
            }
        },
        receivedStatus: function(status) {
			this.currentStatus.set(status);
        },
        stopUpdate: function(status) {
            if(!status.getRunning()) {
                this.enableAllButtons();
                status.removeListener("reloaded", this.updateProgressBar, this);
                status.removeListener("reloaded", this.stopUpdate, this);
                this.progressBar.hide();
                return false;
            };
        },
        triggerButtonClicked: function() {
            this.disableAllButtons();
            this.loadStatus(function(status){
                this.renderStateLabel(status);
                this.progressBar.setPercent(0);
                this.progressBar.show();
                status.follow({
                    rel: "trigger",
                    cb: function() {
                        status.removeListener("reloaded", this.updateProgressBar, this);
                        this.importingFinished();
                    },
                    scope: this
                });
                status.addListener("reloaded", this.updateProgressBar, this);
                status.reload();
            }, this);
        },
		statusChanged: function(status) {
			if(status) {
				this.renderStateLabel(status);
				if(status.getRunning()) {
					this.progressBar.show();
					status.addListener("reloaded", this.updateProgressBar, this);
					status.addListener("reloaded", this.stopUpdate, this);
					status.reload();
				} else {
					this.enableAllButtons();
				}
				if(status.getEnabled()) {
					this.enableToggleButton.setLabel("disable");
				} else {
					this.enableToggleButton.setLabel("enable");
				}
			}
		},
		enableToggleButtonClicked: function() {
			if(this.currentStatus.get()) {
				var rel = "enable";
				if(this.currentStatus.get().getEnabled()) {
					rel = "disable";
				}
				this.currentStatus.get().follow({
					rel: rel,
					cb: this.loadStatus,
					scope: this
				});
			}
		},
		reloadConfigButtonClicked: function() {
			this.disableAllButtons();
            
            this.loadStatus(function(status){
                status.follow({
                    rel: "reloadConfig",
                    cb: function() {
                        this.enableAllButtons();
                    },
                    scope: this
                });
            }, this);
		},
        updateProgressBar: function(status) {
            this.renderStateLabel(status);
            this.progressBar.setPercent(parseInt(status.getDone()*100));
            // let's poll the progress
            setTimeout(function() {
                status.reload();
            }, 2000);
        },
        enableAllButtons: function() {
            this.triggerButton.enable();
            this.fullSyncButton.enable();
			this.enableToggleButton.enable();
			this.reloadConfigButton.enable();
		},
        disableAllButtons: function() {
            this.triggerButton.disable();
            this.fullSyncButton.disable();
			this.enableToggleButton.disable();
			this.reloadConfigButton.disable();
		},
        fullSyncButtonClicked: function() {
            this.disableAllButtons();
            
            this.loadStatus(function(status){
                this.renderStateLabel(status);
                this.progressBar.setPercent(0);
                this.progressBar.show();
                status.follow({
                    rel: "fullSync",
                    cb: function() {
                        status.removeListener("reloaded", this.updateProgressBar, this);
                        this.importingFinished();
                    },
                    scope: this
                });
                status.addListener("reloaded", this.updateProgressBar, this);
                status.reload();
            }, this);
        },
        importingFinished: function() {
            this.enableAllButtons();
            this.progressBar.setPercent(100);
            this.loadStatus(function(status){
                this.progressBar.hide();
                this.renderStateLabel(status);
            }, this);
        },
		loadActivateConfiguration: function() {
			var _this = this;
            RestBeans.root.follow({
                rel: "schema",
                cb: function(schemaList){
		            if(schemaList.getItems()) {
		                schemaList.getItems().each(function(schema){
		                    if(schema.getName() === "ebx") {
					            schema.getTypes().each(function(type) {
					                if(type.getName() === "ImporterConfiguration") {
										_this.initImporterConfigurations(type);
					                }
					            });
		                    }
		                });
		            }
                }
            });
		},
		initImporterConfigurations: function(type) {
			type.follow({
				rel: "primaryResource",
				cb: function(configurations) {
					console.log("got configs");
					console.log(configurations);
		            configurations.getItems().each(function(config) {
		                this.configSelection.items.add(config);
						if(config.getActive()) {
							this.activeConfig = config;
						}
		            }, this);
					if(this.activeConfig) {
						var formBinderView = this.createFormBinderFromConfiguration(this.activeConfig);
						this.container.items.add(formBinderView);
						this.saveConfigButton.enable();
					}
				},
				scope: this
			});
		},
		createFormBinderFromConfiguration: function(configuration) {
            var sections;
            try {
                sections = Forms["ImporterConfigurationRestBean"];
            } catch (error) {
                console.error(error);
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
			var formBinderView = new FormBinder(CopyUtil.copyConfig(sections, {
				entity: configuration,
				// keyedInputs: config.keyedInputs,
				ignoredMembers: {
					links: true,
					page: true
				}
			}));
			return formBinderView;
		},
		saveConfigButtonClicked: function() {
			if(this.activeConfig) {
				this.activeConfig.persist();
			}
		}
    });
    return CMSImporterView;

});
