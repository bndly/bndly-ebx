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
requirejs.config({
    //By default load any module IDs from "lib"
    baseUrl: 'scripts',
    //except, if the module ID starts with "cy",
    //load it from the "scripts/cy" directory. paths
    //config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
    paths: {
        jquery: 'lib/jquery',
        bootstrap: 'lib/bootstrap/js/bootstrap.min',
        d3: 'lib/d3.v3'
    }
});

ebxServiceDescription = "/bndly/communicationDescription.jsonp";

requirejs(['jquery'], function(jq) {
    requirejs(['bootstrap'], function() {
        requirejs(['d3'], function() {
                require(["cy/RestBeans", "cy/ui/container/NavigationBar", "cy/views/PurchasableView", "cy/views/PurchasingView", "cy/views/PurchasedView", "cy/views/ConfigurationView", "cy/ui/input/InputTypeLoader"], function(RestBeans, NavigationBar, PurchasableView, PurchasingView, PurchasedView, ConfigurationView, InputTypeLoader) {
					require(["cy/WebSocketLogger"], function(WebSocketLogger) {
						var websocketUrl = (window.location.protocol === "https:" ? "wss://" : "ws://") + window.location.host + "/websocket/";
						WebSocketLogger.instance.connect(websocketUrl);
	                    var services = RestBeans.root;
	                    services.addSingleInvocationListener("reloaded", function() {
	                        var navBar = new NavigationBar({
	                            brand: "eBX",
	                            items: [
	                                {
	                                    label: "Purchasable",
	                                    view: PurchasableView
	                                },
	                                {
	                                    label: "Purchasing",
	                                    view: PurchasingView
	                                },
	                                {
	                                    label: "Purchased",
	                                    view: PurchasedView
	                                },
	                                {
	                                    label: "Configuration",
	                                    view: ConfigurationView
	                                }
	                            ]
	                        });
	                        navBar.renderTo($("body"));
	                    }, this);
	                    services.reload();
					});
                });
        });
    });
});
