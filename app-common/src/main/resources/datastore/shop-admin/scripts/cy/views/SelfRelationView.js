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
define(["cy/ui/container/ViewContainer", "cy/form/Forms", "cy/FormBinder", "cy/CopyUtil"], function(ViewContainer, Forms, FormBinder, CopyUtil) {

    var SelfRelationView = ViewContainer.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }

            var entityType = config.entity.clazzName();

            var sections;
            try {
                sections = Forms[entityType];
            } catch (error) {
                console.log("no sections for " + entityType);
            }
            if (!sections) {
                sections = {};
            } else {
                if (sections instanceof Array) {
                    sections = {
                        sections: sections
                    };
                }
            }
            this.assertPersistIsHandled(sections);
            var view = new FormBinder(CopyUtil.copyConfig(sections, {
                entity: config.entity,
				keyedInputs: config.keyedInputs,
                ignoredMembers: {
                    links: true,
                    page: true
                }
            }));

            config.items = [
                view
            ];
            this.callSuper(arguments, config);
        },
        assertPersistIsHandled: function(config) {
            if (!config.listeners) {
                config.listeners = {};
            }
            if (!config.listeners.beforePersist) {
                config.listeners.beforePersist = function(entry, inputsByMemberOrKey, cb) {
                    cb();
                };
            }
        }
    });
    return SelfRelationView;
});
