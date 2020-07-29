package org.bndly.ebx.jcr.velocity.datastore;

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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import org.apache.jackrabbit.JcrConstants;
import static org.mockito.Mockito.*;
import org.osgi.service.component.ComponentContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class JCRVelocityDataProviderTest {
	private final JCRVelocityDataProvider jcrVelocityDataProvider = new JCRVelocityDataProvider();
	private final TestJcrDao testJcrDao = new TestJcrDao();
	private final Dictionary<String, Object> properties = new Hashtable<>();
	private final ComponentContext componentContext = mock(ComponentContext.class);
	private final Session session = mock(Session.class);
	private final NodeType fileNodeType = mock(NodeType.class);
	
	private Node mockFileNode(String nodePath, String fileContentString) throws RepositoryException, UnsupportedEncodingException {
		final Node node = mock(Node.class);
		when(node.getPrimaryNodeType()).thenReturn(fileNodeType);
		when(node.hasProperty(Property.JCR_DATA)).thenReturn(Boolean.TRUE);
		Property dataProp = mock(Property.class);
		Binary binary = mock(Binary.class);
		byte[] fileContent = fileContentString.getBytes("UTF-8");
		when(binary.getStream()).thenReturn(new ByteArrayInputStream(fileContent));
		when(dataProp.getBinary()).thenReturn(binary);
		when(node.getProperty(Property.JCR_DATA)).thenReturn(dataProp);
		when(fileNodeType.getName()).thenReturn(JcrConstants.NT_FILE);
		when(session.getNode(nodePath)).thenReturn(node);
		return node;
	}
	
	private void mockMissingNode(String nodePath) throws RepositoryException {
		when(session.getNode(nodePath)).thenThrow(new PathNotFoundException(nodePath));
	}
	
	@BeforeClass
	public void setup() throws RepositoryException, UnsupportedEncodingException{
		jcrVelocityDataProvider.setJcrDao(testJcrDao);
		testJcrDao.setSession(session);
		when(componentContext.getProperties()).thenReturn(properties);
		properties.put("name", "jcr");
		properties.put("rootFolder", "/dam/templates");
		mockFileNode("/dam/templates/foo.vm", "helloworld");
		mockMissingNode("/dam/templates/foo-does-not-exist.vm");
		jcrVelocityDataProvider.activate(componentContext);
	}
	
	@Test
	public void testProvidingExistingResource() throws UnsupportedEncodingException {
		byte[] bytes = jcrVelocityDataProvider.getBytes("/jcr/foo.vm");
		Assert.assertNotNull(bytes);
		Assert.assertEquals(new String(bytes, "UTF-8"), "helloworld");
		
	}
	
	@Test
	public void testProvidingMissingResource() throws UnsupportedEncodingException {
		byte[] bytes = jcrVelocityDataProvider.getBytes("/jcr/foo-does-not-exist.vm");
		Assert.assertNull(bytes);
		
	}
}
