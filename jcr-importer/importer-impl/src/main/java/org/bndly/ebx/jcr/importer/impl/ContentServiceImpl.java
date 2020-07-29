package org.bndly.ebx.jcr.importer.impl;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-impl
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

import org.bndly.ebx.jcr.importer.api.UnsupportedClassException;
import org.bndly.ebx.jcr.importer.api.JcrUtil;
import org.bndly.ebx.jcr.importer.api.ContentNotFoundException;
import org.bndly.ebx.jcr.importer.api.ContentService;
import org.bndly.ebx.jcr.importer.api.ContentContextData;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.jcr.importer.api.PublicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

/**
 *
 * @author Stephan Gödecker &lt;stephan.goedecker@cybercon.de&gt;
 */
public class ContentServiceImpl implements ContentService {

	private static final Logger LOG = LoggerFactory.getLogger(ContentServiceImpl.class);

	private final Session session;
	private final JCRImporterConfiguration importerConfiguration;
	private final PublicationService publicationService;

	public ContentServiceImpl(Session session, JCRImporterConfiguration importerConfiguration, PublicationService publicationService) {
		if (session == null) {
			throw new IllegalArgumentException("session is not allowed to be null");
		}
		this.session = session;
		this.importerConfiguration = importerConfiguration;
		this.publicationService = publicationService;
	}

	@Override
	public void doPublish(Iterable<String> paths) {
		publicationService.doPublish(importerConfiguration, paths);
	}

	@Override
	public Node getContentNodeByName(String contentName, Node parentNode) {
		if (parentNode != null) {
			try {
				if (parentNode.hasNode(contentName)) {
					return parentNode.getNode(contentName);
				} else {
					throw new ContentNotFoundException("Couldn't find child of '" + parentNode.getPath() + "' named '" + contentName + "'");
				}
			} catch (RepositoryException e) {
				LOG.debug("could not get content node by name {} from parent node", contentName);
				return null;
			}
		} else {
			throw new IllegalArgumentException("'parentNode' must not be null");
		}
	}

	@Override
	public Node getContentNodeByNameAndParentPath(String contentName, String parentPath) {

		Node parentNode = getNodeByPathInternal(parentPath);

		if (parentNode != null) {
			return getContentNodeByName(contentName, parentNode);
		} else {
			throw new ContentNotFoundException("Parent node at '" + parentPath + "' couldn't be found.");
		}
	}

	@Override
	public Node getContentNodeByPath(String path) {
		Node requestedNode = getNodeByPathInternal(path);

		return requestedNode;
	}

	@Override
	public Node getContentNodeFromContextData(ContentContextData contentContextData) {
		Node contentNode = null;

		//in a JCR environment we should be try to access the related content node by
		//path first, since in relation a ID search is way too expensive
		if (contentContextData.getContentLocation() != null) {
			String loc = contentContextData.getContentLocation();

			contentNode = getNodeByPathInternal(loc);
		} else {

			String cid = contentContextData.getContentId();

			//TODO: implement id search
			//contentNode = cmsDao.getContentRepository().getContent(cid);
			LOG.warn("not implemented id search called. Better make this path thing work ;)");
		}
		if (contentNode == null) {
			throw new ContentNotFoundException("could not find content " + contentContextData.getContentId());
		}
		return contentNode;
	}

	private Node getNodeByPathInternal(String absolutePath) {
		try {
			if (session.nodeExists(absolutePath)) {
				return session.getNode(absolutePath);
			} else {
				LOG.error("Requested node '{}' does not exist.", absolutePath);
			}
		} catch (RepositoryException e) {
			LOG.error("Error while retrieving node '{}': {}", absolutePath, e.getMessage());
		}

		return null;
	}

	/**
	 * Creates the folders between the parent path and the target path
	 *
	 * @param parentPath the absolute path to an already existing node
	 * @param targetPath the absolute path of the node to create
	 */
	@Override
	public void createFolders(String parentPath, String targetPath) {
		Node parentNode = getContentNodeByPath(parentPath);
		if (parentNode == null) {
			throw new ContentNotFoundException("could not find node for parent path " + parentPath);
		}
		String relativePath = targetPath.substring(parentPath.length());
		if (relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1);
		}
		try {
			assertTargetFolderExists(relativePath, parentNode);
		} catch (RepositoryException ex) {
			throw new IllegalStateException("could not create folders", ex);
		}
	}

	private Node assertTargetFolderExists(String relativePath, Node currentFolder) throws RepositoryException {
		if (relativePath.isEmpty()) {
			return currentFolder;
		} else if (relativePath.length() > 1) {
			int i = relativePath.indexOf('/', 1);
			if (i > 0) {
				String subFolderName = relativePath.substring(0, i);
				Node subFolder;
				try {
					subFolder = currentFolder.getNode(subFolderName);
				} catch (PathNotFoundException e) {
					subFolder = createContent(currentFolder, subFolderName, "sling:Folder");
				}
				return assertTargetFolderExists(relativePath.substring(i + 1), subFolder);
			} else {
				Node subFolder;
				try {
					subFolder = currentFolder.getNode(relativePath);
				} catch (PathNotFoundException e) {
					subFolder = createContent(currentFolder, relativePath, "sling:Folder");
				}
				return subFolder;
			}
		} else if ("/".equals(relativePath)) {
			return currentFolder;
		} else {
			throw new IllegalArgumentException("could not assert that folder '" + relativePath + "' exists, because it was invalid.");
		}
	}

	@Override
	public Node createContent(Node parentNode, String contentName, String contentType) {
		if (parentNode != null) {
			try {

				Node createdNode = parentNode.addNode(contentName, contentType);

				if ("nt:unstructured".equalsIgnoreCase(contentType)) {
					createdNode.setProperty("id", UUID.randomUUID().toString());
				}
				return createdNode;

			} catch (RepositoryException e) {
				LOG.error("Error while creating content ({}): {}", contentName, e.getMessage());
			}
		} else {
			LOG.error("parentNode is null");
		}

		return null;
	}

	@Override
	public Node createImageContent(Node parentNode, String contentName, String contentType) {
		Node contentNode = createContent(parentNode, contentName, contentType);
		if (contentNode != null) {
			try {
				Node jcrContent = contentNode.addNode("jcr:content");
				jcrContent.setPrimaryType("dam:AssetContent");
				jcrContent.setProperty("cq:parentPath", parentNode.getPath());
				Node renditionsFolder = jcrContent.addNode("renditions", "nt:folder");
			} catch (RepositoryException e) {
				LOG.error("Error while creating image content ({}): {}", contentName, e.getMessage());
			}
		}

		return contentNode;
	}

	@Override
	public Node getRenditionsFolderFromAsset(Node assetNode) {
		if (assetNode != null) {
			String currentPath = null;
			try {
				currentPath = assetNode.getPath();
				if (assetNode.hasNode("jcr:content")) {
					Node jcrContent = assetNode.getNode("jcr:content");
					if (jcrContent != null && jcrContent.hasNode("renditions")) {
						Node renditionsNode = jcrContent.getNode("renditions");

						return renditionsNode;
					}
				}
			} catch (RepositoryException e) {
				LOG.error("Failed to retrieve original rendition from '{}'", currentPath);
			}
		}

		return null;
	}

	@Override
	public Node createOriginalRendition(Node renditionsFolder, InputStream input, String mimeType) throws RepositoryException {
		if (renditionsFolder != null) {
			Node renditionOriginal = renditionsFolder.addNode("original", "nt:file");

			Node renditionJcrContent = renditionOriginal.addNode("jcr:content", "nt:resource");
			JcrUtil.setProperty(renditionJcrContent, "jcr:data", input);
			JcrUtil.setProperty(renditionJcrContent, "jcr:mimeType", mimeType);
			JcrUtil.setProperty(renditionJcrContent, "jcr:lastModified", Calendar.getInstance());

			return renditionOriginal;
		} else {
			return null;
		}
	}

	@Override
	public String getContentNodeId(Node node) {
		try {
			if (node.hasProperty("id")) {
				return node.getProperty("id").getString();
			}
		} catch (RepositoryException e) {
			LOG.error("Couldn't retrieve uuid from node");
		}

		return null;
	}

	@Override
	public Value mapObjectToJcrValue(Object obj) {

		try {
			ValueFactory vf = session.getValueFactory();
			if (obj instanceof String) {
				return vf.createValue((String) obj);
			} else if (obj instanceof Long) {
				return vf.createValue((Long) obj);
			} else if (obj instanceof Double) {
				return vf.createValue((Double) obj);
			} else if (obj instanceof Boolean) {
				return vf.createValue((Boolean) obj);
			} else if (obj instanceof Calendar) {
				return vf.createValue((Calendar) obj);
			} else {
				throw new UnsupportedClassException("Property of class '" + obj.getClass().getSimpleName() + "' is not supported and therefore not persisted");
			}

		} catch (RepositoryException e) {
			LOG.error("Error while creating ValueFactory");
		}

		return null;
	}

	@Override
	public Node querySingle(String query) {
		try {
			QueryManager qm = session.getWorkspace().getQueryManager();

			Query q = qm.createQuery(query, Query.XPATH);

			QueryResult result = q.execute();

			if (result.getNodes().hasNext()) {
				return result.getNodes().nextNode();
			}
		} catch (RepositoryException e) {
			LOG.error("Error while executing JCR query: '{}'", e.getMessage());
		}
		return null;
	}

	@Override
	public String getCmsPath(String pathName, String language) {

		if (importerConfiguration != null) {

			for (JCRImporterConfiguration.Path path : importerConfiguration.getPaths()) {
				if (pathName.equals(path.getName())) {
					if (language == null || language.equals(path.getLanguage())) {
						return path.getJCRNodePath();
					}
				}
			}
		}
		throw new IllegalArgumentException("could not find a path configuration with name='" + pathName + "' and language='" + language + "'");
	}

	@Override
	public Session getCurrentSession() {
		return session;
	}

}
