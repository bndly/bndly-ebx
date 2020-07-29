# eBX JCR Importer

## Introduction
The eBX JCR Importer is used to push data from eBX into a JCR. A typical use case is to push product information in the JCR in order to avoid load towards eBX.

## Connection configuration
For a minimal setup the following additional bundle is required: `org.bndly.ebx:org.bndly.ebx.jcr-velocity`. This bundle contains a configuration class, that is needed to express the connection details.

The connection details need to be specified in an OSGI config. Here is an example:

```
"org.bndly.ebx.jcr.velocity.impl.ImporterConfigurationImpl-local": {
	"name": "local",
	"enabled": true,
	"user": "admin",
	"password": "admin",
	"languages": ["de","en"],
	"paths": [
		"content::/content"
	],
	"url": "http://author.localhost:8080/server"
}
```

- `name` is used to identify the configuration. It has no effect on the actual connection.
- `enabled` is used to prevent the configuration from being used during imports. A disabled connection configuration will not lead to any data imported into the specified JCR.
- `user` is the name of the user, that shall be used to log into the target JCR. The login will typically happen via the WebDAV JCR implementation.
- `password` is the password, that shall be used to the login.
- `languages` is the collection of languages, that the importer should support in the target JCR. This is just a shared configuration value, that _can_ be used when working with the JCR. The connection can be established even without any `languages`.
- `paths` is a collection of key value pairs, that are separated by `::`. The key is an alias for a path (the value). In the example above, the path `/content` can be referenced by the alias `content`. The main purpose is to configure some static paths in the JCR without having to hard code them in your import logic.
- `url` is the actual URL of the WebDAV servlet, that will be accessed by the importer.

A JCR supports multiple workspaces. If you are working with a naked _Apache Sling_ or a full fledged _Adobe Experience Manager_ (AEM), then you should use the `CmsDaoImpl` to define the workspace, that should be used for the import. For Sling use the workspace `default` and for AEM use `crx.default`:

```
"org.bndly.ebx.jcr.importer.impl.CmsDaoImpl": {
	"defaultWorkspace": "default:
}
```


The JCR Importer requires a publication service. The publication service is used to transport imported data from the authoring JCR to the publication JCR. Here is an example configuration:

```
"org.bndly.ebx.jcr.importer.impl.SlingPublicationServiceImpl": {
	"name": "local",
	"url": "http://author.localhost:8080/libs/sling/distribution/services/agents/publish",
	"httpClient.target": "(service.pid=org.apache.http.client.HttpClient.default)"
}
```

- `name` is just used for identifying the publication service
- `url` is used to identify the resource in the target Sling or AEM, that can perform the publication via the Sling distribution components.
- `httpClient.target` defines an LDAP filter expression, that is used to look up a HTTP Client, that shall be used for communication with Sling.

If you just have a single JCR, then you need to provide your own No-Op implementation of a publication service.

Last the JCR Importer component needs to be configured. Here is an example configuration:

```
"org.bndly.ebx.jcr.importer.impl.JCRImporterImpl": {
	"name": "local",
	"persistenceLifecycleHookEnabled": true,
	"cmsDao.target": "(name=local)",
	"publicationService.target": "(name=local)"
}
```

- `name` is just used for identifying the JCR Importer.
- `persistenceLifecycleHookEnabled` is used to tell the JCR Importer if he should hook into the persistence lifecycle of eBX entities. If this value is `true`, that each modification of an eBX entity will be tracked and your synchronization strategies will schedule jobs for later execution. In some cases you do not want this automatism. Therefore it can be disabled via this configuration property. Then an entity will only be synchronized, if it is explicitly passed to the JCR Importer.
- `cmsDao.target` defines an LDAP filter expression to look up the `CmsDaoImpl` that shall be used for establishing connections (Remember: the `CmsDaoImpl` defines the JCR workspace)
- `publicationService.target` defines an LDAP filter expression, that refers to the publication service, that we configured above.


## Testing a configured connection
In order to test your connection configuration you can use the REST API of the JCR Importer. Perform a  `GET` on `/bndly/jcr-importer.xml` and you should see a response similar to this:

```
<jcrImporterStatus>
	<enabled>true</enabled>
	<running>false</running>
	<connected>true</connected>
	<done>0.0</done>
	<repositoryUrl>http://author.localhost:8080/server</repositoryUrl>
</jcrImporterStatus>
```
If `connected` is set to `false`, then the connection could not be established. Then you should check the log of eBX for errors. If `connected` is set to `true`, then everything is fine.

## Triggering an import per entity
In order to start an import per entity manually, you should follow the `jcrimport` link, that is injected in to the JAXB message class for the according entity. 

Lets say you want to import the `Product` with id `4711` into the JCR, then you would drill down in the eBX REST API until you reach `/bndly/ebx/Product/4711`. In the response you will find a link called `jcrimport`. If you follow that link, then the entity related import strategies will be called for the according entity and the importer will immediately execute the created jobs.