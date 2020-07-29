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
	"cy/views/TwoColumnView", 
	"cy/Navigation", 
	"cy/views/SchemaView", 
	"cy/views/ResourceInvocationView", 
	"cy/views/EventView",
	"cy/views/DataStoreView"
], function(
		TwoColumnView, 
		Navigation, 
		SchemaView, 
		ResourceInvocationView, 
		EventView,
		DataStoreView
) {
    var ConfigurationView = TwoColumnView.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            config.nav = new Navigation({
                sections: [
                    {
                        name: "Application",
                        items: [
                            {
                                labelText: "Schema",
                                view: new SchemaView()
                            },
                            {
                                labelText: "Invocations",
                                view: new ResourceInvocationView()
                            },
                            {
                                labelText: "Events",
                                view: new EventView()
                            }
                        ]
                    },
                    {
                        name: "BPM",
                        items: [
                            {
                                labelText: "Definitions",
                                entityType: "BusinessProcessDefinition"
                            },
                            {
                                labelText: "Instances",
                                entityType: "BusinessProcessInstance"
                            }
                        ]
                    },
                    {
                        name: "Data",
                        items: [
                            {
                                labelText: "List",
                                view: new DataStoreView()
                            }
                        ]
                    },
                    {
                        name: "Documents",
                        items: [
                            {
                                labelText: "PDF",
                                entityType: "PDFDocument"
                            },
                            {
                                labelText: "Property sets",
                                entityType: "PropertySetRestBean"
                            }
                        ]
                    },
                    {
                        name: "Translations",
                        items: [
                            {
                                labelText: "Translations",
                                entityType: "TranslatedObjectRestBean"
                            },
                            {
                                labelText: "Languages",
                                entityType: "LanguageRestBean"
                            }
                        ]
                    },
                    {
                        name: "Other",
                        items: [
                            {
                                labelText: "Countries",
                                entityType: "CountryRestBean"
                            }
                        ]
                    }/*,
                    {
                        name: "Validation",
                        items: [
                            {
                                labelText: "Rules",
                                entityType: "RuleSetRestBean",
                                modalConfig: {
                                    large: true
                                }
                            }
                        ]
                    }*/
                ]
            });

            this.callSuper(arguments, config);
        },
        renderTo: function(renderTarget) {
            this.invokeSuper(arguments);

            this.nav.contentRenderTarget = this.rightColumn;
            this.nav.renderTo(this.leftColumn);
        }
    });
    return ConfigurationView;
});
