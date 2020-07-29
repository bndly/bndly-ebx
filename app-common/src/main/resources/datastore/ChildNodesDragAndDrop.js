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
	"DragAndDrop",
	"jquery"
], function(
	DragAndDrop,
	$
) {
	var
	draggableSelector = "[draggable='true']",
	ChildNodesDragAndDrop = DragAndDrop.extend("ChildNodesDragAndDrop", {
		construct: function(config) {
			if(!config) {
				config = {};
			}
			this.callSuper(arguments, config);
		},
		bindTo: function(target) {
			this.callSuper(arguments, target);
		}
	});
	ChildNodesDragAndDrop.init = function() {
		$(".js-child-nodes").each(function(){
			var
				$listOfChildNodes = $(this)
			;
			$listOfChildNodes.find(".js-child-node").each(function(){
				var
					$childNodeItem = $(this),
					draggable
				;
				if($childNodeItem.is(draggableSelector)) {
					draggable = this;
				} else {
					draggable = $childNodeItem.find(draggableSelector).first()[0];
				}
				if(draggable) {
					new DragAndDrop({
						onDragAndDrop: function(){
							var targetIndex = this.dropTarget.data("index"),
								url = this.$draggable.data("move-url") + targetIndex
							;
							$.ajax({
								url: url,
								type: "POST",
								success: function() {
									window.location = window.location;
								},
								error: function() {
								}
							});
						}
					}).bindTo(draggable);
				}
			});

		});
	};
	return ChildNodesDragAndDrop;
});
