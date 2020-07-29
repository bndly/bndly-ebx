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
define(["cy/Observable"], function(Observable) {

	var Relation = Observable.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
			this.callSuper(arguments, config);
			this.setParent(config.parent);
			this.setChild(config.child);
		},
		onParentSaved: function() {
			// save the children
			if (this.child) {
				this.child.persist();
			}
		},
		onParentRemoved: function() {
			// remove the children
			if (this.child) {
				this.child.remove();
			}
		},
		onChildReloaded: function() {
			// set the parent in the read child
			if (this.child) {
				this.child.set(this.getChildProperty(), this.parent);
			}
		},
		getParent: function() {
			return this.parent;
		},
		setParent: function(parent) {
			// bind listeners to persistence related events
			if (this.parent) {
				this.parent.removeListener("persisted", this.onParentSaved);
				this.parent.removeListener("removed", this.onParentRemoved);
			}
			this.parent = parent;
			if (this.parent) {
				this.parent.addListener("persisted", this.onParentSaved, this);
				this.parent.addListener("removed", this.onParentRemoved, this);
			}
		},
		getChildProperty: function() {
			return this.childProperty;
		},
		getChild: function() {
			return this.child;
		},
		setChild: function(child) {
			// bind listener to read event
			if (this.child) {
				this.child.removeListener("reloaded", this.onChildReloaded);
			}
			this.child = child;
			if (this.child) {
				this.child.addListener("reloaded", this.onChildReloaded, this);
			}
		}
	});
	return Relation;
});
