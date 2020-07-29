package org.bndly.ebx.jcr.importer.api.path;

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
public final class JCRPath {
	private final String path;
	private final String nodeName;
	private final boolean isRoot;
	private static final JCRPath ROOT = new JCRPath("/");

	public static JCRPath newInstance(String path) {
		return new JCRPath(path);
	}
	
	private JCRPath(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		isRoot = "/".equals(path);
		if (!isRoot) {
			path = cleanPath(path);
			nodeName = path.substring(path.lastIndexOf("/") + 1);
		} else {
			nodeName = null;
		}
		this.path = path;
	}
	
	static String cleanPath(String input) {
		boolean isAbsolute = input.startsWith("/");
		int prev = 0;
		int i = input.indexOf("/", prev);
		StringBuilder stringBuilder = new StringBuilder();
		while (true) {
			if (i - prev == 0) {
				// skip
			} else if (i == -1) {
				String trail = input.substring(prev);
				if (!"/".equals(trail)) {
					if (isAbsolute) {
						if (prev < input.length() - 1) {
							stringBuilder.append(trail);
						}
					} else {
						stringBuilder.append(trail);
					}
				}
				break;
			} else {
				String sub = input.substring(prev, i);
				if (!"/".equals(sub)) {
					stringBuilder.append(input.substring(prev, i));
				}
			}
			prev = i;
			i = input.indexOf("/", prev + 1);
		}
		String r = stringBuilder.toString();
		if (isAbsolute && r.isEmpty()) {
			return "/";
		}
		return r;
	}

	/**
	 * Method to escape filenames by replacing white spaces with underscores.
	 * @param input the string to be escaped
	 * @return the resulting string or null, if the provided {@code input} was null or empty.
	 */
	public static String escapeFileName(String input) {
		if (input == null || input.isEmpty()) {
			return null;
		}
		return input.replaceAll("\\s", "_");
	}
	
	public final JCRPath parent() {
		if (isRoot()) {
			return null;
		} else {
			int i = path.lastIndexOf("/");
			if (i == 0) {
				return ROOT;
			} else {
				return new JCRPath(path.substring(0, i));
			}
		}
	}

	public final boolean isRoot() {
		return isRoot;
	}

	public final JCRPath resolve(String relativePath) {
		String p = cleanPath(relativePath);
		if (p.isEmpty()) {
			return this;
		}
		if (!p.startsWith("/")) {
			p = "/" + p;
		}
		if (isRoot()) {
			return new JCRPath(p);
		} else {
			return new JCRPath(path + p);
		}
	}

	public final String nodeName() {
		return nodeName;
	}
	
	@Override
	public final String toString() {
		return path;
	}
	
}
