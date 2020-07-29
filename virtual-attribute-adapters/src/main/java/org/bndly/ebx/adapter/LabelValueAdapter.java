package org.bndly.ebx.adapter;

/*-
 * #%L
 * org.bndly.ebx.virtual-attribute-adapters
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

import org.bndly.ebx.model.TranslatedObject;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.ContextProvider;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.model.Attribute;
import org.bndly.schema.model.NamedAttributeHolder;
import org.bndly.schema.model.Schema;
import org.bndly.schema.model.StringAttribute;
import org.bndly.schema.model.Type;
import org.bndly.schema.model.TypeAttribute;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
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
@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = LabelValueAdapter.Configuration.class)
public class LabelValueAdapter implements VirtualAttributeAdapter<StringAttribute> {

	@ObjectClassDefinition
	public @interface Configuration {
		@AttributeDefinition(
				name = "bindings",
				description = "Definition of where which attribute is being taken as the key for resolving the label for an entity type"
		)
		String[] bindings() default {
			"Country.name",
			"CreditCardBrand.name",
			"PersonTitle.name",
			"ProductAttribute.name",
			"ProductAttributeValue.stringValue",
			"Salutation.name",
			"ShipmentMode.name",
			"WishListItemPriority.name",
			"WishListPrivacy.name"
		};
		
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(LabelValueAdapter.class);
	@Reference
	private ContextProvider contextProvider;
	@Reference(target = "(service.pid=org.bndly.schema.api.services.Engine.ebx)")
	private Engine engine;
	
	private static final String BINDING_PREFIX = "binding.";
	private final Map<String, String> entityToLabelAttribute = new HashMap<>();
	
	@Activate
	public void activate(ComponentContext componentContext) {
		Dictionary<String, Object> props = componentContext.getProperties();
		if(props != null) {
			Object bindings = props.get("bindings");
			if (bindings != null) {
				if (String[].class.isInstance(bindings)) {
					String[] bindingsarray = (String[]) bindings;
					for (int i = 0; i < bindingsarray.length; i++) {
						String binding = bindingsarray[i];
						String[] tmp = binding.split(",");
						for (String string : tmp) {
							int splitAt = string.indexOf(".");
							if (splitAt > -1) {
								String entity = binding.substring(0, splitAt);
								String attribute = binding.substring(splitAt + 1);
								entityToLabelAttribute.put(entity, attribute);
							}
						}
					}
				}
			}
			Enumeration<String> keys = props.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				Object value = props.get(key);
				if (value != null && String.class.isInstance(value)) {
					if (key.startsWith(BINDING_PREFIX)) {
						String entity = key.substring(BINDING_PREFIX.length());
						String attribute = (String) value;
						entityToLabelAttribute.put(entity, attribute);
					}
				}
			}
		}
		engine.getVirtualAttributeAdapterRegistry().register(this);
	}
	
	@Deactivate
	public void deactivate() {
		engine.getVirtualAttributeAdapterRegistry().unregister(this);
	}
	
	@Override
	public boolean supports(StringAttribute attribute, NamedAttributeHolder holder) {
		return "label".equals(attribute.getName());
	}

	@Override
	public Object read(StringAttribute attribute, Record r) {
		Context ctx = contextProvider.getCurrentContext();
		if (ctx == null) {
			return null;
		}
		Locale locale = ctx.getLocale();
		if (locale == null) {
			return null;
		}
		String lang = locale.getLanguage();
		if (lang == null || "".equals(lang)) {
			return null;
		}
		Type type = r.getType();
		
		// special treatment, if the type is TranslatedObject
		if (TranslatedObject.class.getSimpleName().equals(type.getName())) {
			Long id = r.getId();
			if (id == null) {
				return null;
			}
			String key = r.getAttributeValue("translationKey", String.class);
			LOG.debug("token: [{}] {} ", lang, key);
			String stringValue = cleanStringValueOfTranslation("PICK Translation t IF t.translatedObject.id=? AND t.language.name=?", id, lang);
			LOG.debug("token: [{}] {} -> {}", new Object[]{lang, key, stringValue});
			return stringValue;
		}
		
		String nameOfTheAttribute = entityToLabelAttribute.get(type.getName());
		if(nameOfTheAttribute == null) {
			return null;
		}
		if(!r.isAttributePresent(nameOfTheAttribute)) {
			return null;
		}
		
		Attribute att = r.getAttributeDefinition(nameOfTheAttribute);
		boolean isStringAttribute = StringAttribute.class.isInstance(att);
		boolean isTypeAttribute = TypeAttribute.class.isInstance(att) && TranslatedObject.class.getSimpleName().equals(((TypeAttribute)att).getType().getName());
		
		if(isStringAttribute) {
			Schema schema = type.getSchema();
			String key = schema.getName() + "." + type.getName();
			String stringValueOfTheAttribute = r.getAttributeValue(att.getName(), String.class);
			if(stringValueOfTheAttribute == null) {
				stringValueOfTheAttribute = "";
			}
			key += "." + nameOfTheAttribute + "." + stringValueOfTheAttribute;
			
			LOG.debug("token: [{}] {} ", lang, key);
			String stringValue = cleanStringValueOfTranslation("PICK Translation t IF t.translatedObject.translationKey=? AND t.language.name=?", key, lang);
			LOG.debug("token: [{}] {} -> {}", new Object[]{lang, key, stringValue});
			return stringValue;
		} else if(isTypeAttribute) {
			// TranslatedObject should be supported here
			Object value = r.getAttributeValue(att.getName());
			if(value == null) {
				return null;
			}
			Long idOfTranslatedObject = null;
			if(Record.class.isInstance(value)) {
				Record rValue = (Record) value;
				idOfTranslatedObject = rValue.getId();
			} else if(Long.class.isInstance(value)) {
				idOfTranslatedObject = (Long) value;
			}
			if(idOfTranslatedObject == null) {
				return null;
			}
			
			LOG.debug("token: [{}] TranslatedObject id={} ", lang, idOfTranslatedObject);
			String stringValue = cleanStringValueOfTranslation("PICK Translation t IF t.translatedObject.id=? AND t.language.name=?", idOfTranslatedObject, lang);
			LOG.debug("token: [{}] TranslatedObject id={} -> {}", new Object[]{lang, idOfTranslatedObject, stringValue});
			return stringValue;
		} else {
			return null;
		}
	}

	private String cleanStringValueOfTranslation(String nQuery, Object... args) {
		Iterator<Record> queryResult = engine.getAccessor().query(nQuery, args);
		String stringValue = null;
		if(queryResult.hasNext()) {
			stringValue = queryResult.next().getAttributeValue("stringValue", String.class);
		}
		if(stringValue == null) {
			stringValue = "";
		}
		return stringValue;
	}

	@Override
	public void write(StringAttribute attribute, Record r, Object value) {
	}

	public void setContextProvider(ContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

}
