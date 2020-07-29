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
define(["./InputViewContainer", "cy/ui/input/InputViewComponent", "cy/Collection"], function(InputViewContainer, InputViewComponent, Collection) {
    var Form = InputViewContainer.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            this.callSuper(arguments, config);
        },
        renderTo: function(renderTarget) {
            this.element = $(renderTarget).append("<form class=\"form-horizontal\"></form>").children().last();
            $(this.element).submit(function() {
                // don't do a submit
                return false;
            });
            this.rendered = true;
            this.renderItems();
            if (this.hidden) {
                this.hide();
            }
        },
        hide: function() {
            if (this.element) {
                $(this.element).hide();
            }
        },
        show: function() {
            if (this.element) {
                $(this.element).show();
            }
        }
    });
    return Form;
});
