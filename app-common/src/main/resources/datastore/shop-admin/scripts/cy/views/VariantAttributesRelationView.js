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
define(["cy/EntityRelationViewComponent", "cy/RestBeans", "cy/LabelFunctions", "cy/EntityCollection", "cy/ui/Text"], function(EntityRelationViewComponent, RestBeans, LabelFunctions, EntityCollection, Text) {
    var VariantAttributesRelationView = EntityRelationViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            this.callSuper(arguments, config);
            this.listen(this.parent, "changed", this.variantChanged, this);
            this.listen(this.parent, "persisted", this.variantPersisted, this);
            this.loadAllDetails();
        },
        loadAllDetails: function() {
            var variantDetailsProto = new RestBeans["VariantDetailRestBean"]();
			if(this.parent.getId() || this.parent.hasLink("self")) {
				variantDetailsProto.setVariant(this.parent.ref());
				new EntityCollection({
					proto: variantDetailsProto,
					listeners: {
						allLoaded: {
							fn: this.allDetailsLoaded,
							scope: this
						}
					}
				}).load();
			}
        },
        allDetailsLoaded: function(variantDetails) {
            console.log("all details loaded. "+variantDetails.size());
            variantDetails.each(function(detail) {
                if(detail.getProductAttributeValue()) {
                    var name = detail.getProductAttributeValue().getProductAttribute().getName(),
                            attributeConfig = this.assertAttributeConfigExists(name);
                    // each attribute is assigned with a value in the association object "VariantDetailRestBean"
                    detail.setVariant(this.parent.ref());
                    attributeConfig.variantDetail = detail;
                    this.selectOptionOfProductAttributeValue(attributeConfig);                    
                }
            }, this);
        },
        assertAttributeConfigExists: function(name) {
            if (!this.attributes[name]) {
                this.attributes[name] = {
                    attributeName: name
                };
            }
            return this.attributes[name];
        },
        renderTo: function(renderTarget) {
            this.form = $(renderTarget).append("<form class=\"form-horizontal\"></form>").children().last();
            this.attributes = {};
            var product = this.parent.getProduct();
            if (product) {
                this.productChanged(product);
            }
        },
        variantChanged: function(parent, member, newValue, oldValue) {
            console.log("variant changed " + member);
            if (member === "product") {
                this.productChanged(parent.get(member));
            }
        },
        productChanged: function(product) {
            if (this.form) {
                this.form.children().remove();

                var pfa = new RestBeans.ProductFamilyAttributeRestBean();
                pfa.setFamily(product.getFamily());
                pfa.addListener("foundSimilar", this.renderAttributesOfPage, this);
                pfa.findAll();
            }
        },
        renderAttributesOfPage: function(proto, page) {
            if (page.getPage().getTotalRecords()) {
                // load the next page
                var next = page.hasLink("next");
                if (next) {
                    page.follow({
                        link: next,
                        cb: function(nextPage) {
                            this.renderAttributesOfPage(proto, nextPage);
                        },
                        scope: this
                    });
                }
                ;

                // render the already known attributes
                page.getItems().each(function(attribute) {
                    this.renderAttribute(attribute);
                }, this);
            } else {
				var text = new Text({value: "The product \"" + LabelFunctions(this.parent.getProduct()) + "\" does not define any attributes."});
                var p = $(this.form).append("<p></p>").children().last();
				text.renderTo(p);
            }
        },
        renderAttribute: function(attribute) {
            var name = attribute.getAttribute().getName();
            var label = LabelFunctions(attribute.getAttribute());
            if (!label) {
                label = name;
            }
            var attributeConfig = this.assertAttributeConfigExists(name);

            // each attribute is assigned with a value in the association object "VariantDetailRestBean"
            if (!attributeConfig.variantDetail) {
                var variantDetail = new RestBeans.VariantDetailRestBean();
                variantDetail.setVariant(this.parent.ref());
                attributeConfig.variantDetail = variantDetail;
            }

            attributeConfig.controlGroup = $(this.form).append("<div class=\"control-group\"></div>").children().last();
            attributeConfig.label = $(attributeConfig.controlGroup).append("<label class=\"control-label\"></label>").children().last();
			$(attributeConfig.label).attr("for", name);
			attributeConfig.labelText = new Text({value: label});
			attributeConfig.labelText.renderTo(attributeConfig.label);
            attributeConfig.controls = $(attributeConfig.controlGroup).append("<div class=\"controls\"></div>").children().last();
            attributeConfig.select = $(attributeConfig.controls).append("<select></select>").children().last();
            var _this = this;
            attributeConfig.select.change(function() {
                console.log("selected "+attributeConfig.select.val());
                if(attributeConfig.variantDetail) {
                    var val = new RestBeans.ProductAttributeValueReferenceRestBean();
                    val.setLink("self", attributeConfig.select.val());
                    attributeConfig.variantDetail.setProductAttributeValue(val);
                }
            });

            // load the available values for the attribute
            var valueProto = new RestBeans.ProductAttributeValueRestBean();
            var pa = new RestBeans.ProductAttributeRestBean();
            pa.setId(attribute.getAttribute().getId());
            valueProto.setProductAttribute(pa);
            valueProto.addListener("foundSimilar", this.renderValuesOfAttribute, this);
            valueProto.findAll();
        },
        renderValuesOfAttribute: function(proto, page) {
            // load the next page
            var next = page.hasLink("next");
            if (next) {
                page.follow({
                    link: next,
                    cb: function(nextPage) {
                        this.renderValuesOfAttribute(proto, nextPage);
                    },
                    scope: this
                });
            }
            ;

            page.getItems().each(function(attributeValue) {
                this.renderValueOfAttribute(attributeValue);
            }, this);
        },
        renderValueOfAttribute: function(attributeValue) {
            var name = attributeValue.getProductAttribute().getName();
            var href = attributeValue.hasLink("self").getHref();
            var label = LabelFunctions(attributeValue);
            if (!label) {
                label = attributeValue.getStringValue();
            }
            var attributeConfig = this.attributes[name];
            var opt = $(attributeConfig.select).append("<option></option>").children().last();
			$(opt).attr("value", href);
			new Text({value: label}).renderTo(opt);
            this.selectOptionOfProductAttributeValue(attributeConfig);
        },
        variantPersisted: function() {
            for(var name in this.attributes) {
                var attributeConfig = this.attributes[name];
                attributeConfig.variantDetail.setVariant(this.parent.ref());
                if(attributeConfig.variantDetail.getProductAttributeValue()) {
                    // console.log("persisting "+name+" now.");
                    attributeConfig.variantDetail.persist();
                }
            }
        },
        selectOptionOfProductAttributeValue: function(attributeConfig) {
            if (attributeConfig.variantDetail) {
                if (attributeConfig.select) {
                    var pav = attributeConfig.variantDetail.getProductAttributeValue();
                    if (pav) {
                        var val = pav.hasLink("self").getHref();
                        //console.log("setting attribute value " + val);
                        $(attributeConfig.select).val(val);
                    } else {
                        //console.log("no productattribute value");
                    }
                }
            }
        }
    });
    return VariantAttributesRelationView;

});
