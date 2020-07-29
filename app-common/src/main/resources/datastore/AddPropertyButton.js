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
	"MultiToolbar"
], function(
	Class,
	$,
	MultiToolbar
) {
	var AddPropertyButton = Class.extend("AddPropertyButton", {
		construct: function(config) {
			this.config = config;
			this.configByType = {};
		},
		bindTo: function(wrapper) {
			if(!wrapper.AddPropertyButton) {
				wrapper.AddPropertyButton = this;
				var
					$wrapperDiv = $(wrapper),
					$button = $wrapperDiv.find(".js-propertytype"),
					controlGroupTemplate = $wrapperDiv.data("input-template"),
					propertyNameInput = $wrapperDiv.find(".js-propertyname"),
					availableTypes = $wrapperDiv.find(".js-propertytype-selection"),
					$form = $button.closest(".js-nodeproperties"),
					that = this
				;
				this.FormProperties = $form[0].FormProperties;
				this.propertContainerTarget = $wrapperDiv.siblings(".js-newproperty-target");
				this.configsByType = {};
				this.wrapperDiv = $wrapperDiv;
				this.multitoolbarTemplate = $form.data("multitoolbar-template");
				this.movehandleTemplate = $form.data("multipropertymovehandle-template");
				this.controlGroupTemplate = controlGroupTemplate;
				this.propertyNameInput = propertyNameInput;
				availableTypes.find(".js-propertytype-choice").each(function() {
					var	$anchor = $(this),
						config = {
							multi: $anchor.data("multi"),
							type: $anchor.data("type"),
							template: $anchor.data("input-template")
						}
					;
					if(config.template) {
						that.configsByType[config.type] = config;
					}
					$anchor.on("click", function(e) {
						that.anchorClicked(e, config)
					});
				});
			}
		},
		anchorClicked: function(e, config) {
			if(this.propertyName()) {
				var
					$propertyContainer = $(this.controlGroupTemplate),
					inputTemplate = config.template ? config.template : this.configsByType[config.type].template,
					$inputInstance = $(inputTemplate),
					$label = $propertyContainer.find("label"),
					propertyName = this.propertyName(),
					multitoolbarInstance = null,
					multipropertymovehandleInstance = null,
					fieldName = propertyName+"@"+config.type+(config.multi?"[]":""),
					multitoolbarInstance = null,
					movehandleInstance = null
				;
				$propertyContainer.data("multi", config.multi);
				$propertyContainer.data("propertyname", propertyName);
				$label.text(propertyName);
				$label.attr("for", fieldName);

				// ajust the created input element
				if($inputInstance.is("[name='REPLACE']")) {
					$inputInstance.attr("name", fieldName);
				} else {
					$inputInstance.find("input[name='REPLACE'],select[name='REPLACE']").attr("name", fieldName);
				}

				// then put it in the property container
				$propertyContainer.find(".js-input-border").prepend($inputInstance);

				// put the property container in the form
				$propertyContainer.insertAfter(this.propertContainerTarget);

				if(config.multi) {
					multitoolbarInstance = $(this.multitoolbarTemplate);
					movehandleInstance = $(this.movehandleTemplate);
					$propertyContainer
						.attr("data-input-template", inputTemplate)
						.attr("data-propertyname", propertyName)
						.attr("data-type", config.type)
					;
					$propertyContainer.find(".js-input-border")
						.before(multitoolbarInstance)
						.prepend(movehandleInstance)
						.attr("data-dd-target", "true")
						.attr("data-dd-group", "property-"+propertyName)
					;
				}
				this.FormProperties.rebind();
				this.propertyNameInput.val("");
			}
			e.preventDefault();
		},
		propertyName: function() {
			return this.propertyNameInput.val();
		}
	});
	AddPropertyButton.rebind = function(target) {
		$(target).find(".js-newproperty").each(function() {
			new AddPropertyButton().bindTo(this);
		});
	};
	return AddPropertyButton;
});
