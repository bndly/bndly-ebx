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
define([ebxServiceDescription, "cy/StringUtil", "cy/Observable", "cy/Collection"], function(serviceDescription, StringUtil, Observable, Collection) {
    // data contains the description of the service.
    // now is the time to build the runtime types.
    // return data;

    var cy = {};
    var entry = serviceDescription.entry;
    var types = serviceDescription.types;
    var typesByRootElement = {};
    var upperCaseFirst = StringUtil.upperCaseFirst;

    // getter, setter, loadData, serialize, follow("add", function(result){ })
    var createGetter = function(_member) {
        return function(_value, _silent) {
            var v = this[_member];
            return v;
        };
    };

    var createSetter = function(_member, isComplexType, isCollection, classDef) {
        if (isComplexType && !isCollection) {
            classDef["_" + _member + "Changed"] = function(nestedEntity, nestedMember, nestedValue, nestedOldValue) {
                this.fireEvent("changed", this, _member + "." + nestedMember, nestedValue, nestedOldValue);
            };
        } else if (isComplexType && isCollection) {
            classDef["_" + _member + "EntryInserted"] = function(insertedEntry, index) {
                if (insertedEntry) {
                    insertedEntry.addListener("changed", this["_" + _member + "EntryChanged"], this);
                }
            };
            classDef["_" + _member + "EntryRemoved"] = function(removedEntry, index) {
                if (removedEntry) {
                    removedEntry.removeListener("changed", this["_" + _member + "EntryChanged"]);
                }
            };
            classDef["_" + _member + "EntryChanged"] = function(entry, entryMember, entryValue, oldEntryValue) {
                var index = this[_member].indexOf(entry);
                if (index > -1) {
                    this.fireEvent("changed", this, _member + "[" + index + "]." + entryMember, entryValue, oldEntryValue);
                }
            };
        } else if (!isComplexType && isCollection) {
            classDef["_" + _member + "EntryInserted"] = function(insertedEntry, index) {
                // maybe fire a change
                var index = this[_member].indexOf(insertedEntry);
                if (index > -1) {
                    this.fireEvent("changed", this, _member + "[" + index + "]", insertedEntry, undefined);
                }
            };
            classDef["_" + _member + "EntryRemoved"] = function(removedEntry, index) {
                // maybe fire a change
                var index = this[_member].indexOf(removedEntry);
                if (index > -1) {
                    this.fireEvent("changed", this, _member + "[" + index + "]", removedEntry, undefined);
                }
            };
        }
        return function(_value, _silent) {
            var old = this[_member];

            this[_member] = _value;
            if (old !== _value) {
                if (isComplexType && !isCollection) {
                    if (old) {
                        // remove listener
                        old.removeListener("changed", this["_" + _member + "Changed"]);
                    }
                    if (_value) {
                        // add listener
                        _value.addListener("changed", this["_" + _member + "Changed"], this);
                    }
                } else if (isCollection) {
                    if (old) {
                        // remove listeners from collection
                        old.removeListener("inserted", this["_" + _member + "EntryInserted"]);
                        old.removeListener("removed", this["_" + _member + "EntryRemoved"]);

                        if (isComplexType) {
                            // remove listeners from collection entries
                            old.each(function(entry) {
                                entry.removeListener("changed", this["_" + _member + "EntryChanged"]);
                            }, this);
                        }

                    }
                    if (_value) {
                        // remove listeners from collection
                        _value.addListener("inserted", this["_" + _member + "EntryInserted"], this);
                        _value.addListener("removed", this["_" + _member + "EntryRemoved"], this);

                        if (isComplexType) {
                            // remove listeners from collection entries
                            _value.each(function(entry) {
                                entry.addListener("changed", this["_" + _member + "EntryChanged"], this);
                            }, this);
                        }
                    }
                }
                if (!_silent) {
                    if (typeof (this.fireEvent) === "function") {
                        this.fireEvent("changed", this, _member, _value, old);
                    }
                }
            }
        };
    };

    var getTypeByRootElement = function(rootElement) {
        var modelName = typesByRootElement[rootElement];
        if (modelName) {
            return cy[modelName];
        }
        return undefined;
    };

    var generateModel = function(modelName) {
        var classDef = {};
        var modelDefinition = types[modelName];
        if (!modelDefinition) {
            throw new Error("unknown model: " + modelName);
        }

        if (modelDefinition.rootElement) {
            typesByRootElement[modelDefinition.rootElement] = modelName;
        }

        // generate constructor
        classDef.construct = function(config) {
            if (arguments.callee.$parent.construct) {
                this.callSuper(arguments, config);
            }
        };

        // generate getter and setter
        for (var member in modelDefinition["members"]) {
            var memberDef = modelDefinition["members"][member];
            var m = member,
                    m2 = upperCaseFirst(member),
                    memberType = memberDef.type,
                    isCollection = memberDef.collection;
            var isSimpleType = memberType === "String" || memberType === "Number" || memberType === "Boolean" || memberType === "Date";
            classDef["get" + m2] = createGetter(m);
            classDef["set" + m2] = createSetter(m, !isSimpleType, isCollection, classDef);
        }

        classDef.clazz = function() {
            return cy[modelName];
        };

        classDef.clazzName = function() {
            return modelName;
        };

        classDef.get = function(member) {
            var m = upperCaseFirst(member);
            var getterMethod = this["get" + m];
            if (getterMethod) {
                return this["get" + m]();
            }
            return undefined;
        };

        classDef.set = function(member, value, silent) {
            var m = upperCaseFirst(member);
            var setterMethod = this["set" + m];
            if (setterMethod) {
                this["set" + m](value, silent);
            }
        };

        var addMemberNamesToCollection = function(typeName, collection) {
            for (var member in types[typeName]["members"]) {
                collection.add(member);
            }
            if (types[typeName].parent) {
                addMemberNamesToCollection(types[typeName].parent, collection);
            }
        };

        classDef.members = function() {
            var tmp = new Collection();
            addMemberNamesToCollection(modelName, tmp);
            return tmp;
        };

        var getMemberDeclaration = function(typeName, memberName) {
            var type = types[typeName];
            if (type) {
                var member = type.members[memberName];
                if (!member) {
                    if (type.parent) {
                        return getMemberDeclaration(type.parent, memberName);
                    } else {
                        return undefined;
                    }
                } else {
                    return member;
                }
            }
            return undefined;
        };

        classDef.memberIsCollection = function(member) {
            var declaration = getMemberDeclaration(modelName, member);
            return declaration ? declaration.collection === true : false;
        };

        classDef.memberType = function(member) {
            var declaration = getMemberDeclaration(modelName, member);
            return declaration ? declaration.type : undefined;
        };

        classDef.extends = function(parentTypeName) {
            if (parentTypeName === modelName) {
                return true;
            } else {
                var curModel = modelDefinition;
                while (curModel !== null && curModel !== undefined) {
                    if (curModel.parent === parentTypeName) {
                        return true;
                    } else {
                        curModel = types[curModel.parent];
                    }
                }
                return false;
            }
        };

        var getMemberBindingForType = function(memberDescription, typeName) {
            for (var binding in memberDescription["bindings"]) {
                var bindingType = memberDescription["bindings"][binding];
                if (bindingType === typeName) {
                    return binding;
                }
            }

            var parentTypeName = types[typeName]["parent"];
            if (parentTypeName) {
                return getMemberBindingForType(memberDescription, parentTypeName);
            } else {
                return undefined;
            }

        };

        var getMemberWithType = function(typeName, memberTypeName) {
            for (var member in types[typeName].members) {
                var memberDescription = types[typeName].members[member];
                if (memberDescription.type === memberTypeName) {
                    return member;
                }
            }
            if (types[typeName].parent) {
                return getMemberWithType(types[typeName].parent, memberTypeName);
            }
            return undefined;
        };

        var findLinkInParentResource = function(config) {
            var rel = config.rel;
            var consumes = config.consumes;
            var c = [];
            for (var t in types) {
                var tDef = types[t];
                if (tDef["links"]) {
                    for (var tRel in tDef["links"]) {
                        if (tRel === rel) {
                            if (tDef["links"][tRel].consumes === consumes || !consumes) {
                                c.push(t);
                            }
                        }
                    }
                }
            }
            return c;
        };

        var findLinkForPrimaryResource = function(primaryResourceName, rootResourceName) {
            var tDef = types[rootResourceName];
            if (!tDef) {
                throw new Error("unknown resource: " + rootResourceName);
            }
            var c = [];
            if (tDef["links"]) {
                for (var tRel in tDef["links"]) {
                    if (tDef["links"][tRel].returns === primaryResourceName) {
                        c.push(tRel);
                    }
                }
            }
            return c;
        };

        var findLinkByRel = function(rel, rootResourceName) {
            var tDef = types[rootResourceName];
            if (!tDef) {
                throw new Error("unknown resource: " + rootResourceName);
            }
            return tDef["links"][rel];
//			var c = [];
//			if (tDef["links"]) {
//                                
//				for (var tRel in tDef["links"]) {
//					if (tDef["links"][tRel].returns === primaryResourceName) {
//						c.push(tRel);
//					}
//				}
//			}
//			return c;
        };

        classDef.reload = function(silent) {
            this.follow({
                rel: "self",
                cb: function(_self) {
                    var data = {};
                    _self.serialize(data);
                    this.loadData(data, silent);
                    this.fireEvent("reloaded", this);
                },
                fcb: function(_error) {
                    this.fireEvent("reloadFailed", this, _error);
                },
                scope: this
            });
        };

		classDef.find = function() {
			var _t = this;
			followPrimaryResourceLink("find", this,
			function(url) {
				if (!url) {
					_t.fireEvent("notFound", _t);
				} else {
					if(typeof(url) !== "string") {
						if(url.clazzName() === _t.clazzName()) {
							var selfLink = url.hasLink("self");
							if(selfLink) {
								url = selfLink.getHref();
							} else {
								_t.fireEvent("notFound", _t);
							}
						} else {
							_t.fireEvent("notFound", _t);
						}
						
					}
					_t.setLink("self", url, "GET");
					_t.reload();
					_t.fireEvent("found", _t);
					
				}
			},
			function(_error) {
				_t.fireEvent("notFound", _t);
			}, this, "find", "find");
		};

        classDef.findAll = function() {
			console.log("find all for "+modelName);
            var _t = this;
            var linkExists = followPrimaryResourceLink("findAll", this,
                    function(url) {
                        if(typeof(url) === "string") {
                            _t.follow({
                                url: url,
                                cb: function(page) {
                                    _t.fireEvent("foundSimilar", _t, page);
                                },
                                fcb: function(_error) {
                                    _t.fireEvent("noSimilarFound", _t);
                                }
                            });
                        } else if(url instanceof cy["AtomLink"]) {
                            _t.follow({
                                link: url,
                                cb: function(page) {
                                    _t.fireEvent("foundSimilar", _t, page);
                                },
                                fcb: function(_error) {
                                    _t.fireEvent("noSimilarFound", _t);
                                }
                            });
                        } else {
                            _t.fireEvent("foundSimilar", _t, url);
                        }
                    },
                    function(_error) {
                        _t.fireEvent("notFound", _t);
                    }, this, "findAll", "findAll");
            if(!linkExists) {
                this.primaryResource({
                    cb: function(page){
                        _t.fireEvent("foundSimilar", _t, page);
                    },
                    fcb: function(_error) {
                        _t.fireEvent("noSimilarFound", _t);
                    }
                }, "findAll");
            }
        };

        var followPrimaryResourceLink = function(rel, payload, cb, fcb, scope, hint, purpose) {
//			console.log("following primary resource link");
            if (!scope) {
                scope = this;
            }
            var parents = findLinkInParentResource({
                rel: hint ? hint : rel,
                consumes: modelName
            });
            if (parents.length === 1) {
                var p = parents[0];
                var linksToParent = findLinkForPrimaryResource(p, cy.root.clazzName());
				var primaryResourceCallback = function(_p) {
					console.log("handling primary resource "+modelName);
					_p.follow({
						rel: rel,
						payload: payload,
						cb: cb,
						fcb: fcb,
						scope: scope
					});
				};
                if (linksToParent.length === 1) {
                    cy.root.follow({
                        rel: linksToParent[0],
                        cb: primaryResourceCallback,
                        scope: scope
                    });
                    return true;
                } else if (linksToParent.length === 0) {
					tryWithSchema({
						cb: primaryResourceCallback,
						noLink: function() {
							console.log("no way found to get to " + p + " from the root resource.");
						},
                        scope: scope
					}, purpose);
					return true;
                } else {
                    console.log("no unique way found to get to " + p + " from the root resource.");
                }
            } else if (parents.length === 0) {
                console.debug("no parent found where " + modelName + " could be sent to via '" + rel + "'.");
            } else {
                console.log("multiple parents found where " + modelName + " could be sent to via '" + rel + "': " + parents);
            }
            return false;
        };

        classDef.followPrimaryResourceLink = function(config) {
            followPrimaryResourceLink(config.rel, config.payload, config.cb, config.fcb, config.scope, config.hint);
        };

        classDef.primaryResource = function(config, purpose) {
			console.log("do stuff with primary resource");
            var link;
            if (modelDefinition.referenceType) {
                var ft = getFullType(modelName);
                if (!ft) {
                    console.warn("could not find full type for " + modelName);
                } else {
                    link = findLinkByRel("list", ft);
                }
            } else {
                link = findLinkByRel("list", modelName);
            }
            if (link) {
                if (link.returns) {
                    var linksToParent = findLinkForPrimaryResource(link.returns, cy.root.clazzName());
                    if (linksToParent.length === 1) {
                        cy.root.follow({
                            rel: linksToParent[0],
                            cb: config.cb,
                            fcb: config.fcb,
                            scope: config.scope
                        });
                    } else {
						// it could be a generated model from a schema.
						config.noLink = function(){
							console.warn("could find primary resource to " + modelName + " and the return type, but there was no unique link from the primary resource to the return type.");
						};
						console.log("trying to access primary resource for "+modelName);
						tryWithSchema(config, purpose);
                    }
                } else {
                    console.warn("could find primary resource to " + modelName + " but the link did not provide a return type");
                }
            } else {
                console.warn("could not find primary resource to " + modelName);
            }
        };

		var tryWithSchema = function(config, purpose) {
			if(!purpose) {
				console.log("missing purpose info");
			}
			if(!cy.schemas) {
//				console.log("load schema information");
				cy.root.follow({
					rel: "schema",
					cb: function(schemaBeans) {
						cy.schemas = {};
						schemaBeans.getItems().each(function(schema){
							cy.schemas[schema.getName()] = {};
							schema.getTypes().each(function(type){
								if(!type.getIsAbstract()) {
									var link = type.hasLink("primaryResource");
									if(link) {
										cy.schemas[schema.getName()][type.getName()] = {
											rootLink: link,
											type: type
										};
									}
								}
							}, this);
						},this);
//						console.log("loaded schema information.");
						tryWithSchema(config, purpose);
					},
					scope: this
				});
				return;
			} else {
				for(var schemaName in cy.schemas) {
					if(typeof(schemaName) === "string") {
						var schema = cy.schemas[schemaName];
						var patchedModelName = modelName;
						var i = patchedModelName.indexOf("RestBean");
						var j = patchedModelName.indexOf("ReferenceRestBean");
						var k = patchedModelName.indexOf("ListRestBean");
						if(i > -1) {
							patchedModelName = patchedModelName.substr(0,i);
						} else if(j > -1) {
							patchedModelName = patchedModelName.substr(0,j);
						} else if(k > -1) {
							patchedModelName = patchedModelName.substr(0,k);
						}
						if(schema[patchedModelName]) {
//							console.log("found model "+modelName+" ("+patchedModelName+") with a primary resource link");
							schema[patchedModelName].type.follow({
								rel: "primaryResource",
								cb: function(pr){
									console.log("did load primary resource "+modelName+" "+purpose);
									config.cb.call(config.scope ? config.scope : this, pr);
								},
								fcb: function(pr){
									console.log("did not load primary resource");
									if(config.fcb) {
										config.fcb.call(config.scope ? config.scope : this, pr);
									}
								}
							});
//							schema[patchedModelName].type.follow({
//								rel: "primaryResource",
//								cb: config.cb,
//								fcb: config.fcb,
//								scope: config.scope
//							});
							return;
						}
					}
				}
				console.log("could not find link");
				config.noLink.call(config.scope);
			}
		};

        classDef.persistDelayedAndReload = function(fn, silent) {
            fn(this, this.persistCallback(true, silent));
        };
        
        classDef.persistAndReload = function(silent) {
            this._persist(true, silent);
        };

        classDef.persist = function(silent) {
            this._persist(false, silent);
        };

        classDef.persistCallback = function(reload, silent) {
            var _t = this;
            return function() {
                var updateLink = _t.hasLink("update");
                if (updateLink) {
                    _t.follow({
                        payload: _t,
                        link: updateLink,
                        cb: function() {
                            _t.fireEvent("persisted", _t);
                            if (reload) {
                                _t.reload();
                            }
                        },
                        fcb: function(_error) {
                            _t.fireEvent("persistenceFailed", _t, _error);
                        },
                        scope: _t
                    });
                } else {
                    // create
                    followPrimaryResourceLink("add", _t,
                            function(url) {
                                if (url) {
                                    _t.setLink("self", url, "GET");
                                }
                                _t.fireEvent("persisted", _t);
                                if (reload) {
                                    console.log("reloading after persist")
                                    _t.reload();
                                }
                            },
                            function(_error) {
                                _t.fireEvent("persistenceFailed", _t, _error);
                            });
                }
            };
        };
        
        classDef._persist = function(reload, silent) {
            this.persistCallback(reload, silent)();
        };

        classDef.remove = function(silent) {
            var removeLink = this.hasLink("remove");
            if (removeLink) {
                this.follow({
                    link: removeLink,
                    cb: function() {
                        this.fireEvent("removed", this);
                    },
                    fcb: function(_error) {
                        this.fireEvent("removeFailed", this, _error);
                    },
                    scope: this
                });
            }
        };

        classDef.hasLink = function(rel) {
            var member = getMemberWithType(modelName, "AtomLink");
            var result = undefined;
            if (member) {
                var v = this.get(member);
                if (v !== undefined) {
                    v.each(function(item, index) {
                        if (item.getRel() === rel) {
                            result = item;
                        }
                    }, this);
                }
            }
            return result;
        };

        classDef.follow = function(config) {
            var rel = config.rel,
                    cb = config.cb,
                    scope = config.scope,
                    item = config.link,
                    fcb = config.fcb,
                    payload = config.payload,
                    url = config.url;
            if (!item) {
                if (!rel) {
                    if (!url) {
                        throw new Error("provide a link or rel when calling 'follow'");
                    } else {
                        item = new cy.AtomLink();
                        item.setHref(url);
                        item.setMethod("GET");
                    }
                } else {
                    item = this.hasLink(rel);
                }
            }
            if (item) {
                var t = this;
                var dataToSend = undefined;
                var headers = {};
                if (payload) {
                    if (payload.wrappedSerialize && typeof (payload.wrappedSerialize) === "function") {
                        dataToSend = {};
                        payload.wrappedSerialize(dataToSend);
                        dataToSend = JSON.stringify(dataToSend);
                    } else {
                        dataToSend = payload;
                    }
                    headers["Content-Type"] = "application/json";
                }
                var url = item.getHref();
                if(typeof(url) !== "string") {
                    throw new Error("can not load url, because it isn't a string");
                }
                $.ajax({
                    dataType: "json",
                    url: url,
                    headers: headers,
                    xhrFields: {
                        withCredentials: true
                    },
					beforeSend: function(xhr){
						xhr.setRequestHeader("Accept", "application/json");
					},
                    type: item.getMethod(),
                    data: dataToSend,
                    complete: function(xhr, code) {
                        if (xhr.status === 0) {
                            console.log("no status in response for "+url+ " via "+item.getMethod());
                        } else {
                            if (xhr.status >= 400 && fcb) {
                                cb = fcb;
                            }

                            var elements = [];
                            if (xhr.status !== 204) {
                                var loc = xhr.getResponseHeader("Location");
                                if (xhr.getResponseHeader("Content-Type") === "application/json") {
                                    if (xhr.responseText) {
                                        var payload = $.parseJSON(xhr.responseText);
                                        for (var element in payload) {
                                            // get the according type
                                            var elementType = getTypeByRootElement(element);
                                            if (elementType) {
                                                // loadPayload
                                                var instance = new elementType();
                                                instance.loadData(payload[element]);
                                                elements.push(instance);
                                            }
                                        }
                                    }
                                } else if (loc) {
                                    elements.push(loc);
                                } else {
                                    // firefox jquery hack
                                    if (xhr.responseText) {
                                        try {
                                            var payload = $.parseJSON(xhr.responseText);
                                            for (var element in payload) {
                                                // get the according type
                                                var elementType = getTypeByRootElement(element);
                                                if (elementType) {
                                                    // loadPayload
                                                    var instance = new elementType();
                                                    instance.loadData(payload[element]);
                                                    elements.push(instance);
                                                }
                                            }
                                        } catch (error) {
                                        }
                                    }
                                }
                            }

                            // invoke callback
                            if (typeof (cb) === "function") {
                                if (!scope) {
                                    scope = t;
                                }
                                cb.apply(scope, elements);
                            }
                        }
                    }
                });
            }
        };

        classDef.setLink = function(rel, url, method) {
            var member = getMemberWithType(modelName, "AtomLink");
            if (member) {
                var v = this.get(member);
                if (v === undefined || v === null) {
                    v = new Collection();
                    this.set(member, v, true);
                }
                var found = null;
                v.each(function(item, index) {
                    if (item.getRel() === rel) {
                        found = item;
                    }
                }, this);
                if (!found) {
                    found = new cy.AtomLink();
                    v.add(found);
                }
                found.setRel(rel);
                found.setHref(url);
                if (!method) {
                    method = "GET";
                }
                found.setMethod(method);
            }
        };

        var getFullType = function(typeName) {
            var result = undefined;
            if (types[typeName].referenceType) {
                for (var i in types[typeName].sub) {
                    result = getFullType(types[typeName].sub[i]);
                    if (result) {
                        return result;
                    }
                }
            } else {
                result = typeName;
            }
            return result;
        };
		cy.getFullType = getFullType;

        var getReferenceType = function(typeName) {
            if (types[typeName].referenceType) {
                return typeName;
            }
            if (types[typeName].parent) {
                return getReferenceType(types[typeName].parent);
            }
            return undefined;
        };

        classDef.sub = function() {
            return modelDefinition.sub;
        };

        classDef.deRef = function() {
            var fullType = getFullType(modelName);
            if (fullType) {
                var full = new cy[fullType]();
                var tmp = {};
                this.serialize(tmp);
                full.loadData(tmp, true);
                return full;
            }
            return undefined;

        };

        classDef.refType = function(){
            var refType = getReferenceType(modelName);
            return cy[refType];
        };
        classDef.ref = function() {
            var refType = getReferenceType(modelName);
            if (refType) {
                var ref = new cy[refType]();
                var tmp = {};
                this.serialize(tmp);
                ref.loadData(tmp, true);
                return ref;
            }
            return undefined;
        };

        var isSimpleBindingType = function(bindingType) {
            return bindingType === "Boolean" || bindingType === "Number" || bindingType === "String" || bindingType === "Date";
        };

        classDef.loadPayload = function(payload, silent) {
            if (modelDefinition.rootElement) {
                var data = payload[modelDefinition.rootElement];
                this.loadData(data, silent);
            } else {
                console.log("can't load payload for " + modelName + " because no rootElement is defined.");
            }
        };

        classDef.loadData = function(data, silent) {
            if (data !== undefined) {
                for (var member in modelDefinition["members"]) {
                    var memberDescription = modelDefinition["members"][member];
                    var isCollection = memberDescription.collection;
                    var c;
                    if (isCollection) {
                        c = new Collection();
                    }
                    for (var binding in memberDescription["bindings"]) {
                        var bindingType = memberDescription["bindings"][binding];
                        var isSimple = isSimpleBindingType(bindingType);
                        if (!isSimple) {
                            if (isCollection) {
                                var a = data[binding];
                                if (!(a instanceof Array)) {
                                    a = [a];
                                }
                                for (var tmp in a) {
                                    if (a[tmp] !== undefined) {
										if(cy[bindingType]) {
											var v = new cy[bindingType]();
											v.loadData(a[tmp], silent);
											c.add(v);
										} else {
											console.warn("could not find constructor for "+bindingType);
										}
                                    }
                                }
                            } else {
                                if (data[binding] !== undefined) {
                                    var v = new cy[bindingType]();
                                    v.loadData(data[binding], silent);
                                    this.set(member, v, silent);
                                }
                            }
                        } else {
                            if (isCollection) {
                                var a = data[binding];
                                if (!(a instanceof Array)) {
                                    a = [a];
                                }
                                for (var tmp in a) {
                                    var v = a[tmp];
                                    if (v !== undefined) {
                                        if (bindingType === "Date") {
                                            v = new Date(v);
                                        }
                                        c.add(v);
                                    }
                                }
                            } else {
                                var v = data[binding];
                                if (v !== undefined) {
                                    if (bindingType === "Date") {
                                        v = new Date(v);
                                    }
                                    this.set(member, v, silent);
                                }
                            }
                        }
                    }

                    if (isCollection) {
                        this.set(member, c, silent);
                    }
                }
            }
            if (arguments.callee.$parent.loadData) {
                arguments.callee.$parent.loadData.call(this, data, silent);
            }
        };

        classDef.wrappedSerialize = function(data) {
            var rootElementName = modelDefinition["rootElement"];
            if (rootElementName) {
                data[rootElementName] = {};
                this.serialize(data[rootElementName]);
            } else {
                throw new Error("can't execute wrapped serialize on " + modelName);
            }
        };

        classDef.serialize = function(data) {
            for (var member in modelDefinition["members"]) {
                var memberDescription = modelDefinition["members"][member];
                var value = this.get(member);
                if (value !== null && value !== undefined) {
                    var typeOfMember = cy[memberDescription["type"]];
                    var isCollection = memberDescription.collection;

                    // complex type
                    if (typeOfMember) {
                        if (isCollection) {
                            value.each(function(item, index) {
                                var binding = getMemberBindingForType(memberDescription, item.clazzName());
                                if (binding) {
                                    if (data[binding] === undefined) {
                                        data[binding] = [];
                                    }
                                    var tmp = {};
                                    item.serialize(tmp);
                                    data[binding].push(tmp);
                                }
                            });
                        } else {
                            // find the binding to use
                            var binding = getMemberBindingForType(memberDescription, value.clazzName());
                            if (binding) {
                                data[binding] = {};
                                value.serialize(data[binding]);
                            }
                        }


                        // simple type
                    } else {
                        if (isCollection) {
                            value.each(function(item, index) {
                                for (var binding in memberDescription["bindings"]) {
                                    var bindingType = memberDescription["bindings"][binding];
                                    if (isSimpleBindingType(bindingType)) {
                                        if (data[binding] === undefined) {
                                            data[binding] = [];
                                        }
                                        data[binding].push(item);
                                    }
                                }
                            });
                        } else {
                            for (var binding in memberDescription["bindings"]) {
                                var bindingType = memberDescription["bindings"][binding];
                                if (isSimpleBindingType(bindingType)) {
                                    data[binding] = value;
                                }
                            }
                        }
                    }
                }
            }
            if (arguments.callee.$parent.serialize) {
                arguments.callee.$parent.serialize.call(this, data);
            }
        };

        // generate parent type to extend
        var parent = modelDefinition.parent;
        var parentClass;
        if (parent) {
            if (cy[parent] === undefined) {
                generateModel(parent);
            }
            parentClass = cy[parent];
        } else {
            parentClass = Observable;
        }

        cy[modelName] = parentClass.extend(classDef);
    };

    for (var type in types) {
        if (cy[type] === undefined) {
            generateModel(type);
        }
    }

    // load the root resource in order to get access to all primary resources such as carts, orders etc.
    var services = new cy.Services();
    cy.root = services;
    services.setLink("self", entry);
    services.reload();

    return cy;
});
