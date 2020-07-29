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
	"Class",
	"jquery"
], function(
	Class,
	$
) {
	var DragAndDrop = Class.extend("DragAndDrop", {
		construct: function(config) {
			if(!config) {
				config = {};
			}
			this.config = config;
			if(typeof(config.canDrop) === "function") {
				this.canDrop = config.canDrop;
			}
			if(typeof(config.dropTargetFilter) === "function") {
				this.dropTargetFilter = config.dropTargetFilter;
			}
			if(typeof(config.onDragAndDrop) === "function") {
				this.onDragAndDrop = config.onDragAndDrop;
			}
			if(!this.canDrop) {
				this.canDrop = function(e) {
					var
						t = this.dropTargetFilter(e),
						targetDDGroup = t.data("dd-group")
					;
					if(targetDDGroup === this.ddGroup) {
						return true;
					}
				}
			}
			if(!this.dropTargetFilter) {
				this.dropTargetFilter = function(e) {
					return $(e.target).closest("[data-dd-target='true']");
				}
			}
			if(!this.onDragAndDrop) {
				this.onDragAndDrop = function(droppedOnDD) {
					var $dt = $(this.dropTarget),
						dtPropertyDescriptor = $dt.find("input,select").first()[0].PropertyDescriptor,
						draggedPropertyDescriptor = this.$draggable.find("input,select").first()[0].PropertyDescriptor,
						dtIndex,
						tmp
					;
					$dt.removeClass("drop-highlight");
					if(dtPropertyDescriptor && draggedPropertyDescriptor) {
						dtIndex = dtPropertyDescriptor.data.index;
						draggedPropertyDescriptor.setIndex(dtIndex);
						tmp = parseInt(dtIndex);
					}
					if($dt.prevAll().filter(this.$draggable).length !== 0) {
						$dt.after(this.$draggable);
						$dt.prevAll().addBack().find("input,select").each(function(){
							if(this.PropertyDescriptor) {
								var idx = this.PropertyDescriptor.data.index;
								if(idx !== undefined) {
									idx = parseInt(idx);
									idx = idx - 1;
									this.PropertyDescriptor.setIndex(idx);
								}
							}
						});
					} else {
						$dt.before(this.$draggable);
						$dt.nextAll().addBack().find("input,select").each(function(){
							if(this.PropertyDescriptor) {
								var idx = this.PropertyDescriptor.data.index;
								if(idx !== undefined) {
									idx = parseInt(idx);
									idx = idx + 1;
									this.PropertyDescriptor.setIndex(idx);
								}
							}
						});
					}
				}
			}
		},
		bindTo: function(target) {
			if(!target.DragAndDrop) {
				var
					dd = $(target),
					that = this
				;
				this.$draggable = this.dropTargetFilter({target: target});
				this.ddGroup = this.$draggable.data("dd-group");
				this.dropTarget = null;

				target.addEventListener("dragstart", function(e) {
					DragAndDrop.currentDD = that;
					return DragAndDrop.currentDD.onDragStart(e);
				}, false);
				target.addEventListener("dragenter", function(e) {
					DragAndDrop.currentDD.onDragEnter(e);
				}, false);
				target.addEventListener("dragleave", function(e) {
					DragAndDrop.currentDD.onDragLeave(e);
				}, false);
				target.addEventListener("dragover", function(e) {
					DragAndDrop.currentDD.onDragOver(e);
				}, false);
				target.addEventListener("drop", function(e) {
					DragAndDrop.currentDD.onDrop(e);
				}, false);
				target.addEventListener("dragend", function(e) {
					DragAndDrop.currentDD.onDragEnd(e);
					DragAndDrop.currentDD = null;
				}, false);
				target.DragAndDrop = this;
			}
		},
		onDragStart: function(e) {
			var id = this.ddGroup+"-dragged";
			this.dropTarget = null;
			this.$draggable.addClass("dragged");
		},
		onDrag: function(e) {
		},
		onDragEnter: function(e) {
		},
		onDragLeave: function(e) {
		},
		onDragOver: function(e) {
			if(this.canDrop(e)) {
				if(this.dropTarget === null) {
					this.dropTarget = this.dropTargetFilter(e);
					this.dropTarget.addClass("drop-highlight");
				} else {
					var toHighlight = this.dropTargetFilter(e);
					if(this.dropTarget[0] !== toHighlight[0]) {
						this.dropTarget.removeClass("drop-highlight");
						this.dropTarget = toHighlight;
						toHighlight.addClass("drop-highlight");
					}
				}

				if (e.preventDefault) {
					e.preventDefault();
				}
				return false;
			}
		},
		onDrop: function(e) {
			if(this.dropTarget) {
				if (e.stopPropagation) {
					e.stopPropagation();
				}
				if (e.preventDefault) {
					e.preventDefault();
				}
				this.onDragAndDrop(this._getDragAndDropInstance(e.target));
				return false;
			}

		},
		onDragEnd: function(e) {
			if(this.dropTarget !== null) {
				this.dropTarget.removeClass("drop-highlight");
			}
			this.$draggable.removeClass("dragged");
			this.dropTarget = null;
		},
		_getDragAndDropInstance: function(element) {
			if(!element) {
				return null;
			}
			if(element.DragAndDrop) {
				return element.DragAndDrop;
			}
			return this._getDragAndDropInstance(element.parentElement);
		}
	});
	DragAndDrop.rebind = function(target) {
		$(target).find("[draggable='true']").each(function() {
			new DragAndDrop().bindTo(this);
		});
	}
	DragAndDrop.currentDD = null
	return DragAndDrop;
});
