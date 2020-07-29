# eBX Initial Fixture Deployer

## Introduction
The purpose of the initial fixture deployer is to deploy a fixed data set upon start of the application. This may be necessary if the application relies on some data to be present or if the application should provision its environment on its own.

## How to define and deploy an initial fixture
The fixture is defined as a JSON file. The file needs to be named `initial-fixture.json`. If such a file is found in the _eBX Data Store_, then it will be passed to the `org.bndly.schema.fixtures.api.FixtureDeployer` OSGI service for deployment. If the deployment went through without any issues, then the file will be deleted. This way deleted data will not re-appear after first installation.

In order to prevent the fixture installing mechanism either do not provide the fixture or configure the `org.bndly.ebx.fixture.InitialFixtureDeployer` OSGI service accordingly. The config property `enbaleInitialFixture` controls the installation mechanism. If it is set to true, then a found initial fixture will be installed.

The content of the fixture file is defined as follows:

```
{
	"items": [
		{
			"type": "THE_NAME_OF_THE_ENTITY_TYPE",
			"entries": [
				{
					"_condition": {
						"type": "notExists",
						"values": {
							"name": "MALE"
						}
					},
					"_key": "MY_KEY_MALE",
					"name": "MALE"
				},
				{
					"_condition": {
						"type": "notExists",
						"values": {
							"name": "FEMALE"
						}
					},
					"_key": "MY_KEY_FEMALE",
					"name": "FEMALE"
				}
			]
		},
		{
			"type": "THE_NAME_OF_ANOTHER_ENTITY_TYPE",
			"entries": [
				...
			]
		}
	]
}
```

The example show, that the different entity types can provide object instances represented as JSON objects. The objects can contain a `_condition` object to only insert those items, if there is no existing object, that matches with the `values` of the condition. No matter if an object already exists or is being created, it can be stored by defining a `_key`. The `_key` can then be used to reference it in another entity. This way partial inserts can be implemented by using fixtures. Here is an example for a reference:

```
{
	"items": [
		{
			"type": "THE_NAME_OF_THE_ENTITY_TYPE",
			"entries": [
				{
					"_key": "MY_KEY_MALE",
					"name": "MALE"
				}
			]
		},
		{
			"type": "THE_NAME_OF_ANOTHER_ENTITY_TYPE",
			"entries": [
				{
					"aPointerOnAnObject": { "_ref": "MY_KEY_MALE" }
				}
			]
		}
	]
}
```

The example fixture would reference the created `MY_KEY_MALE` from within the newly created `THE_NAME_OF_ANOTHER_ENTITY_TYPE` instance.

Please note, that you do not need to put a `_key` property in all fixture entries.

If you are using the eBX Data Deployment, then put the `initial-fixture.json` in the `framework/data-deployment` folder and it will be picked up and installed as soon as the eBX _Schema_ is available.

## Install schema related fixtures
Besides the installation of an initial fixture the deployer can also install fixtures, that match with the named of an available _Schema_. If a file follows the pattern `SCHEMA_NAME.DESCRIPTION-fixture.json`, then it will be deployed to the _Schema_ with the name `SCHEMA_NAME`. 
This can be useful when migrating data from one environment to another without having to bother about DB ids. You can write the fixture and simply drop it in the _eBX Data Store_. The fixture deployer will then install the fixture. 

The fixture will be installed upon creation or modification of the fixture file.

If you wish to disable this behavior set the OSGI property `enbaleAnyFixture` of `org.bndly.ebx.fixture.InitialFixtureDeployer` to `false`.