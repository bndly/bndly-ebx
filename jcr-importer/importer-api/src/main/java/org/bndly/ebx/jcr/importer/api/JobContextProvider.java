package org.bndly.ebx.jcr.importer.api;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-api
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

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public interface JobContextProvider {
	public static interface Callback<E> {
		E doWithContext(JobContext context);
	}
	public static interface VoidCallback {
		void doWithContext(JobContext context);
	}
	<E> E read(Callback<E> callback);
	<E> E write(Callback<E> callback);
	void newJobContext();
	/**
	 * access the job context to create a new one and continue work with the old one
	 * @param newContextCallback callback to be called with the newly created context
	 * @param oldContextCallback callback to be called with the old context (will not be accessible via 'read' anymore.
	 */
	void newJobContext(VoidCallback newContextCallback, VoidCallback oldContextCallback);
	
	/**
	 * access a new job context, that lives in memory. there will be no synchronization or persistence involved.
	 * @param callback the callback to invoke on the context
	 */
	void inMemoryContext(VoidCallback callback);
}
