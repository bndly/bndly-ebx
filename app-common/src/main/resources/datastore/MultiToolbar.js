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
	var MultiToolbar = Class.extend("MultiToolbar", {
		construct: function(config) {
			this.config = config;
		},
		bindTo: function(target) {
			if(!target.MultiToolbar) {
				target.MultiToolbar = this;
				var	that = this,
					multitoolbar = $(target),
					inputTemplate = null,
					propertyname = null,
					type = null,
					$propertyBorder = null
					$form = multitoolbar.closest(".js-nodeproperties"),
					multitoolbarTemplate = $form.data("multitoolbar-template"),
					movehandleTemplate = $form.data("multipropertymovehandle-template")
				;
				if(this.config && this.config.inputConfig) {
					inputTemplate = this.config.inputConfig.inputTemplateString;
					propertyname = this.config.inputConfig.propertyName;
					type = this.config.inputConfig.type;
				} else {
					$propertyBorder = $(target).closest(".js-property-border");
					inputTemplate = $propertyBorder.data("input-template");
					propertyname = $propertyBorder.data("propertyname");
					type = $propertyBorder.data("type");
				}
				this.FormProperties = $form[0].FormProperties;
				this.inputTemplate = inputTemplate;
				this.propertyname = propertyname;
				this.type = type;
				this.multitoolbarTemplate = multitoolbarTemplate;
				this.movehandleTemplate = movehandleTemplate;
				this.inputWrapperTemplate = $form.data("multipropertyinput-template");
				this.target = multitoolbar;
				this.target.children().find("button").each(function(){
					var button = $(this),
						semantic = button.data("semantic")
					;
					if(semantic && that[semantic]) {
						button.on("click", function(e){
							that[semantic](e);
						});
					}
				});
			}
		},
		add: function(e) {
			console.log("add property value");
			e.preventDefault();
			var	inputWrapperInstance = $(this.inputWrapperTemplate),
				inputInstance = $(this.inputTemplate),
				fieldName = this.propertyname+"@"+this.type+"[]"
			;
			inputWrapperInstance.find(".js-replace-me").replaceWith(inputInstance);
			inputWrapperInstance.find("input[name='REPLACE'],select[name='REPLACE']").attr("name", fieldName)
			inputWrapperInstance.attr("data-dd-target", "true").attr("data-dd-group", "property-"+this.propertyname);
			inputWrapperInstance.insertAfter(this.target);
			this.FormProperties.rebind();
		},
		remove: function(e) {
			console.log("remove property value");
			e.preventDefault();
		},
		movetotop: function(e) {
			console.log("move to top");
			e.preventDefault();
		},
		moveup: function(e) {
			console.log("move up");
			e.preventDefault();
		},
		movedown: function(e) {
			console.log("move down");
			e.preventDefault();
		},
		movetobottom: function(e) {
			console.log("move to bottom");
			e.preventDefault();
		}
	});
	MultiToolbar.rebind = function(target) {
		$(target).find(".js-multitoolbar").each(function() {
			new MultiToolbar().bindTo(this);
		});
	};
	return MultiToolbar;
});
