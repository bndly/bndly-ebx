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

import org.bndly.common.reflection.CompiledBeanPropertyAccessorWriter;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.model.Country;
import org.bndly.ebx.model.ImporterConfiguration;
import org.bndly.ebx.model.ImporterSupportedLanguage;
import org.bndly.ebx.model.PathConfiguration;
import org.bndly.schema.beans.ActiveRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class SchemaBasedJCRImporterConfiguration implements JCRImporterConfiguration {
	
	private static final Logger LOG = LoggerFactory.getLogger(SchemaBasedJCRImporterConfiguration.class);
	private static final CompiledBeanPropertyAccessorWriter CONFIG_PROPERTY_ACCESSOR = new CompiledBeanPropertyAccessorWriter(ImporterConfiguration.class);
	
	private final ImporterConfiguration importerConfiguration;

	public SchemaBasedJCRImporterConfiguration(ImporterConfiguration importerConfiguration) {
		this.importerConfiguration = importerConfiguration;
	}
	
	public void reloadConfig() {
		((ActiveRecord)importerConfiguration).reload();
	}
	
	public void disable() {
		importerConfiguration.setImportEnabled(false);
		((ActiveRecord)importerConfiguration).update();
	}

	public void enable() {
		importerConfiguration.setImportEnabled(true);
		((ActiveRecord)importerConfiguration).update();
	}

	public ImporterConfiguration getImporterConfiguration() {
		return importerConfiguration;
	}
	
	@Override
	public boolean isEnabled() {
		Boolean tmp = importerConfiguration.getImportEnabled();
		return tmp == null ? false : tmp;
	}

	@Override
	public Credentials getCredentials() {
		String user = importerConfiguration.getCmsUser();
		String password = importerConfiguration.getCmsPassword();
		SimpleCredentials creds = new SimpleCredentials(user, password.toCharArray());
		return creds;
	}

	@Override
	public String getUrl() {
		return importerConfiguration.getUrl();
	}

	@Override
	public List<Path> getPaths() {
		List<PathConfiguration> tmp = importerConfiguration.getPaths();
		if (tmp == null || tmp.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List<Path> r = new ArrayList<>(tmp.size());
		for (final PathConfiguration pathConfiguration : tmp) {
			r.add(new Path() {
				@Override
				public String getName() {
					return pathConfiguration.getName();
				}

				@Override
				public String getLanguage() {
					return pathConfiguration.getContentLanguage();
				}

				@Override
				public String getJCRNodePath() {
					return pathConfiguration.getCmsPath();
				}
			});
		}
		return r;
	}

	@Override
	public String getName() {
		return importerConfiguration.getName();
	}

	@Override
	public List<String> getSupportedLanguages() {
		List<ImporterSupportedLanguage> supported = importerConfiguration.getSupportedLanguages();
		if (supported == null || supported.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List<String> locales = new ArrayList<>();
		for (ImporterSupportedLanguage lang : supported) {
			String locale = lang.getLanguage().getName();
			Country country = lang.getCountry();
			if (country != null) {
				String isoCode2 = country.getIsoCode2();
				if (isoCode2 != null) {
					locale += "-" + isoCode2;
				}
			}
			locales.add(locale);
		}
		return locales;
	}

	@Override
	public String getWorkspace() {
		return null; // TODO: add a property 'workspace'
	}

	@Override
	public Boolean getBoolean(String configKey) {
		Object val = CONFIG_PROPERTY_ACCESSOR.get(configKey, importerConfiguration);
		if (Boolean.class.isInstance(val)) {
			return (Boolean) val;
		}
		return null;
	}
	
}
