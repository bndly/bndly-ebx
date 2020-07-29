require.config({
	paths: {
		jquery: "../../../repo/libs/vendor/bin/jquery/js/jquery-3.2.1.min",
		bootstrap: "../../../repo/libs/vendor/bin/bootstrap/js/bootstrap.min"
	},
	packages: [
		{
			name: "codemirror",
			location: "codemirror-5.9",
			main: "lib/codemirror"
		}
	]
});

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
requirejs([
	"jquery",
	"Mixin",
	"FormProperties",
	"FormCreateChild",
	"RemoveNodeButton",
	"UploadFileButton",
	"ChildNodesDragAndDrop"
], function (
	$,
	Mixin,
	FormProperties,
	FormCreateChild,
	RemoveNodeButton,
	UploadFileButton,
	ChildNodesDragAndDrop
) {
	// load bootstrap javascript after jquery.
	requirejs(["bootstrap", "domReady!"], function(bootstrap, document) {
		new FormProperties().bindTo(document);
		new FormCreateChild().bindTo(document);
		new RemoveNodeButton().bindTo(document);
		$("div.file-upload").each(function(){
			new UploadFileButton().bindTo(this);
		});
		ChildNodesDragAndDrop.init();
	});
});
