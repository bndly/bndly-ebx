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
	"cy/EntityRelationViewComponent", 
	"cy/RestBeans", 
	"cy/ui/container/Form", 
	"cy/EntityCollection",
	"cy/ui/Image",
	"cy/ui/input/FileInput"
	], 
function(EntityRelationViewComponent, RestBeans, Form, EntityCollection, Image, FileInput) {
	var ImageUploadingView = EntityRelationViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			config.fileInput = new FileInput({
				name: "image",
				label: "new image",
				listeners: {
					uploaded: {
						fn: this.newImageUploaded,
						scope: this
					}
				}
			});
			config.items = [
				config.fileInput
			];

			config.associationEntityType = "ProductDataRestBean";
			config.associationParentField = "product";
			
			var proto = new RestBeans[config.associationEntityType]();
			var p = new RestBeans[config.parent.clazzName()];
			var self = config.parent.hasLink("self");
			if(self) {
				p.setLink("self", self.getHref());
				proto.set(config.associationParentField, p);	
				proto.setRelation("image");
			}
			
			config.imageAssociationsOfParent = new EntityCollection({
				proto: proto
			});

			this.callSuper(arguments, config);

			// load the already defined variant model attributes for the product
			this.listen(this.parent, "persisted", this.parentPersisted, this);
			this.listen(this.imageAssociationsOfParent, "allLoaded", this.allAssociationsLoaded, this);
			this.listen(this.imageAssociationsOfParent, "inserted", this.associationAdded, this);

			if(self) {
				this.imageAssociationsOfParent.load();
			}
		},
		renderTo: function(renderTarget) {
			this.invokeSuper(arguments);
		},
		parentPersisted: function(parent) {
			
		},
		allAssociationsLoaded: function(associations) {
			console.log("found "+associations.size()+" data associations");
		},
		associationAdded: function(association) {
			var data = association.getBinaryData();
			if(data) {
				var downloadLink = data.hasLink("download");
				var image = new Image({
					source: downloadLink.getHref()
				});
				this.items.add(image);
			}
		},
		newImageUploaded: function(input, imageData) {
			var pd = new RestBeans.ProductDataRestBean();
			pd.setBinaryData(imageData.ref());
			pd.setProduct(this.parent.ref());
			pd.setRelation("image");
			pd.addSingleInvocationListener("reloaded", function(productData){
				this.imageAssociationsOfParent.add(productData);
			}, this);
			pd.persistAndReload();
		}
	});
	return ImageUploadingView;

});
