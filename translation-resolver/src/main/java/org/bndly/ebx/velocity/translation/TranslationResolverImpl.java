package org.bndly.ebx.velocity.translation;

/*-
 * #%L
 * org.bndly.ebx.translation-resolver
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

import org.bndly.common.velocity.api.TranslationResolver;
import org.bndly.ebx.model.Translation;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.services.Engine;
import java.util.Iterator;
import java.util.Locale;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = TranslationResolver.class, immediate = true)
public class TranslationResolverImpl implements TranslationResolver {

	@Reference(target = "(service.pid=org.bndly.schema.api.services.Engine.ebx)")
	private Engine engine;

	@Override
	public String resolveTranslation(String input, Locale locale) {
		if (input == null) {
			return "";
		}
		if (locale == null) {
			return input + " (MISSING LOCALE)";
		}
		Iterator<Record> res = engine.getAccessor().query("PICK " + Translation.class.getSimpleName() + " t IF t.language.name=? AND t.translatedObject.translationKey=? LIMIT ?", locale.getLanguage(), input, 1);
		if (res.hasNext()) {
			return res.next().getAttributeValue("stringValue", String.class);
		} else {
			return input;
		}
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

}
