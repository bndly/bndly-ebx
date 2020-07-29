package org.bndly.ebx.jcr.velocity;

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

import org.bndly.ebx.jcr.velocity.impl.ImporterConfigurationImpl;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.jcr.importer.api.CmsDao;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.jcr.importer.api.PreconfiguredCmsDao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import org.apache.jackrabbit.spi2davex.Spi2davexRepositoryServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = JCRDao.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(factory = true, ocd = CmsDaoImpl.Configuration.class)
public class CmsDaoImpl implements PreconfiguredCmsDao, JCRDao {

	@ObjectClassDefinition(
			name = "JCR DAO"
	)
	public @interface Configuration {
		@AttributeDefinition(name = "Name", description = "A name for this DAO, that can be used to reference it.")
		String name() default "default";
		
		@AttributeDefinition(name = "URL", description = "The URL of the JCR repository.")
		String url() default "http://localhost:8081/server";
		
		@AttributeDefinition(name = "User", description = "The user name for the login credentials")
		String user() default "admin";
		
		@AttributeDefinition(name = "Password", description = "The user password for the login credentials", type = AttributeType.PASSWORD)
		String password() default "admin";
		
		@AttributeDefinition(name = "Workspace", description = "The name of the JCR workspace to use.")
		String workspace() default "default";
		
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(CmsDaoImpl.class);
	private JCRImporterConfiguration configuration;
	private String defaultWorkspace = "default";

	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter da = new DictionaryAdapter(componentContext.getProperties());
		defaultWorkspace = da.getString("workspace", defaultWorkspace);
		configuration = new ImporterConfigurationImpl(da);
	}
	
	private <E> E run(DefaultingJCRSessionCallback<E> callback, JCRImporterConfiguration cfg) {
		if (callback == null) {
			return null;
		}
		if (cfg == null) {
			return callback.getDefault();
		}

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
					Session session = repository.login(cfg.getCredentials(), defaultWorkspace);
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

	@Override
	public <E> E run(final CmsDao.JCRSessionCallback<E> jcrsc) {
		return run(new DefaultingJCRSessionCallback<E>() {
			@Override
			public E getDefault() {
				return null;
			}

			@Override
			public E doInJCRSession(Session sn) throws RepositoryException {
				return jcrsc.doInJCRSession(sn);
			}
		});
	}

	@Override
	public <E> E run(DefaultingJCRSessionCallback<E> jcrsc) {
		return run(jcrsc, configuration);
	}

	@Override
	public JCRImporterConfiguration getConfiguration() {
		return configuration;
	}
	
}
