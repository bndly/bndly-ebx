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

import org.bndly.common.data.io.ReplayableInputStream;
import org.bndly.rest.api.ContentType;
import org.bndly.rest.common.beans.ListRestBean;
import org.bndly.rest.controller.api.EntityParser;
import org.bndly.rest.jaxb.context.GlobalJaxbContextProvider;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = EntityParser.class, immediate = true)
public class ListMessageBodyReader implements EntityParser {

	@Reference
	private GlobalJaxbContextProvider jaxbContextProvider;

	public void setJaxbContextProvider(GlobalJaxbContextProvider jaxbContextProvider) {
		this.jaxbContextProvider = jaxbContextProvider;
	}

	@Override
	public ContentType getSupportedContentType() {
		return ContentType.XML;
	}

	@Override
	public Object parse(ReplayableInputStream in, Class<?> requiredType) throws IOException {
		JAXBContext ctx = jaxbContextProvider.getContextObject(JAXBContext.class);
		try {
			in = ReplayableInputStream.replayIfPossible(in);
			return (ListRestBean) ctx.createUnmarshaller().unmarshal(in);
		} catch (JAXBException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
