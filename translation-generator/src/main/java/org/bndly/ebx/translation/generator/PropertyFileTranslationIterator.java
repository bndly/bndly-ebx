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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyFileTranslationIterator {

	private static final Logger LOG = LoggerFactory.getLogger(PropertyFileTranslationIterator.class);

	private final List<Locale> supportedLocales;
	private final List<String> bundleNames;

	public PropertyFileTranslationIterator(Locale[] locales, String[] bundles) {
		supportedLocales = Arrays.asList(locales);
		bundleNames = Arrays.asList(bundles);
	}

	public PropertyFileTranslationIterator(List<Locale> locales, List<String> bundles) {
		supportedLocales = locales;
		bundleNames = bundles;
	}

	public void traverse(PropertyFileTranslationIteratorListener listener) {
		for (Locale locale : supportedLocales) {
			for (String bundleName : bundleNames) {
				File file = new File(bundleName + "_" + locale.getLanguage() + ".properties");
				if (file.exists()) {
					PropertyResourceBundle bundle;
					try {
						FileInputStream is = new FileInputStream(file);
						InputStreamReader reader = new InputStreamReader(is, "UTF-8");
						bundle = new PropertyResourceBundle(reader);
					} catch (IOException ex) {
						LOG.error("could not open bundle: {}", bundleName, ex);
						continue;
					}
					Enumeration bundleKeys = bundle.getKeys();

					while (bundleKeys.hasMoreElements()) {
						String key = (String) bundleKeys.nextElement();
						String value = bundle.getString(key);
						listener.onVisit(locale, bundleName, key, value);
					}
				}
			}
		}
	}
}
