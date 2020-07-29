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
define(["cy/ui/container/ViewContainer", "cy/Collection", "cy/ui/input/Button", "cy/ui/Text"], function(ViewContainer, Collection, Button, Text) {

	var ButtonGroup = ViewContainer.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}

			this.callSuper(arguments, config);
		},
		renderTo: function(renderTarget) {
			this.element = $(renderTarget).append("<div class=\"btn-group\"></div>").children().last();
			
			// this.invokeSuper(arguments);
			arguments.callee.$parent.renderTo.call(this, this.content);
		},
		renderItem: function(component) {
			if(this.rendered) {				
				var isButton = component instanceof Button;
				if(isButton) {
					arguments.callee.$parent.renderItem.call(this, component, this.element);
				}
			}
		}
	});
	return ButtonGroup;
});
