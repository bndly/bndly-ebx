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

import org.bndly.common.crypto.api.Base64Service;
import org.bndly.common.json.model.JSObject;
import org.bndly.common.json.parsing.JSONParser;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.jcr.importer.api.JCRImporterConfiguration;
import org.bndly.ebx.jcr.importer.api.PublicationService;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import javax.jcr.Credentials;
import javax.jcr.GuestCredentials;
import javax.jcr.SimpleCredentials;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
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
@Component(service = PublicationService.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(factory = true, ocd = SlingPublicationServiceImpl.Configuration.class)
public class SlingPublicationServiceImpl implements PublicationService {

	@ObjectClassDefinition(
			name = "Sling Publication Service"
	)
	public @interface Configuration {
		@AttributeDefinition(name = "Name", description = "The name of the publication service, that can be used to reference it.")
		String name();
		
		@AttributeDefinition(name = "URL", description = "The URL of the Sling resource, that shall receive the paths to be replicated/published.")
		String url();
		
		@AttributeDefinition(name = "HTTP Client", description = "The filter expression to get the HTTP client to communicate with Sling.")
		String httpClient_target();
		
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(SlingPublicationServiceImpl.class);
	
	@Reference(name = "httpClient")
	private HttpClient httpClient;
	
	@Reference
	private Base64Service base64Service;
	
	private String url;

	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(componentContext.getProperties());
		url = dictionaryAdapter.getString("url");
	}
	
	private static class PublicationResponse {
		private final boolean success;
		private final String message;
		private final String state;

		public PublicationResponse(boolean success, String message, String state) {
			this.success = success;
			this.message = message;
			this.state = state;
		}

		public boolean isSuccess() {
			return success;
		}

		public String getMessage() {
			return message;
		}

		public String getState() {
			return state;
		}
		
	}
	
	@Override
	public void doPublish(JCRImporterConfiguration configuration, final Iterable<String> paths) {
		// POST the paths to replicate on a specific URL and sling will do the REST (haha)
		HttpPost post = new HttpPost(url);
		Credentials credentials = configuration.getCredentials();
		String userName;
		String password;
		if (SimpleCredentials.class.isInstance(credentials)) {
			SimpleCredentials sc = ((SimpleCredentials) credentials);
			userName = sc.getUserID();
			char[] passwordChars = sc.getPassword();
			password = passwordChars == null ? "" : new String(passwordChars);
		} else if (GuestCredentials.class.isInstance(credentials)) {
			GuestCredentials gc = ((GuestCredentials) credentials);
			userName = null;
			password = null;
		} else {
			LOG.warn("no credentials in provided by the JCR configuration. Falling back to GuestCredentials");
			userName = null;
			password = null;
		}
		/*
		request:
		Content-Type:application/x-www-form-urlencoded
		action:ADD
		path:/content/de/about-swyp
		path:/content/de/about-swyp/jcr:content
		path:/content/de/about-swyp/jcr:content/mainContent
		path:/content/de/about-swyp/jcr:content/mainContent/welcomeText
		
		response:
		Content-Type:application/json
		{"message":"an unexpected error has occurred"}
		*/
		try {
			if (userName != null) {
				byte[] bytes = (userName + ":" + password).getBytes("UTF-8");
				post.addHeader("Authorization", "Basic " + base64Service.base64Encode(bytes));
			}
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			Iterable<? extends NameValuePair> parameters = new Iterable<NameValuePair>() {
				@Override
				public Iterator<NameValuePair> iterator() {
					final Iterator<String> pathIter = paths.iterator();
					return new Iterator<NameValuePair>() {
						boolean didAdd = false;

						@Override
						public boolean hasNext() {
							if (!didAdd) {
								return true;
							}
							return pathIter.hasNext();
						}

						@Override
						public NameValuePair next() {
							if (!didAdd) {
								didAdd = true;
								return new BasicNameValuePair("action", "ADD");
							}
							String path = pathIter.next();
							return new BasicNameValuePair("path", path);
						}

						@Override
						public void remove() {
							// no-op
						}
						
					};
				}
			};
			UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(parameters, Charset.forName("UTF-8"));
			post.setEntity(requestEntity);
			PublicationResponse response = httpClient.execute(post, new ResponseHandler<PublicationResponse>() {
				@Override
				public PublicationResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					HttpEntity responseEntity = response.getEntity();
					try {
						boolean isJson = responseEntity.getContentType() != null && responseEntity.getContentType().getValue().startsWith("application/json");
						String charset = "UTF-8";
						Header encoding = responseEntity.getContentEncoding();
						if (encoding != null) {
							charset = encoding.getValue();
						}
						LOG.debug("publication returned status code {}", response.getStatusLine().getStatusCode());
						if (response.getStatusLine().getStatusCode() == 202 && isJson) {
							// in case of success: STATUS 202 "{"success":true,"state":"ACCEPTED","message":"[QUEUED]"}"
							JSObject parsed = (JSObject) new JSONParser().parse(responseEntity.getContent(), charset);
							return new PublicationResponse(true, parsed.getMemberStringValue("message"), parsed.getMemberStringValue("state"));
						} else if (response.getStatusLine().getStatusCode() == 503 && isJson) {
							// in case of failure:  STATUS 503 "{"message":"an unexpected error has occurred"}
							JSObject parsed = (JSObject) new JSONParser().parse(responseEntity.getContent(), charset);
							return new PublicationResponse(false, parsed.getMemberStringValue("message"), null);
						} else {
							// should be a failure
							return new PublicationResponse(false, EntityUtils.toString(responseEntity, charset), null);
						}
					} finally {
						if (responseEntity != null) {
							EntityUtils.consumeQuietly(responseEntity);
						}
					}
				}
			});
			if (response.isSuccess()) {
				LOG.debug("successfully triggered publication");
			} else {
				LOG.warn("failed to trigger publication: {}", response.getMessage());
			}
		} catch (IOException e) {
			LOG.error("failed to publish: " + e.getMessage(), e);
		}
	}
	
}
