package org.bndly.ebx.translation.generator;

/*-
 * #%L
 * org.bndly.ebx.translation-generator
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

import org.bndly.ebx.model.Language;
import org.bndly.ebx.model.TranslatedObject;
import org.bndly.ebx.model.Translation;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service= PropertiesFilesToTranslationsGeneratorImpl.class, immediate = true)
@Designate(ocd = PropertiesFilesToTranslationsGeneratorImpl.Configuration.class)
public class PropertiesFilesToTranslationsGeneratorImpl {

	@ObjectClassDefinition
	public @interface Configuration {

		@AttributeDefinition(
				name = "file names",
				description = "list of property files containing translations"
		)
		String[] propertiesFileNames();

		@AttributeDefinition(
				name = "file location",
				description = "location where the property files are stored"
		)
		String propertyFileLocation() default "";
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesFilesToTranslationsGeneratorImpl.class);

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	private String[] propertiesFileNames;
	private String propertyFileLocation;

	private Engine getEngine() {
		return schemaBeanFactory.getEngine();
	}

	public void insertTranslations() {
		LOG.info("inserting translations");
		if (propertiesFileNames == null || propertyFileLocation == null) {
			LOG.warn("translation file name list or translation file location was null");
			return;
		}
		String[] tmp = new String[propertiesFileNames.length];
		for (int i = 0; i < this.propertiesFileNames.length; i++) {
			String space = "";
			if (!propertyFileLocation.endsWith(File.separator)) {
				space = File.separator;
			}
			tmp[i] = propertyFileLocation + space + this.propertiesFileNames[i];
		}
		final Map<String, TranslatedObject> translatedObjectsMap = new HashMap<>();
		final Map<String, Language> languagesMap = new HashMap<>();
		final Accessor accessor = getEngine().getAccessor();
		final RecordContext ctx = accessor.buildRecordContext();
		new PropertyFileTranslationIterator(new Locale[]{Locale.GERMAN, Locale.ENGLISH}, tmp).traverse(new PropertyFileTranslationIteratorListener() {
			@Override
			public void onVisit(Locale locale, String bundleName, String key, String value) {
				TranslatedObject translatedObject;
				Iterator<Record> res = accessor.query("PICK " + TranslatedObject.class.getSimpleName() + " t IF t.translationKey=? LIMIT ?", ctx, null, key, 1);
				if (res.hasNext()) {
					LOG.info("found existing TranslatedObject for key {}", key);
					Record translatedObjectRecord = res.next();
					translatedObject = schemaBeanFactory.getSchemaBean(TranslatedObject.class, translatedObjectRecord);
				} else {
					LOG.info("creating new TranslatedObject for key {}", key);
					translatedObject = schemaBeanFactory.getSchemaBean(TranslatedObject.class, ctx.create(TranslatedObject.class.getSimpleName()));
					translatedObject.setTranslationKey(key);
					((ActiveRecord) translatedObject).persist();
				}
				translatedObjectsMap.put(key, translatedObject);

				String lang = locale.getLanguage();
				Language language;
				Iterator<Record> langRes = accessor.query("PICK " + Language.class.getSimpleName() + " l IF l.name=? LIMIT ?", ctx, null, lang, 1);
				if (langRes.hasNext()) {
					LOG.info("found existing Language for code {}", lang);
					language = schemaBeanFactory.getSchemaBean(Language.class, langRes.next());
				} else {
					LOG.info("creating new Language for code {}", lang);
					language = schemaBeanFactory.getSchemaBean(Language.class, ctx.create(Language.class.getSimpleName()));
					language.setName(lang);
					((ActiveRecord) language).persist();
				}
				languagesMap.put(lang, language);

				Long count = accessor.count(
						"COUNT " + Translation.class.getSimpleName() + " t IF t.language.id=? AND t.translatedObject.id=?", 
						((ActiveRecord) language).getId(), 
						((ActiveRecord) translatedObject).getId()
				);
				if (count == null || count < 1) {
					LOG.info("inserting value {} for key {} in language {}", new Object[]{value, key, locale.getLanguage()});
					Translation t = schemaBeanFactory.getSchemaBean(Translation.class, ctx.create(Translation.class.getSimpleName()));
					t.setLanguage(language);
					t.setTranslatedObject(translatedObject);
					t.setStringValue(value);
					((ActiveRecord) t).persist();
				} else {
					LOG.info("skipping insertion of value {} for key {} in language {}", new Object[]{value, key, locale.getLanguage()});
				}
			}
		});
		LOG.info("inserted translations");
	}

	@Activate
	public void activate(Configuration configuration) {
		propertiesFileNames = configuration.propertiesFileNames();
		propertyFileLocation = configuration.propertyFileLocation();
		if (schemaBeanFactory != null && schemaBeanFactory.getEngine().getDeployer().getDeployedSchema().getName().equals("ebx")) {
			insertTranslations();
		} else {
			LOG.warn("properties could not be inserted as translations, because there was no schema bean factory for the ebx schema available.");
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

	public void setPropertiesFileNames(String propertiesFileNames) {
		this.propertiesFileNames = propertiesFileNames.split(",");
	}

	public void setPropertyFileLocation(String propertyFileLocation) {
		if (!propertyFileLocation.endsWith("/")) {
			propertyFileLocation += "/";
		}
		this.propertyFileLocation = propertyFileLocation;
	}

	public void setSchemaDeploymentListeners(List schemaDeploymentListeners) {
		schemaDeploymentListeners.add(this);
	}

}
