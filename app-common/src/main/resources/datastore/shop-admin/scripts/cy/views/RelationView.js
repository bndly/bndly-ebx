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
define(["cy/ui/container/ViewContainer", "cy/form/Forms", "cy/FormBinder", "cy/ui/Text"], function(ViewContainer, Forms, FormBinder, Text) {

	var RelationView = ViewContainer.extend({
		construct: function(config) {
			if (!config) {
				config = {};
			}
			config.items = [
			];
			this.callSuper(arguments, config);
		},
		renderRelatedEntities: function() {
			var proto = new RestBeans[relationDescriptor.child]();
			proto.set(relationDescriptor.childProperty, _this.entity);
			var dataProvider = new EntityTableDataProvider({
				proto: proto
			});
			var cols = tableConfigurations[proto.clazzName()];
			if (!cols) {
				cols = TableBinder.prototype.generateColumnDefinitions(proto.clazzName());
			}
			var tableBinder = new TableBinder({
				dataProvider: dataProvider,
				columns: cols
			});
			tableBinder.renderTo(this.view);

			var label = "New";
			var labelText = new Text({value: label});
			var btn = $(this.view)
				.append("<a href=\"#\" class=\"btn\"></a>")
				.children()
				.last();
			labelText.renderTo($(btn));
			// $(btn).click(createClickCallback(this, btn, action.event, action));
		}
	});
	return RelationView;
});
