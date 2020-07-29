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

import org.bndly.ebx.jcr.velocity.JCRDao;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class TestJcrDao implements JCRDao {
	private Session session;
	
	@Override
	public <E> E run(DefaultingJCRSessionCallback<E> callback) {
		try {
			return callback.doInJCRSession(session);
		} catch (RepositoryException ex) {
			return callback.getDefault();
		}
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
