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
define(["cy/RestBeans"],function(RestBeans) {
    return [
        {
            items: [{
                    member: "cmsUser",
                    labelText: "CMS User",
                    mandatory: true
                },
                {
                    member: "cmsPassword",
                    labelText: "CMS Password",
					mandatory: true,
					inputConfig: {
						type: "password"
					}
                },
				{
					member: "url",
                    labelText: "Connection URL",
					mandatory: true
				},
                {
                    member: "importEnabled",
                    labelText: "Import Enabled"
                },
                {
                    member: "exportChannels",
                    labelText: "Export Product Channels"
                },
                {
                    member: "exportMarketingProducts",
                    labelText: "Export Product Marketing Data"
                },
                {
                    member: "exportMasters",
                    labelText: "Export Product Master Data"
                },
				{
                    member: "paths",
                    config: {
                        sections: [
                            {
                                items: [
                                    {
                                        member: "items",
                                        labelText: "Paths"
                                    }
                                ]
                            }
                        ]
                    },
                    embedded: true
				}]
        }
    ];

});
