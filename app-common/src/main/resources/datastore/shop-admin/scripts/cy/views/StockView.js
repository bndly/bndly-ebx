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
define(["cy/ui/ViewComponent", "cy/RestBeans", "cy/ui/Text"], function(ViewComponent, RestBeans, Text) {
    var StockView = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }

            this.callSuper(arguments, config);
            this.listen(this.parent, "reloaded", this.loadStockItem, this);
			this.stockLabel = new Text({});
        },
        renderTo: function(renderTarget) {
            this.stockLabel.renderTo(renderTarget);
            this.invokeSuper(arguments);
            this.loadStockItem();
        },
        destroy: function() {
            this.invokeSuper(arguments);
			this.stockLabel.destroy();
        },
        loadStockItem: function() {
            if (this.parent.getSku()) {
                var si = new RestBeans["StockItemRestBean"]();
                si.setSku(this.parent.getSku());
                si.addSingleInvocationListener("found", function() {
                    si.addSingleInvocationListener("reloaded", this.didLoadStockItem, this);
                }, this);
                si.addSingleInvocationListener("notFound", function() {
                    this.didNotFindStockItem();
                }, this);
                si.find();
            } else {
				this.stockLabel.value.set("There is no SKU defined to look up the current stock.");
            }
        },
        didLoadStockItem: function(si) {
            this.stockLabel.value.set("There are "+si.getStock()+" items on stock.");
        },
        didNotFindStockItem: function() {
            this.stockLabel.value.set("There are 0 items on stock.");
        }
    });
    return StockView;

});
