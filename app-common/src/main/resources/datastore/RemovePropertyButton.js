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
	"jquery",
	"Enable",
	"Disable"
], function(
	Class,
	$,
	Enable,
	Disable
) {
	var RemovePropertyButton = Class.extend("RemovePropertyButton", {
		construct: function(config) {
			this.config = config;
			Enable.mixInto(this);
			Disable.mixInto(this);
		},
		bindTo: function(target) {
			if(!target.RemovePropertyButton) {
				var
					$button = $(target),
					$border = $button.closest(".js-input-border"),
					$propertyBorder = $button.closest(".js-property-border"),
					$buttonLabel = $button.find(".js-removeproperty-label"),
					removeLabelText = $button.data("label-remove"),
					revertLabelText = $button.data("label-revert"),
					$icon = $button.find(".js-icon"),
					removeIconClass = $button.data("icon-remove"),
					revertIconClass = $button.data("icon-revert"),
					input = $border.find("input,select"),
					multi = $propertyBorder.data("multi"),
					removeInput = $("<input type=\"hidden\" name=\"\" value=\"true\">"),
					removeInputTarget,
					onRemove = this.onRemove.bind(this),
					onKeep = this.onKeep.bind(this),
					that = this,
					isolated = input.length > 0 && multi
				;
				if(isolated) {
					removeInput.attr("name", input.attr("name")+"#remove");
					removeInputTarget = $button;
				} else {
					removeInput.attr("name", $propertyBorder.data("propertyname")+"#remove");
					removeInputTarget = $propertyBorder;
				}
				this.isolated = isolated;
				this.target = $button;
				this.buttonLabel = $buttonLabel;
				this.icon = $icon;
				this.input = Enable.mixInto(Disable.mixInto(input));
				this.multi = multi;
				this.removeInput = removeInput;
				this.removeLabelText = removeLabelText;
				this.removeIconClass = removeIconClass;
				this.revertLabelText = revertLabelText;
				this.revertIconClass = revertIconClass;
				this.removeInputTarget = removeInputTarget;
				$button.on("click", function() {
					if(that.willRemove()) {
						onRemove();
					} else {
						onKeep();
					}
				});
				target.RemovePropertyButton = this;
			}
		},
		willRemove: function() {
			return this.target.is(".js-will-remove");
		},
		onRemove: function() {
			this.remove();
		},
		onKeep: function() {
			this.keep();
		},
		remove: function() {
			var that = this;
			this.updateRemovalButton("btn-warning js-will-remove", "btn-danger", this.removeLabelText, this.removeIconClass);
			this.input.enable();
			// if(this.multi && !this.isolated) {
			// 	this.controlGroup.find("input+button.removeproperty,select+button.removeproperty").each(function(){
			// 		if(this.RemovePropertyButton instanceof RemovePropertyButton && this.RemovePropertyButton !== that) {
			// 			// this.RemovePropertyButton.keep();
			// 			if(!this.RemovePropertyButton.willRemove()) {
			// 				this.RemovePropertyButton.input.enable();
			// 			}
			// 			this.RemovePropertyButton.enable();
			// 		}
			// 	});
			// }
			this.removeRemovalMarker();
		},
		keep: function() {
			var that = this;
			this.updateRemovalButton("btn-danger", "btn-warning js-will-remove", this.revertLabelText, this.revertIconClass);
			this.input.disable();
			// if(this.multi && !this.isolated) {
			// 	this.controlGroup.find("input+button.removeproperty,select+button.removeproperty").each(function(){
			// 		if(this.RemovePropertyButton instanceof RemovePropertyButton && this.RemovePropertyButton !== that) {
			// 			// this.RemovePropertyButton.remove();
			// 			this.RemovePropertyButton.disable();
			// 			this.RemovePropertyButton.input.disable();
			// 		}
			// 	});
			// }
			this.addRemovalMarker();
		},
		updateRemovalButton: function(buttonClassToRemove, buttonClassToAdd, buttonText, iconClass) {
			this.target.removeClass(buttonClassToRemove).addClass(buttonClassToAdd);
			this.buttonLabel.text(buttonText);
			this.icon.removeClass().addClass(iconClass);
		},
		addRemovalMarker: function() {
			this.removeInput.appendTo(this.removeInputTarget);
		},
		removeRemovalMarker: function() {
			this.removeInput.detach();
		}
	});
	RemovePropertyButton.rebind = function(target) {
		$(target).children().find(".js-removeproperty").each(function() {
			new RemovePropertyButton().bindTo(this);
		});
	};
	return RemovePropertyButton;
});
