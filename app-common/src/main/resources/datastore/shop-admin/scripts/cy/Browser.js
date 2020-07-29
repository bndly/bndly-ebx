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
	"cy/TableBinder", 
	"cy/EntityTableDataProvider", 
	"cy/SearchConfiguration", 
	"cy/ui/input/SearchTermInput", 
	"cy/SearchTableDataProvider", 
	"cy/RestBeans",
	"cy/ui/Text"
], function(
		ViewComponent, 
		TableBinder, 
		EntityTableDataProvider, 
		SearchConfiguration, 
		SearchTermInput, 
		SearchTableDataProvider,
		RestBeans,
		Text
) {

    var Browser = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            this.callSuper(arguments, config);
			this.addListener("reload", this.reloadTable, this);
        },
        renderTitle: function(renderTarget) {
            if (this.titleText) {
				this.title = new Text({value: this.titleText, tag: "h1"});
				this.title.renderTo($(renderTarget));
            }
        },
        renderActions: function(renderTarget) {
			this.actions.push({
                    labelText: "reload",
                    event: "reload"
                });
            var searchConfig = SearchConfiguration(this.entityType);
            if (searchConfig) {
                this.searchTermInput = new SearchTermInput();
                this.listen(this.searchTermInput, "searchTermSubmitted", this.searchTermSubmitted, this);
                this.searchTermInput.renderTo(renderTarget);
                this.searchTermInput.form.addClass("pull-right");
                if (!this.actions) {
                    this.actions = [];
                }
                this.actions.push({
                    labelText: "reindex",
                    event: "reindex"
                });
            } else {
                console.debug("no search config for "+this.entityType);
            }

            if (this.actions) {
                this.toolbar = $(renderTarget)
                        .append("<div class=\"btn-toolbar\"></div>")
                        .children()
                        .last();
                this.toolbarButtons = $(this.toolbar)
                        .append("<div class=\"btn-group\"></div>")
                        .children()
                        .last();
                var createClickCallback = function(_this, action) {
                    return function() {
                        _this.fireEvent(action.event, _this, action);
                        return false;
                    };
                };
                for (var i in this.actions) {
                    var action = this.actions[i];
                    var label = action.labelText;
                    if (!label) {
                        label = action.event;
                    }
					
					action.btn = new Text({value: label, tag: "a", cls: "btn"});
					action.btn.renderTo($(this.toolbarButtons));
					$(action.btn.element).attr("href", "#");
                    $(action.btn.element).click(createClickCallback(this, action));
                }
            }
        },
        searchTermSubmitted: function(searchTerm, input) {
            this.tableBinder.dataProvider.setSearchTerm(searchTerm);
            this.tableBinder.dataProvider.loadPage();
        },
        renderTable: function(renderTarget) {
            if (this.tableBinder) {
                if (!(this.tableBinder instanceof TableBinder)) {
                    if (!this.tableBinder.dataProvider) {
                        if (SearchConfiguration(this.entityType)) {
                            this.tableBinder.dataProvider = new SearchTableDataProvider({
                                entityType: this.entityType
                            });
                        } else {
                            console.debug("no search config for "+this.entityType);
                            this.tableBinder.dataProvider = new EntityTableDataProvider({
                                entityType: this.entityType
                            });
                        }
                    }
                    this.tableBinder = new TableBinder(this.tableBinder);
                }
                this.tableBinder.renderTo(renderTarget);
            }
        },
        renderTo: function(renderTarget) {
            this.renderTitle(renderTarget);
            this.renderActions(renderTarget);
            this.renderTable(renderTarget);
        },
		reloadTable: function() {
			if (this.tableBinder) {
                if ((this.tableBinder instanceof TableBinder)) {
                    if (this.tableBinder.dataProvider) {
						this.tableBinder.dataProvider.loadPage();
					}
				}
			}
		}
    });
    return Browser;
});
