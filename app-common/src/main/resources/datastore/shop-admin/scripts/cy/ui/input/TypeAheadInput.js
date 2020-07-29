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
define(["cy/ui/input/TextInput", "cy/RestBeans", "cy/LabelFunctions", "cy/Task", "cy/Collection", "cy/SearchTableDataProvider", "cy/ui/input/InputTypeRegistry", "cy/ui/Text"], function (TextInput, RestBeans, LabelFunctions, Task, Collection, SearchTableDataProvider, InputTypeRegistry, Text) {
	var TypeAheadInput = TextInput.extend({
		construct: function (config) {
			if (!config) {
				config = {};
			}
			if (!config.entryLabelFn) {
				config.entryLabelFn = LabelFunctions;
			}
			if (!config.dataProvider) {
				if (config.entity && config.member) {
					var memberType = config.entity.memberType(config.member);
					memberType = RestBeans.getFullType(memberType);
					config.dataProvider = new SearchTableDataProvider({
						entityType: memberType
					});
				}
			}
			config.selectedIndex = -1;
			this.callSuper(arguments, config);
			if (this.dataProvider) {
				this.listen(this.dataProvider, "load", this.searchResultReceived, this);
			}

		},
		destroy: function () {
			arguments.callee.$parent.destroy.call(this);
		},
		renderTo: function (renderTarget) {
			this.input = $(renderTarget).append("<input type=\"text\"/>").children().last();
			if (this.disabled) {
				$(this.input).attr("disabled", "");
			}
			var _this = this;
			$(this.input).keydown(function (e) {
				_this.keydown(e);
			});
			$(this.input).keyup(function (e) {
				_this.keyup(e);
			});
			$(this.input).focus(function () {
				(_this.searchResultList).show();
			});
			$(this.input).blur(function () {
				(_this.searchResultList).hide();
				if (_this.noResult) {
					$(_this.input).val("");
					$(_this.noResultText).hide();
				}
				if (!$(_this.input).val()) {
					_this.setBoundValue(null);
				}
			});
			this.noResultText = $(renderTarget).append("<span class=\"help-inline typeahead noresults\">no results</span>").children().last();
			$(this.noResultText).hide();
			this.searchResultList = $(renderTarget).append("<ul class=\"nav nav-tabs nav-stacked typeahead\"></ul>").children().last();
			$(this.searchResultList).css({
				position: "absolute",
				width: $(this.input).outerWidth()
			});
			$(this.searchResultList).hide();
			var bv = this.getBoundValue();
			this.setValue(bv);
		},
		keyTriggersSearch: function (e) {
			var ignoredKeyCodes = {
				9: true,
				13: true,
				16: true,
				19: true,
				20: true,
				27: true,
				33: true,
				34: true,
				35: true,
				36: true,
				37: true,
				38: true,
				39: true,
				40: true,
				45: true,
				91: true,
				92: true,
				112: true,
				113: true,
				114: true,
				115: true,
				116: true,
				117: true,
				118: true,
				119: true,
				120: true,
				121: true,
				122: true,
				123: true,
				144: true,
				145: true
			};
			var isNoVisibleKey = e.shiftKey || e.ctrlKey || e.altKey || e.metaKey;
			if (isNoVisibleKey) {
				return false;
			}
			return ignoredKeyCodes[e.keyCode] ? false : true;
		},
		keydown: function (e) {
			if (e.keyCode === 40) {
				e.preventDefault();
			} else if (e.keyCode === 38) {
				e.preventDefault();
			} else if (e.keyCode === 13) {
				// Prevent return from submitting any form here (needs to be in keydown instead of keyup).
				// Closes the dropdown and focuses it.
				if (this.task) {
					this.task.cancel();
				}
				if (this.usedArrows) {
					var binding = this.searchResultBindings[this.selectedIndex];
					this.setValue(binding.entry);
					this.setBoundValue(binding.entry);
					$(this.searchResultList).hide();
				} else {
					this.submitSearch();
				}
				e.preventDefault();
				return false;
			}
		},
		keyup: function (e) {
			if (e.keyCode === 40) {
				// Down-arrow should go to the first visible item.
				this.incrementSelection();
			} else if (e.keyCode === 38) {
				// Up-arrow should go to the last visible item.
				this.decrementSelection();
			} else if (this.input.val()) {
				// setup a delayed search
				if (this.keyTriggersSearch(e)) {
					this.submitDelayedSearch();
				}
			} else {
				if (this.task) {
					this.task.cancel();
				}
			}
		},
		incrementSelection: function () {
			if(this.searchResultBindings) {
				this.usedArrows = true;
				this.selectedIndex++;
				if (this.selectedIndex === this.searchResultBindings.length) {
					this.selectedIndex = 0;
				}
				this.selectEntry();
			}
		},
		decrementSelection: function () {
			if(this.searchResultBindings) {
				this.usedArrows = true;
				if (this.selectedIndex === 0) {
					this.selectedIndex = this.searchResultBindings.length;
				}
				this.selectedIndex--;
				this.selectEntry();
			}
		},
		selectEntry: function () {
			for (var i in this.searchResultBindings) {
				var li = this.searchResultBindings[i].li;
				var a = this.searchResultBindings[i].a;
				if (i == this.selectedIndex) {
					$(li).addClass("active");
				} else {
					$(li).removeClass("active");
				}
			}
		},
		submitDelayedSearch: function () {
			if (this.dataProvider.searchTerm === this.buildSearchTerm()) {
				return false;
			}
			if (this.task) {
				this.task.cancel();
			}
			this.task = new Task({
				fn: this.submitSearch,
				scope: this
			});
			this.task.delay(500);
		},
		buildSearchTerm: function () {
			return "*" + this.input.val() + "*";
		},
		submitSearch: function () {
			this.usedArrows = false;
			this.selectedIndex = -1;
			this.dataProvider.setSearchTerm(this.buildSearchTerm());
			this.dataProvider.loadPage();
		},
		searchResultReceived: function (p) {
			var c = p;
			if (!(c instanceof Collection)) {
				// if we get a ListRestBean
				c = c.getItems();
			}

			this.searchResultBindings = [];
			$(this.searchResultList).children().remove();
			if (c.isEmpty()) {
				$(this.noResultText).show();
				this.noResult = true;
			} else {
				this.noResult = false;
				$(this.noResultText).hide();
				c.each(function (item) {
					this.addSearchResultItem(item);
				}, this);
			}
		},
		addSearchResultItem: function (item) {
			var _this = this;
			var label = this.entryLabelFn(item);
			var li = $(this.searchResultList).append("<li></li>").children().last();
			
			var a = new Text({value: label, tag: "a"});
			a.renderTo($(li));
			var index = this.searchResultBindings.length;
			$(a.element).mousedown(function () {
				_this.input.val(label);
				_this.selectedValue = item;
				_this.selectedIndex = index;
				_this.setBoundValue(item);
			});
			this.searchResultBindings.push({
				li: li,
				a: a,
				entry: item
			});
		},
		discoverRemote: function (bind, scope) {
			if (this.entity && this.member) {
				var memberType = this.entity.memberType(this.member);
				new RestBeans[memberType]().primaryResource({
					cb: function (primary) {
						var link = primary.hasLink("typeahead");
						if (link) {
							bind.call(scope, link.getHref());
						}
					},
					scope: this
				});
			}
		},
		getValue: function () {
			return this.selectedValue;
		},
		setValue: function (value) {
			var label = this.entryLabelFn(value);
			if (!label) {
				label = "";
			}
			$(this.input).val(label);
			this.selectedValue = value;
		}
	});
	InputTypeRegistry.register("TypeAheadInput", TypeAheadInput);
	return TypeAheadInput;
});
