package org.bndly.ebx.jcr.velocity.datastore;

/*-
 * #%L
 * org.bndly.ebx.jcr-velocity
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

import org.bndly.ebx.jcr.velocity.JCRDao;
import org.bndly.ebx.jcr.velocity.JCRDao.DefaultingJCRSessionCallback;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.common.velocity.api.VelocityDataProvider;
import org.bndly.ebx.jcr.importer.api.path.JCRPath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = VelocityDataProvider.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(factory = true, ocd = JCRVelocityDataProvider.Configuration.class)
public class JCRVelocityDataProvider implements VelocityDataProvider {

	@ObjectClassDefinition
	public @interface Configuration {

		@AttributeDefinition(name = "Name", description = "The name of the dataprovider. The name should be lowercase and should not contain whitespaces or special characters.")
		String name() default "jcr";

		@AttributeDefinition(name = "JCR root folder", description = "The root folder for resources of this data provider within the target JCR.")
		String rootFolder() default "/";

		@AttributeDefinition(name = "JCR DAO", description = "An OSGI filter expression to access the JCRDao implementation to use for connections to the JCR.")
		String jcrDao_target() default "(name=default)";

	}
	
	private static final Logger LOG = LoggerFactory.getLogger(JCRVelocityDataProvider.class);
	
	private String prefix;
	private static final byte[] EMPTY_ARRAY = new byte[]{};
	private static final InputStream EMPTY_BYTE_ARRAY_STREAM = new ByteArrayInputStream(EMPTY_ARRAY);
	
	@Reference(name = "jcrDao")
	private JCRDao jcrDao;
	private String name;
	private JCRPath rootFolder;

	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties()).emptyStringAsNull();
		this.name = dictionaryAdapter.getString("name", "jcr");
		rootFolder = JCRPath.newInstance(dictionaryAdapter.getString("rootFolder", "/"));
		this.prefix = "/" + name;
		// perform a connection test
		boolean success = jcrDao.run(new DefaultingJCRSessionCallback<Boolean>() {
			@Override
			public Boolean doInJCRSession(Session session) throws RepositoryException {
				try {
					session.getRootNode();
					return true;
				} catch (RepositoryException e) {
					return false;
				}
			}

			@Override
			public Boolean getDefault() {
				return false;
			}

		});
		if (success) {
			LOG.info("JCR connection test was successfull");
		} else {
			LOG.error("JCR connection test failed");
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getBytes(final String resourceName) {
		if (!isJCRResource(resourceName)) {
			return null;
		}
		return jcrDao.run(new DefaultingJCRSessionCallback<byte[]>() {
			@Override
			public byte[] getDefault() {
				return null;
			}

			@Override
			public byte[] doInJCRSession(Session sn) throws RepositoryException {
				Node node = sn.getNode(getResourcePathInJCR(resourceName));
				if (!node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FILE)) {
					return getDefault();
				}
				try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
					JcrUtils.readFile(node, bos);
					bos.flush();
					return bos.toByteArray();
				} catch (IOException e) {
					LOG.error("error while reading file data from jcr", e);
					return getDefault();
				}
			}
		});
	}

	@Override
	public InputStream getStream(String resourceName) {
		if (!isJCRResource(resourceName)) {
			return null;
		}
		byte[] bytes = getBytes(resourceName);
		return bytes == null ? null : new ByteArrayInputStream(bytes);
	}

	@Override
	public long getLastModified(final String resourceName) {
		if (!isJCRResource(resourceName)) {
			return -1;
		}
		return jcrDao.run(new DefaultingJCRSessionCallback<Long>() {
			@Override
			public Long getDefault() {
				return -1L;
			}

			@Override
			public Long doInJCRSession(Session sn) throws RepositoryException {
				Node node = sn.getNode(getResourcePathInJCR(resourceName));
				if (!node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FILE)) {
					return getDefault();
				}
				Calendar lm = JcrUtils.getLastModified(node);
				return lm.getTimeInMillis();
			}
		});
	}

	@Override
	public boolean isModified(final String resourceName, final long knownAge) {
		if (!isJCRResource(resourceName)) {
			return false;
		}
		return jcrDao.run(new DefaultingJCRSessionCallback<Boolean>() {
			@Override
			public Boolean getDefault() {
				return false;
			}

			@Override
			public Boolean doInJCRSession(Session sn) throws RepositoryException {
				Node node = sn.getNode(getResourcePathInJCR(resourceName));
				if (!node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FILE)) {
					return false;
				}
				Calendar lm = JcrUtils.getLastModified(node);
				return lm.getTimeInMillis() > knownAge;
			}
		});
	}

	@Override
	public boolean exists(final String resourceName) {
		if (!isJCRResource(resourceName)) {
			return false;
		}
		return jcrDao.run(new DefaultingJCRSessionCallback<Boolean>() {
			@Override
			public Boolean getDefault() {
				return false;
			}

			@Override
			public Boolean doInJCRSession(Session sn) throws RepositoryException {
				Node node = sn.getNode(getResourcePathInJCR(resourceName));
				return node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FILE);
			}
		});
	}
	
	private String getResourcePathInJCR(String resourceName) {
		String trimmedResourceName;
		if (resourceName.startsWith(getName())) {
			trimmedResourceName = resourceName.substring(getName().length());
		} else if (resourceName.startsWith(prefix)) {
			trimmedResourceName = resourceName.substring(prefix.length());
		} else {
			LOG.error("received illegal resource name: {}", resourceName);
			return null;
		}
		return rootFolder.isRoot() ? trimmedResourceName : rootFolder.toString() + trimmedResourceName;
	}
	
	private boolean isJCRResource(String resourceName) {
		return resourceName.startsWith(getName()) || resourceName.startsWith(prefix);
	}

	/**
	 * This setter exists only for testing purpose.
	 * @param jcrDao the jcr dao for testing
	 */
	void setJcrDao(JCRDao jcrDao) {
		this.jcrDao = jcrDao;
	}
	
}
