package org.bndly.ebx.jcr.velocity.impl;

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

import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = JCRImporterConfiguration.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(factory = true, ocd = ImporterConfigurationImpl.Configuration.class)
public class ImporterConfigurationImpl implements JCRImporterConfiguration {

	@ObjectClassDefinition(
			name = "JCR Importer Configuration"
	)
	public @interface Configuration {

		@AttributeDefinition(name = "Name", description = "The name of this configuration, that can be used for lookups.")
		String name() default "default";

		@AttributeDefinition(name = "Enabled", description = "Marks this configuration as enabled.")
		boolean enabled() default true;

		@AttributeDefinition(name = "Login user", description = "The login user name for the JCR.")
		String user() default "admin";

		@AttributeDefinition(name = "Login password", description = "The login user password for the JCR.", type = AttributeType.PASSWORD)
		String password() default "admin";

		@AttributeDefinition(name = "Languages", description = "The languages, that should be supported/iterated by the importer")
		String[] languages() default {"de", "en"};

		@AttributeDefinition(
				name = "Paths",
				description = "Named paths in the JCR, that will be used to avoid redundant configuration of such paths. The name of the path, the language of the path and the path itself are separated by a : symbol. NAME:LANGUAGE:PATH"
		)
		String[] paths();

		@AttributeDefinition(name = "URL", description = "The URL of the JCR Webdav servlet.")
		String url() default "http://localhost:8081/server";

	}
	
	public static final String NAME = "name";
	public static final String ACTIVE = "active";
	public static final String ENABLED = "enabled";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String LANGUAGES = "languages";
	public static final String PATHS = "paths";
	public static final String URL = "url";
	public static final String EXPORT_MASTERS = "exportMasters";
	public static final String EXPORT_MARKETING_PRODUCTS = "exportMarketingProducts";
	public static final String EXPORT_CHANNELS = "exportChannels";

	private DictionaryAdapter dictionaryAdapter;

	public ImporterConfigurationImpl() {
	}

	public ImporterConfigurationImpl(DictionaryAdapter dictionaryAdapter) {
		this.dictionaryAdapter = dictionaryAdapter;
	}

	@Activate
	public void activate(ComponentContext componentContext) {
		dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties());
	}

	@Override
	public String getName() {
		return dictionaryAdapter.getString(NAME, "default");
	}

	@Override
	public String getUrl() {
		return dictionaryAdapter.getString(URL, "http://localhost:8081/server");
	}

	@Override
	public boolean isEnabled() {
		return dictionaryAdapter.getBoolean(ENABLED, true);
	}

	@Override
	public Credentials getCredentials() {
		return new SimpleCredentials(dictionaryAdapter.getString(USER, "admin"), dictionaryAdapter.getString(PASSWORD, "admin").toCharArray());
	}

	@Override
	public String getWorkspace() {
		return null;
	}

	@Override
	public List<Path> getPaths() {
		Collection<String> r = dictionaryAdapter.getStringCollection(PATHS);
		if (r == null || r.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List<Path> res = new ArrayList<>();
		for (String string : r) {
			int i = string.indexOf(":");
			int j = string.indexOf(":", i + 1);
			if (i > 0 && j > i) {
				final String name = string.substring(0, i);
				final String language = string.substring(i + 1, j);
				final String path = string.substring(j + 1);
				res.add(new Path() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public String getLanguage() {
						return language.isEmpty() ? null : language;
					}

					@Override
					public String getJCRNodePath() {
						return path;
					}
				});
			}
		}
		return res;
	}

	@Override
	public List<String> getSupportedLanguages() {
		Collection<String> r = dictionaryAdapter.getStringCollection(LANGUAGES, "de", "en");
		if (r == null || r.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		return new ArrayList<>(r);
	}

	@Override
	public Boolean getBoolean(String string) {
		return dictionaryAdapter.getBoolean(string);
	}

}
