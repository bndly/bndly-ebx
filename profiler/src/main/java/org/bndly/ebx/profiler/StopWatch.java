package org.bndly.ebx.profiler;

/*-
 * #%L
 * org.bndly.ebx.profiler
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
public class StopWatch {
	private Long s;
	private Long e;
	public void start() {
		s = System.currentTimeMillis();
	}
	public void stop() {
		e = System.currentTimeMillis();
	}
	public long getTotalTimeMillis() {
		if(s == null) {
			return 0;
		}
		Long end = e;
		if(end == null) {
			end = System.currentTimeMillis();
		}
		return e-s;
	}
	
}
