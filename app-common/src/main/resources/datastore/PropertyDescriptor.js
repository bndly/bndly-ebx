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
	var PropertyDescriptor = Class.extend("PropertyDescriptor", {
		construct: function(config) {
		},
		bindTo: function(target) {
			if(target.PropertyDescriptor instanceof PropertyDescriptor) {
				return;
			}
			var $element = $(target);
			this.target = $element;
			this.data = this.loadFrom($element.attr("name"));
			target.PropertyDescriptor = this;
		},
		setIndex: function(index) {
			this.data.index = index;
			this.target.attr("name", this.createFrom(this.data));
		},
		setPropertyName: function(propertyName) {
			this.data.propertyName = propertyName;
			this.target.attr("name", this.createFrom(this.data));
		},
		setPropertyType: function(propertyType) {
			this.data.propertyType = propertyType;
			this.target.attr("name", this.createFrom(this.data));
		},
		setMulti: function(multi) {
			this.data.multi = multi;
			this.target.attr("name", this.createFrom(this.data));
		},
		setAction: function(action) {
			this.data.action = action;
			this.target.attr("name", this.createFrom(this.data));
		},
		createFrom: function(data) {
			var s = data.propertyName;
			if(data.propertyType) {
				s += "@"+data.propertyType;
			}
			if(data.multi) {
				s += "[";
				if(data.index !== null && data.index !== undefined) {
					s += data.index;
				}
				s += "]";
			}
			if(data.action) {
				s += "#"+data.action;
			}
			return s;
		},
		loadFrom: function(string) {
			var res = /([a-zA-Z0-9]+)(@(STRING|LONG|DATE|DECIMAL|DOUBLE|BOOLEAN|BINARY|ENTITY))?(\[([0-9]*)\])?(\#([a-zA-Z0-9]+))?/g.exec(string);
			return {
				propertyName: res[1],
				propertyType: res[3],
				multi: res[4] !== undefined && res[4] !== null && res[4].length > 0,
				index: res[5],
				action: res[7]
			};
		}
	});
	PropertyDescriptor.rebind = function(target) {
		$(target).children().find("input,select").each(function() {
			new PropertyDescriptor().bindTo(this);
		});
	};
	return PropertyDescriptor;
});
