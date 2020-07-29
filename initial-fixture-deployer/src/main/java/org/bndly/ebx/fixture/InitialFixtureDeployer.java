package org.bndly.ebx.fixture;

/*-
 * #%L
 * org.bndly.ebx.initial-fixture-deployer
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

import org.bndly.common.data.api.Data;
import org.bndly.common.data.api.DataStore;
import org.bndly.common.data.api.DataStoreListener;
import org.bndly.common.data.api.NoOpDataStoreListener;
import org.bndly.common.data.io.ReplayableInputStream;
import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.ebx.data.deployment.DataDeployer;
import org.bndly.ebx.data.deployment.DataDeploymentListener;
import org.bndly.schema.fixtures.api.BoundFixtureDeployer;
import org.bndly.schema.fixtures.api.FixtureDeployer;
import java.io.InputStreamReader;
import java.io.Reader;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {InitialFixtureDeployer.class, DataStoreListener.class}, immediate = true)
@Designate(ocd = InitialFixtureDeployer.Configuration.class)
public class InitialFixtureDeployer extends NoOpDataStoreListener implements DataStoreListener, DataDeploymentListener {
	
	@ObjectClassDefinition
	public @interface Configuration {
		@AttributeDefinition(
				name = "Initial fixture installed", 
				description = "Set this property to true, to make sure, that the initial fixture is installed. The value of 'enbaleAnyFixture' will be ignored."
		)
		boolean enbaleInitialFixture() default true;
		
		@AttributeDefinition(
				name = "Any fixture installed", 
				description = "Set this property to true, to install any fixture from a datastore, that ends with '-fixture.json'. The target schema is derived "
						+ "from the file name. For example 'foo.schemaname-fixture.json' would be a fixture for the schema 'schemaname'.")
		boolean enbaleAnyFixture() default true;
		
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(InitialFixtureDeployer.class);
	private static final String SUFFIX = "-fixture.json";
	
	@Reference
	private DataDeployer dataDeployer;
	
	@Reference(target = "(schemaName=ebx)")
	private BoundFixtureDeployer ebxFixtureDeployer;
	
	@Reference
	private FixtureDeployer fixtureDeployer;
	
	@Reference(target = "(service.pid=org.bndly.common.data.api.DataStore.ebx)")
	private DataStore dataStore;

	private boolean enbaleInitialFixture = true;
	private boolean enbaleAnyFixture = true;
	
	@Activate
	public void activate(final ComponentContext componentContext) {
		LOG.info("activating initial fixture deployment");
		DictionaryAdapter da = new DictionaryAdapter(componentContext.getProperties());
		enbaleInitialFixture = da.getBoolean("enbaleInitialFixture", enbaleAnyFixture);
		enbaleAnyFixture = da.getBoolean("enbaleAnyFixture", enbaleAnyFixture);
		dataStore.addListener(this);
		dataDeployer.addListener(this);
	}
	@Deactivate
	public void deactivate(final ComponentContext componentContext) {
		dataDeployer.removeListener(this);
		dataStore.removeListener(this);
	}
	
	@Override
	public void dataStoreIsReady(DataStore dataStore) {
		LOG.info("datastore {} is ready", dataStore.getName());
	}

	@Override
	public void dataStoreClosed(DataStore dataStore) {
		// no-op
	}

	@Override
	public void dataDeployed(DataDeployer dataDeployer) {
		LOG.info("data is deployed");
		// we should only install this fixture, once the application is started.
		// if we do it before, we can not be sure, that the fixturedeployer already knows all available schema bean factories.
		installFixture(dataDeployer);
	}

	@Override
	public Data dataUpdated(DataStore dataStore, Data data) {
		if (isFixtureData(data)) {
			if (!enbaleAnyFixture) {
				LOG.info("skipping installation of fixture {} because this feature has been disabled.", data.getName());
				return data;
			}
			String schemaName = getSchemaNameFromDataName(data.getName());
			installFixture(schemaName, dataStore, data, false, null);
		}
		return data;
	}

	@Override
	public Data dataCreated(DataStore dataStore, Data data) {
		if (isFixtureData(data)) {
			if (!enbaleAnyFixture) {
				LOG.info("skipping installation of fixture {} because this feature has been disabled.", data.getName());
				return data;
			}
			String schemaName = getSchemaNameFromDataName(data.getName());
			installFixture(schemaName, dataStore, data, false, null);
		}
		return data;
	}
	/**
	 * Test for data name and content type, if the content type is defined.
	 * data names should be constructed like this: xxx.{schemaName}-fixture.json
	 * @param data
	 * @return 
	 */
	private boolean isFixtureData(Data data) {
		String name = data.getName();
		if (name != null && name.endsWith(SUFFIX)) {
			if (getSchemaNameFromDataName(name) != null) {
				String ct = data.getContentType();
				if (ct == null || ct.equals("application/json")) {
					return true;
				}
			}

		}
		return false;
	}
	
	private String getSchemaNameFromDataName(String dataName) {
		String schemaName = dataName.substring(0, dataName.length() - SUFFIX.length());
		int i = schemaName.lastIndexOf(".");
		if (i >= 0) {
			schemaName = schemaName.substring(i + 1);
			if (schemaName.isEmpty()) {
				return null;
			} else {
				return schemaName;
			}
		}
		return null;
	}

	private void installFixture(DataDeployer dataDeployer) {
		if (!enbaleInitialFixture) {
			LOG.info("skipping installation of initial fixture because this feature has been disabled.");
			return;
		}
		if (!dataStore.isReady() || !dataDeployer.didDeploy()) {
			LOG.info("skipping installation of initial fixture data store ready: {} data deplyoment done: {}", dataStore.isReady(), dataDeployer.didDeploy());
			return;
		}
		if (ebxFixtureDeployer == null) {
			LOG.info("skipping installation of initial fixture, because the fixture deployer is still missing");
			return;
		}

		Data data = dataStore.findByNameAndContentType("initial-fixture.json", "application/json");
		if (data == null) {
			LOG.info("could not find initial fixture data in data store {}", dataStore.getName());
		}
		installFixture("ebx", dataStore, data, true, ebxFixtureDeployer);
	}

	public boolean installFixture(String schemaName, DataStore dataStore, Data data, boolean deleteAfterInstallation, BoundFixtureDeployer boundFixtureDeployer) {
		if (data == null) {
			LOG.info("did skip installation of fixture because data was null");
			return false;
		}
		ReplayableInputStream is = data.getInputStream();
		if (is == null) {
			LOG.info("did skip installation of fixture because data inputstream was null");
			return false;
		}
		try {
			if (is.getBytesRead() > 0) {
				LOG.info("input stream had already been read, doing a replay now.");
				is = is.doReplay();
			}
			Reader reader = new InputStreamReader(is, "UTF-8");
			LOG.info("deploying fixture to {}", schemaName);
			if (boundFixtureDeployer != null) {
				boundFixtureDeployer.deploy(reader);
			} else {
				fixtureDeployer.deploy(schemaName, reader);
			}
			LOG.info("did deploy fixture to {}", schemaName);
			if (deleteAfterInstallation) {
				dataStore.delete(data);
			}
		} catch (Exception ex) {
			LOG.error("failed to install fixture: " + ex.getMessage(), ex);
			return false;
		}
		return true;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
		dataStore.addListener(this);
	}

	public void setFixtureDeployer(FixtureDeployer fixtureDeployer) {
		this.fixtureDeployer = fixtureDeployer;
	}

}
