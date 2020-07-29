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
	"cy/Expression", 
	"cy/HTMLUtil", 
	"cy/RestBeans", 
	"cy/LabelFunctions", 
	"cy/StringUtil",
	"cy/ui/Text"
], function(
	ViewComponent, 
	Expression, 
	HTMLUtil, 
	RestBeans, 
	LabelFunctions, 
	StringUtil,
	Text
){

    var TableBinder = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if (config.dataProvider) {
                config.dataProvider.addListener("load", this.dataLoaded, this);
            }
            this.callSuper(arguments, config);
        },
        generateColumnDefinitions: function(entityType) {
            var cols = [];
            var proto = new RestBeans[entityType]();
            proto.members()
                    .each(function(member) {
                        var type = proto.memberType(member);
                        if (type === "String" || type === "Number" || type === "Date" || type === "Boolean") {
                            cols.push({
                                path: member,
                                labelText: member
                            });
                        }
                    }, this);
            return cols;
        },
        renderColumnHeader: function(renderTarget) {
            this.header = $(renderTarget)
                    .append("<thead></thead>")
                    .children()
                    .last();
            this.headerRow = $(this.header)
                    .append("<tr></tr>")
                    .children()
                    .last();
            this.headersByIndex = {};
            for (var i in this.columns) {
                var column = this.columns[i];
                var label = column.labelText;
                if (!label) {
                    label = column.path;
                }
                if (!label) {
                    label = "";
                }
                this.headersByIndex[i] = $(this.header)
                        .append("<th></th>")
                        .children()
                        .last();
				new Text({value: label}).renderTo($(this.headersByIndex[i]));
                if (typeof (this.dataProvider.setSortField) === "function") {
                    if (column.path && column.path.split(".").length === 1 && column.path !== "id") {
                        var icon = $(this.headersByIndex[i]).append("<i></i>").children().last();
                        $(icon).click(this.createSortByCallback(column, this.dataProvider, icon));
                    }
                }
            }
        },
        createSortByCallback: function(column, dataProvider, icon) {
            var up = "icon-arrow-up";
            var down = "icon-arrow-down";
            var applyIcon = function() {
                if (dataProvider.sortAscending) {
                    $(icon).addClass(up);
                    $(icon).removeClass(down);
                } else {
                    $(icon).addClass(down);
                    $(icon).removeClass(up);
                }
            };
            applyIcon();
            return function() {
                if (dataProvider.sortField === column.path) {
                    dataProvider.toggleSortDirection();
                } else {
                    dataProvider.setSortField(column.path);
                }
                applyIcon();
                dataProvider.loadPage();
            };
        },
        renderTableRows: function(renderTarget) {
            this.body = $(renderTarget)
                    .append("<tbody></tbody>")
                    .children()
                    .last();
        },
        createActionCallback: function(_this, entry, event) {
            return function() {
                _this.fireEvent(event, entry);
            };
        },
        rerenderRows: function(items) {
            var body = this.body;
            $(body)
                    .children()
                    .remove();
            var columns = this.columns;
            if (body) {
                items.each(function(entry) {
                    var row = $(body)
                            .append("<tr></tr>")
                            .children()
                            .last();
                    for (var i in columns) {
                        var column = columns[i];
                        var cell = $(row)
                                .append("<td></td>")
                                .children()
                                .last();
                        if (column.path) {
                            var cellValue = new Expression({
                                root: entry,
                                path: column.path
                            })
                                    .resolve();
                            if (column.format) {
                                cellValue = column.format(cellValue);
                            }
                            if (cellValue === null || cellValue === undefined) {
                                cellValue = "";
                            } else if (cellValue instanceof Date) {
                                if(column.asTime) {
                                    cellValue = StringUtil.formatTime(cellValue);
                                } else if(column.asDateTime) {
                                    cellValue = StringUtil.formatDate(cellValue) + " " + StringUtil.formatTime(cellValue);
                                } else {
                                    cellValue = StringUtil.formatDate(cellValue);
                                }
                            } else if (true === cellValue) {
                                cellValue = "yes";
                            } else if (false === cellValue) {
                                cellValue = "no";
                            }
                            if (cellValue.clazzName && typeof (cellValue.clazzName) === "function") {
                                cellValue = LabelFunctions(cellValue);
                            }
                            $(cell).append(cellValue);
                        }
                        if (column.actions) {
                            var actionsGroup = $(cell)
                                    .append("<div class=\"btn-group pull-right\"></div>")
                                    .children()
                                    .last();
                            for (var j in column.actions) {
                                var
                                        action = column.actions[j],
                                        label = action.labelText,
                                        link = action.link,
                                        applyLinkToButton = function(e, btn) {
                                            if (e.hasLink(link)) {
                                                $(btn.element).removeAttr("disabled");
                                            } else {
                                                $(btn.element).attr("disabled", "");
                                            }
                                        };
                                if (!label) {
                                    label = action.event;
                                }

								var actionBtn = new Text({value: label, tag: "button", cls: "btn btn-small"});
								actionBtn.renderTo($(actionsGroup));
                                actionBtn.element.click(this.createActionCallback(this, entry, action.event));
                                if (link) {
                                    entry.addListener("changed", function(e) {
                                        applyLinkToButton(e, actionBtn);
                                    }, this);
                                    applyLinkToButton(entry, actionBtn);
                                }
                            }
                        }
                    }
                }, this);
            }
        },
        renderPaginationBar: function(renderTarget) {
            this.paginationWrapper = $(renderTarget)
                    .append("<div class=\"pagination\"></div>")
                    .children()
                    .last();
            this.rerenderPagingationBar();
        },
        rerenderPagingationBar: function() {
            this.paginationWrapper.children()
                    .remove();
            this.paginationList = $(this.paginationWrapper)
                    .append("<ul></ul>")
                    .children()
                    .last();
            var curPage = this.dataProvider.getCurrentPage();
            var total = this.dataProvider.getTotalPages();

            var createPageLoadCallBack = function(pageIndex, dataProvider) {
                return function() {
                    dataProvider.setPageOffset((pageIndex - 1) * dataProvider.getPageSize());
                    dataProvider.loadPage();
                    return false;
                };
            };

            var bonusAttributes = {};
            var disabled = false;
            if (curPage === 1) {
                bonusAttributes.class = "disabled";
                disabled = true;
            }
            ;
            this.prevElement = $(this.paginationList)
                    .append("<li " + HTMLUtil.serializeBonusAttributes(bonusAttributes) + "><a href=\"#\">&laquo;</a></li>")
                    .children()
                    .last();
            if (!disabled) {
                this.prevElement.click(createPageLoadCallBack(curPage - 1, this.dataProvider));
            } else {
                this.prevElement.click(function() {
                    return false;
                });
            }
            for (var i = 1; i <= total; i++) {
                bonusAttributes = {};
                disabled = false;
                if (i === curPage) {
                    bonusAttributes.class = "disabled";
                    disabled = true;
                }
                var pageLink = $(this.paginationList)
                        .append("<li " + HTMLUtil.serializeBonusAttributes(bonusAttributes) + "><a href=\"#\">" + i + "</a></li>")
                        .children()
                        .last();
                if (!disabled) {
                    $(pageLink)
                            .click(createPageLoadCallBack(i, this.dataProvider));
                } else {
                    $(pageLink)
                            .click(function() {
                                return false;
                            });
                }
            }

            bonusAttributes = {};
            disabled = false;
            if (curPage >= total) {
                bonusAttributes.class = "disabled";
                disabled = true;
            }
            this.nextElement = $(this.paginationList)
                    .append("<li " + HTMLUtil.serializeBonusAttributes(bonusAttributes) + "><a href=\"#\">&raquo;</a></li>")
                    .children()
                    .last();
            if (!disabled) {
                this.nextElement.click(createPageLoadCallBack(curPage + 1, this.dataProvider));
            } else {
                this.nextElement.click(function() {
                    return false;
                });
            }
        },
        renderTo: function(renderTarget) {
            this.table = $(renderTarget)
                    .append("<table class=\"table table-striped table-hover\"></table>")
                    .children()
                    .last();
            this.renderColumnHeader(this.table);
            this.renderTableRows(this.table);
            this.renderPaginationBar(renderTarget);

            this.dataProvider.loadPage();
        },
        dataLoaded: function(items) {
            this.rerenderRows(items);
            this.rerenderPagingationBar();
        }
    });
    return TableBinder;
});
