package org.bndly.ebx.dataprovider;

/*-
 * #%L
 * org.bndly.ebx.data-provider
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

import org.bndly.common.data.io.IOUtils;
import org.bndly.common.data.io.ReplayableInputStream;
import org.bndly.common.velocity.api.VelocityDataProvider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = VelocityDataProvider.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = FileVelocityDataProvider.Configuration.class)
public class FileVelocityDataProvider implements VelocityDataProvider {

	@ObjectClassDefinition
	public @interface Configuration {
		@AttributeDefinition(
				name = "Data root",
				description = "The path to the data root on the file system"
		)
		String root() default "";
		
		@AttributeDefinition(
				name = "Name",
				description = ""
		)
		String name() default "The name of this velocity data provider to use as a prefix";
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(FileVelocityDataProvider.class);
	
	private String root;
	private String name;
	
	@Activate
	public void activate(Configuration configuration) {
		name = configuration.name();
		root = configuration.root();
	}
	
	@Deactivate
	public void deactivate() {
		name = null;
		root = null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getBytes(String sourceName) {
		ReplayableInputStream stream = getStream(sourceName);
		if (stream == null) {
			return null;
		}
		try {
			return IOUtils.read(stream);
		} catch (IOException ex) {
			throw new IllegalStateException("failed to copy  " + sourceName + ": " + ex.getMessage(), ex);
		}
	}

	@Override
	public boolean exists(String sourceName) {
		Path r = getResource(sourceName);
		return Files.exists(r) && Files.isRegularFile(r);
	}

	@Override
	public ReplayableInputStream getStream(String sourceName) {
		Path r = getResource(sourceName);
		if (Files.exists(r)) {
			try {
				InputStream is = Files.newInputStream(r, StandardOpenOption.READ);
				return ReplayableInputStream.newInstance(is);
			} catch (IOException ex) {
				LOG.warn("failed to retrieve inputstream of " + r, ex);
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public long getLastModified(String sourceName) {
		Path r = getResource(sourceName);
		if (Files.exists(r)) {
			try {
				FileTime lmt = Files.getLastModifiedTime(r);
				return lmt.toMillis();
			} catch (IOException ex) {
				LOG.warn("failed to retrieve last modified date of " + r, ex);
				return -1;
			}
		} else {
			return -1;
		}
	}

	@Override
	public boolean isModified(String sourceName, long lastModified) {
		return getLastModified(sourceName) > lastModified;
	}

	private Path getResource(String sourceName) {
		Path path = buildPath(sourceName);
		return path;
	}

	private Path buildPath(String sourceName) {
		Path rootPath = Paths.get(root);
		Path path = rootPath;
		StringBuffer sb = null;
		for (int i = 0; i < sourceName.length(); i++) {
			char c = sourceName.charAt(i);
			if (c != '/') {
				if (sb != null) {
					sb.append(c);
				} else {
					sb = new StringBuffer().append(c);
				}
			} else {
				if (sb != null) {
					path = path.resolve(sb.toString());
					sb = null;
				}
			}
		}
		if (sb != null) {
			path = path.resolve(sb.toString());
		}
		return path;
	}

	public void setRoot(String root) {
		this.root = root;
	}

}
