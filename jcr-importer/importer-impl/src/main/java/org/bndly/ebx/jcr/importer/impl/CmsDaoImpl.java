/*
 * Copyright (c) 2011, cyber:con GmbH, Bonn.
 *
 * All rights reserved. This source file is provided to you for
 * documentation purposes only. No part of this file may be
 * reproduced or copied in any form without the written
 * permission of cyber:con GmbH. No liability can be accepted
 * for errors in the program or in the documentation or for damages
 * which arise through using the program. If an error is discovered,
 * cyber:con GmbH will endeavour to correct it as quickly as possible.
 * The use of the program occurs exclusively under the conditions
 * of the licence contract with cyber:con GmbH.
 */
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

import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.jcr.importer.api.CmsDao;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import org.apache.jackrabbit.spi2davex.Spi2davexRepositoryServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = CmsDao.class)
@Designate(ocd = CmsDaoImpl.Configuration.class)
public class CmsDaoImpl implements CmsDao {

	@ObjectClassDefinition
	public @interface Configuration {

		@AttributeDefinition(name = "Default workspace", description = "The name of the default workspace to use for JCR sessions")
		String defaultWorkspace() default CmsDaoImpl.DEFAULT_WORKSPACE;

	}
	
	protected static final String DEFAULT_WORKSPACE = "crx.default";

	private static final Logger LOG = LoggerFactory.getLogger(CmsDaoImpl.class);

	private String defaultWorkspace;

	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties());
		defaultWorkspace = dictionaryAdapter.getString("defaultWorkspace", DEFAULT_WORKSPACE);
	}

	@Override
	public <E> E run(final JCRSessionCallback<E> callback, JCRImporterConfiguration configuration) {
		if (DefaultingJCRSessionCallback.class.isInstance(callback)) {
			return runInternal((DefaultingJCRSessionCallback<E>) callback, configuration);
		} else {
			return runInternal(new DefaultingJCRSessionCallback<E>() {
				@Override
				public E getDefault() {
					return null;
				}

				@Override
				public E doInJCRSession(Session session) throws RepositoryException {
					return callback.doInJCRSession(session);
				}
			}, configuration);
		}
	}

	private <E> E runInternal(DefaultingJCRSessionCallback<E> callback, JCRImporterConfiguration cfg) {
		if (callback == null) {
			return null;
		}
		if (cfg == null) {
			return callback.getDefault();
		}

		String workspace = cfg.getWorkspace();
		workspace = workspace == null ? defaultWorkspace : workspace;
		String url = cfg.getUrl();

		Map<String, String> parameters = new HashMap<>();
		parameters.put(Spi2davexRepositoryServiceFactory.PARAM_REPOSITORY_URI, url);
		ServiceLoader<RepositoryFactory> res = ServiceLoader.load(RepositoryFactory.class);
		Iterator<RepositoryFactory> iter = res.iterator();
		List<RepositoryFactory> factories = new ArrayList<>();
		while (iter.hasNext()) {
			factories.add(iter.next());
		}
		for (RepositoryFactory repoFactory : factories) {
			
			try {
				Repository repository = repoFactory.getRepository(parameters);
				if (repository != null) {
					Session session = repository.login(cfg.getCredentials(), workspace);
					try {
						return callback.doInJCRSession(session);
					} catch (RepositoryException e) {
						LOG.error("failed to execute JCR session callback: " + e.getMessage(), e);
						return callback.getDefault();
					} finally {
						if (session != null) {
							session.logout();
						}
					}
					
				}
			} catch (Exception e) {
				LOG.error("failed to get a JCR repository session: " + e.getMessage(), e);
			}
		}
		LOG.error("could not perform JCR session callback, because no repository could be retrieved.");
		return callback.getDefault();
	}

}
