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
    var Image = ViewComponent.extend({
        construct: function(config) {
            if (!config) {
                config = {};
            }
            this.callSuper(arguments, config);
        },
        destroy: function() {
            this.invokeSuper(arguments);
			this.emptyTextSpan.destroy();
        },
        renderTo: function(renderTarget) {
            if(this.emptyText) {
                this.emptyTextSpan = new Text({value: this.emptyText});
				this.emptyTextSpan.renderTo($(renderTarget));
                this.emptyTextSpan.hide();
            }
            this.wrapper = $(renderTarget).append("<div class=\"imageWrapper\" />").children().last();
            if (!this.source) {
                $(this.wrapper).hide();
                this.emptyTextSpan.show();
            }
            this.element = $(this.wrapper).append("<img/>").children().last();
			$(this.element).attr("src", this.source);
            $(this.wrapper).addClass("img-rounded");
            $(this.wrapper).addClass("img-polaroid");

            var css = {};
            if (this.width) {
                css.width = this.width + "px";
            }
            if (this.height) {
                css.height = this.height + "px";
            }

//            this.removeButton = $(this.wrapper).append("<a class=\"btn btn-mini btn-warning\" href=\"#\"><i class=\"icon-trash icon-white\"></i> remove</a>").children().last();

            $(this.element).css(css);
        },
        setSource: function(source) {
            this.source = source;
            if (this.wrapper) {
                if (!source) {
                    $(this.wrapper).hide();
                    this.emptyTextSpan.show();
                } else {
                    this.emptyTextSpan.hide();
                    $(this.element).attr("src", source);
                    $(this.wrapper).show();
                    
                }
            }
        }
    });
    return Image;
});
