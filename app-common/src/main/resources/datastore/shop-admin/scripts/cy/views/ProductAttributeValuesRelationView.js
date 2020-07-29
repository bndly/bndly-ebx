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
define(["cy/EntityRelationViewComponent", "cy/RestBeans", "cy/LabelFunctions", "cy/ui/input/Button", "cy/ui/container/Form", "cy/ui/input/TextInput", "cy/ui/input/SelectInput", "cy/Collection", "cy/FieldSet"], function(EntityRelationViewComponent, RestBeans, LabelFunctions, Button, Form, TextInput, SelectInput, Collection, FieldSet) {
	var ProductAttributeValuesRelationView = EntityRelationViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			var newButton = new Button({
				label: "new"
			});
			config.form = new Form();
			config.items = [
				config.form,
				newButton
			]
			config.productAttributes = new Collection();

			this.callSuper(arguments, config);

			this.listen(config.productAttributes, "inserted", this.addedProductAttributeValue, this);
			this.listen(newButton, "clicked", function(btn) {
				this.createEmptyProductAttributeValue();
			}, this);

			// load the already defined variant model attributes for the product
			this.loadExistingProductAttributeValues();
			this.listen(this.parent, "persisted", this.productAttributePersisted, this);
		},
		createEmptyProductAttributeValue: function() {
			var ma = new RestBeans.ProductAttributeValueRestBean();
			this.productAttributes.add(ma);
		},
		loadExistingProductAttributeValues: function() {
			var self = this.parent.hasLink("self");
			if (self) {
				var variantModelProto = new RestBeans.ProductAttributeValueRestBean();
				var p = new RestBeans[this.parent.clazzName()];
				p.setLink("self", self.getHref());
				variantModelProto.setProductAttribute(p);
				variantModelProto.addListener("foundSimilar", this.productAttributeValuePageRead, this);
				variantModelProto.findAll();
			} else {
				console.log("parent product attribute has no self link");
			}
		},
		productAttributeValuePageRead: function(proto, page) {
			this.loadPageItemsToCollection(proto, page, this.productAttributeValuePageRead, this.productAttributes);
		},
		addedProductAttributeValue: function(productAttributeValue) {
			var valueInput = new TextInput({
				name: "stringValue",
				label: "Value",
				entity: productAttributeValue,
				member: "stringValue"
			});

			var fs = new FieldSet({
				items: [
					valueInput
				]
			});
			this.form.items.add(fs);
		},
		productAttributePersisted: function(productAttribute) {
			console.log("persisting " + this.productAttributes.size() + " product attribute values");
			this.productAttributes.each(function(ma) {
				if(!ma.getStringValue()) {
					console.log("empty attribute value");
				} else {
					ma.setProductAttribute(productAttribute.ref());
					if (ma.hasLink("update")) {
						console.log("updating");
					} else {
						console.log("creating");
					}
					ma.persistAndReload();	
				}
			}, this);
		}
	});
	return ProductAttributeValuesRelationView;

});
