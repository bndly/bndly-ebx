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
	"cy/LabelFunctions", 
	"cy/ui/input/Button", 
	"cy/ui/container/Form", 
	"cy/ui/input/TextInput", 
	"cy/ui/input/SelectInput", 
	"cy/Collection",
	"cy/EntityCollection",
	"cy/FieldSet", 
	"cy/ui/input/StringListInput"], 
function(EntityRelationViewComponent, RestBeans, LabelFunctions, Button, Form, TextInput, SelectInput, Collection, EntityCollection, FieldSet, StringListInput) {
	var ProductTaggingRelationView = EntityRelationViewComponent.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			var tagNamesOfProduct = new Collection();
			config.tagNamesOfProduct = tagNamesOfProduct;

			config.newTagNameInput = new StringListInput({
				name: "tagNames",
				label: "Tags",
				values: tagNamesOfProduct
			});
			config.form = new Form({
				items: [
					config.newTagNameInput
				]
			});
			config.items = [
				config.form
			];

			var tagAssociationProto = new RestBeans.TagAssociationRestBean();
			var p = new RestBeans[config.parent.clazzName()];
			var self = config.parent.hasLink("self");
			if(self) {
				p.setLink("self", self.getHref());
				tagAssociationProto.setProduct(p);	
			}
			
			config.productTagAssociations = new EntityCollection({
				proto: tagAssociationProto
			});
			config.productTags = new EntityCollection({
				proto: new RestBeans.TagRestBean()
			});
			config.tagsToRemove = new Collection();

			this.callSuper(arguments, config);

			this.listen(this.productTagAssociations, "allLoaded", this.allAssociationsLoaded, this);
			this.listen(this.productTags, "allLoaded", this.allTagsLoaded, this);

			if(self) {
				this.productTagAssociations.load();
			}
			console.log("loading product tags");
			this.productTags.load();

			// load the already defined variant model attributes for the product
			this.listen(this.parent, "persisted", this.productPersisted, this);
			this.listen(this.tagNamesOfProduct, "removed", this.tagNameRemoved, this);
		},
		renderTo: function(renderTarget) {
			this.invokeSuper(arguments);
		},
		allAssociationsLoaded: function() {
			console.log("loaded all tag associations");
			this.associationsLoaded = true;
			if(!this.tagAssociationsByTagId) {
				this.tagAssociationsByTagId = {};
			}
			this.productTagAssociations.each(function(assoc){
				var tag = assoc.getTag();
				if(tag) {
					this.tagAssociationsByTagId[tag.getId()] = assoc;
				}
			}, this);
			this.applyTags();
		},
		allTagsLoaded: function() {
			console.log("loaded all tags");
			this.tagsLoaded = true;
			
			this.tagsById = {};
			this.tagsByName = {};
			this.productTags.each(function(tag){
				this.tagsById[tag.getId()] = tag;
				this.tagsByName[tag.getName()] = tag;
			}, this);
			
			this.applyTags();
		},
		applyTags: function() {
			if(this.tagsLoaded && this.associationsLoaded) {
				this.productTagAssociations.each(function(assoc){
					var t = assoc.getTag();
					if(t) {
						var tag = this.tagsById[t.getId()];
						assoc.setTag(tag);
						this.tagNamesOfProduct.add(tag.getName());
					}
				}, this);
			}

		},
		createdNewTagAssociation: function(assoc) {
			this.productTagAssociations.add(assoc);
			if(!this.tagAssociationsByTagId) {
				this.tagAssociationsByTagId = {};
			}
			this.tagAssociationsByTagId[assoc.getTag().getId()] = assoc;
		},
		createdNewTag: function(tag) {
			this.tagsById[tag.getId()] = tag;
			this.tagsByName[tag.getName()] = tag;
		},
		productPersisted: function(product) {
			this.tagNamesOfProduct.each(function(tagName) {
				var tag = this.tagsByName[tagName];
				if(tag) {
					if(!this.tagAssociationsByTagId) {
						this.tagAssociationsByTagId = {};
					}
					var assoc = this.tagAssociationsByTagId[tag.getId()];
					if(!assoc) {
						console.log("persisting new '"+tagName+"' association for existing tag");
						// only persist new tag associations
						assoc = new RestBeans.TagAssociationRestBean();
						assoc.setProduct(product.ref());
						assoc.setTag(tag.ref());
						assoc.persistAndReload();
					}
				} else {
					// create tag first
					console.log("persisting new '"+tagName+"' tag");
					tag = new RestBeans.TagRestBean();
					tag.setName(tagName);
					tag.addListener("reloaded", function(persisted){
						this.createdNewTag(persisted);
						console.log("persisting new '"+tagName+"' association for newly persisted tag");
						var assoc = new RestBeans.TagAssociationRestBean();
						assoc.setProduct(product.ref());
						assoc.setTag(persisted.ref());
						assoc.addListener("persisted", this.createdNewTagAssociation, this);
						assoc.persistAndReload();
					}, this);
					tag.persistAndReload();
				}
			}, this);
			
			this.tagsToRemove.each(function(tagName){
				console.log("should remove tag '"+tagName+"'");
				var tag = this.tagsByName[tagName];
				if(tag) {
					console.log("-- found tag '"+tagName+"'");
					var assoc = this.tagAssociationsByTagId[tag.getId()];
					if(assoc) {
						console.log("-- found tag '"+tagName+"' association");
						assoc.remove();
						this.tagAssociationsByTagId[tag.getId()] = undefined;
						this.productTagAssociations.remove(assoc);
					}
				}
			}, this);
			this.tagsToRemove.clear();
		},
		tagNameRemoved: function(tagName) {
			this.tagsToRemove.add(tagName);
		}
	});
	return ProductTaggingRelationView;

});
