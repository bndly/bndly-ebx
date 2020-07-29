package org.bndly.ebx.resources.custom;

/*-
 * #%L
 * org.bndly.ebx.resources
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
import org.bndly.ebx.model.Property;
import org.bndly.ebx.model.PropertySet;
import org.bndly.ebx.model.Translation;
import org.bndly.rest.api.ContentType;
import org.bndly.rest.api.Context;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.Parameter;
import org.bndly.rest.beans.ebx.LanguageRestBean;
import org.bndly.rest.beans.ebx.PropertySetRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.Documentation;
import org.bndly.rest.controller.api.DocumentationResponse;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.api.services.QueryByExample;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Path("")
@Component(service = PropertiesFilePropertySetResource.class, immediate = true)
public class PropertiesFilePropertySetResource {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;
	private static final ContentType PROPERTIES_CONTENT_TYPE = new ContentType() {

		@Override
		public String getName() {
			return "text/x-java-properties";
		}

		@Override
		public String getExtension() {
			return "properties";
		}
	};

	@Activate
	public void activate() {
		controllerResourceRegistry.deploy(this);
	}

	@Deactivate
	public void deactivate() {
		controllerResourceRegistry.undeploy(this);
	}
	
	@GET
	@Path("PropertySet/{propertySetName}.properties")
	@AtomLink(rel = "properties",target = PropertySetRestBean.class,parameters = {
		@Parameter(name = "propertySetName",expression = "${this.name}")
	})
	@Documentation(
			authors = "bndly@cybercon.de",
			value = "returns a java properties file with the properties taken from the provided property set name",
			produces = "text/x-java-properties",
			responses = @DocumentationResponse(description = "java properties file")
	)
	public Response downloadPropertiesFile(@PathParam("propertySetName") String propertySetName, @Meta Context context) {
		Engine engine = schemaBeanFactory.getEngine();
		RecordContext ctx = engine.getAccessor().buildRecordContext();
		QueryByExample qbe = engine.getAccessor().queryByExample(PropertySet.class.getSimpleName(), ctx);
		qbe.attribute("name", propertySetName);
		Record set = qbe.eager().single();
		StringWriter sw = new StringWriter();
		if(set != null) {
			PropertySet ps = schemaBeanFactory.getSchemaBean(PropertySet.class, set);
			List<Property> properties = ps.getProperties();
			if(properties != null) {
				for (Property property : properties) {
					sw
						.append(encodeForProperties(property.getName()))
						.append(" = ")
						.append(encodeForProperties(property.getActualValue()))
						.append("\n");
				}
			}
		}
		sw.flush();
		byte[] utf8bytes;
		try {
			utf8bytes = sw.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
		ByteArrayInputStream is = new ByteArrayInputStream(utf8bytes);
		context.setOutputContentType(PROPERTIES_CONTENT_TYPE, "UTF-8");
		return Response.ok(is);
	}
	
	@GET
	@Path("Translation/language/{lang}.properties")
	@AtomLink(rel = "properties",target = LanguageRestBean.class,parameters = {
		@Parameter(name = "lang",expression = "${this.name}")
	})
	@Documentation(
			authors = "bndly@cybercon.de",
			value = "returns a java properties file with the translation objects in the provided language",
			produces = "text/x-java-properties",
			responses = @DocumentationResponse(description = "java properties file")
	)
	public Response downloadTranslationsAsPropertiesFile(@PathParam("lang") String lang, @Meta Context context) {
		Engine engine = schemaBeanFactory.getEngine();
		RecordContext ctx = engine.getAccessor().buildRecordContext();
		QueryByExample qbe = engine.getAccessor().queryByExample(Language.class.getSimpleName(), ctx);
		qbe.attribute("name", lang);
		Record language = qbe.eager().single();
		StringWriter sw = new StringWriter();
		if(language != null) {
			qbe = engine.getAccessor().queryByExample(Translation.class.getSimpleName(), ctx);
			qbe.attribute("language", language);
			qbe.eager();
			List<Record> translations = qbe.all();
			if(translations != null) {
				for (Record translation : translations) {
					Translation t = schemaBeanFactory.getSchemaBean(Translation.class, translation);
					sw
						.append(encodeForProperties(t.getTranslatedObject().getTranslationKey()))
						.append(" = ")
						.append(encodeForProperties(t.getStringValue()))
						.append("\n");
				}
			}
		}
		sw.flush();
		byte[] utf8bytes;
		try {
			utf8bytes = sw.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
		ByteArrayInputStream is = new ByteArrayInputStream(utf8bytes);
		context.setOutputContentType(PROPERTIES_CONTENT_TYPE, "UTF-8");
		return Response.ok(is).header("Content-Type", "text/x-java-properties");
	}

	private String encodeForProperties(String input) {
		if(input == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if("#!=:".indexOf(c) > -1) {
				sb.append('\\').append(c);
			} else {
				sb.append(c);
			}
			
		}
		return sb.toString();
	}
}
