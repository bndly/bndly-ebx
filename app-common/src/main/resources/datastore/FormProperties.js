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
	"MultiToolbar",
	"RemovePropertyButton",
	"AddPropertyButton",
	"DragAndDrop",
	"PropertyDescriptor"
], function(
	Class,
	$,
	MultiToolbar,
	RemovePropertyButton,
	AddPropertyButton,
	DragAndDrop,
	PropertyDescriptor
) {
	var
	formPropertiesSelector = ".js-nodeproperties",
	FormProperties = Class.extend("FormProperties", {
		construct: function() {

		},
		bindTo: function(document) {
			var that = this;
			$(document).find(formPropertiesSelector).each(function(){
				if(!this.FormProperties) {
					that.form = this;
					that.form.FormProperties = that;
					that.rebind();
				}
			});
		},
		rebind: function() {
			this.bindMultiToolbar(this.form);
			this.bindRemovePropertyButton(this.form);
			this.bindAddPropertyButton(this.form);
			this.bindMultiValueDragAndDrop(this.form);
			this.bindPropertyDescriptors(this.form);
			this.bindCodeEditors(this.form);
		},
		bindMultiToolbar: function(target) {
			MultiToolbar.rebind(target);
		},
		bindRemovePropertyButton: function(target) {
			RemovePropertyButton.rebind(target);
		},
		bindAddPropertyButton: function(target) {
			AddPropertyButton.rebind(target);
		},
		bindMultiValueDragAndDrop: function(target) {
			DragAndDrop.rebind(target);
		},
		bindPropertyDescriptors: function(target) {
			PropertyDescriptor.rebind(target);
		},
		bindCodeEditors: function(target) {
			$(target).find("[data-md-editor='code'] input").each(function() {
				var
					$input = $(this),
					$inputBorder = $input.closest(".js-input-border")
					$target = $input.closest(".js-property-border")
					$children = $target.children(),
					codeLanguage = $target.data("md-language"),
					myCodeMirror = CodeMirror($target[0], {
						value: $input.attr("value"), // we are not using .val(), because in that case new line characters would be removed.
						mode:  codeLanguage ? codeLanguage : "javascript"
					})
				;
				$input.attr("type", "hidden");
				// $children.detach().appendTo(target);
				myCodeMirror.on("blur", function(cm){
					$input.val(cm.getValue());
				});
				//$target.find(".input-append").removeClass("input-append");
			});
		}
	});
	return FormProperties;
});
