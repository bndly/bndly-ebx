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
define(["cy/ui/ViewComponent", "cy/ui/Text"], function(ViewComponent, Text) {
    var Link = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            if(!config.title) {
                config.title = config.label;
            }
            if(!config.title) {
                config.title = "download";
            }
            this.callSuper(arguments, config);
        },
        renderTo: function(renderTarget) {
            this.element = $(renderTarget).append("<a></a>").children().last();
			$(this.element).attr("href", this.url);
			this.label = new Text({value: this.title});
			this.label.renderTo($(this.element));
            this.rendered = true;
            if(this.hidden) {
                this.hide();
            }
        },
        setUrl: function(url){
            if(!url) {
                url = "#";
            }
            this.url = url;
            if(this.element) {
                $(this.element).attr("href", url);
            }
        },
        hide: function(){
            if(this.element) {
                $(this.element).hide();
            }
        },
        show: function(){
            if(this.element) {
                $(this.element).show();
            }
        },
        destroy: function() {
            this.invokeSuper(arguments);
            if (this.element) {
				this.label.destroy();
                $(this.element).remove();
            }
        }
    });
    return Link;
});
