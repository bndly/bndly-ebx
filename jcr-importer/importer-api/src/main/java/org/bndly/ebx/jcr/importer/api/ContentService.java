/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bndly.ebx.jcr.importer.api;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-api
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.io.InputStream;
import javax.jcr.Session;

/**
 *
 *
 * @author Stephan Gödecker &lt;stephan.goedecker@cybercon.de&gt;
 */
public interface ContentService {
	
	Session getCurrentSession();
	
	/**
	 * Calls {@link PublicationService#doPublish(org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration, java.lang.Iterable) } on the underlying publication service.The caller does not need to
	 * worry about the JCR importer configuration.
	 *
	 * @param paths The paths in the repository to publish.
	 */
	void doPublish(Iterable<String> paths);

	@Deprecated
    Node getContentNodeByName(String contentName, Node parentNode);

	@Deprecated
    Node getContentNodeByNameAndParentPath(String contentName, String parentPath);

	@Deprecated
    Node getContentNodeByPath(String path);

	@Deprecated
    Node getContentNodeFromContextData(ContentContextData contextData);

	@Deprecated
    void createFolders(String parentPath, String targetPath);

	@Deprecated
    Node createContent(Node parentNode, String contentName, String contentType);

	@Deprecated
    Node createImageContent(Node parentNode, String contentName, String contentType);

	@Deprecated
    Node getRenditionsFolderFromAsset(Node assetNode);

	@Deprecated
    Node createOriginalRendition(Node renditionsFolder, InputStream input, String mimeType) throws RepositoryException;

	@Deprecated
    String getContentNodeId(Node contentNode);

	@Deprecated
    Value mapObjectToJcrValue(Object obj);

	@Deprecated
    Node querySingle(String query);

	@Deprecated
    String getCmsPath(String pathName, String language);
}
