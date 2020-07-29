# eBX Data Deployment
## Introduction
The purpose of the eBX Data Deployment is to deploy file system resources in the database of eBX without having to put the file data in a very large fixture file or SQL script.
The eBX Data Deployment takes files from the file system and stores them in the _eBX Data Store_. The data store is implemented by wrapping `BinaryData` instances as the data items of the store.

## Configuration
In order to enable the deployment, the `org.bndly.ebx.data.deployment.DataDeployerImpl` configuration needs to contain a non-empty property called `folderToScan`. 
If the configured folder does not exist an error message will be logged. If the configured folder is a file, then it will be silently ignored.

## Event listener
The eBX Data Deployment allows to attach event listeners on the deployment. The listener interface is called `org.bndly.ebx.data.deployment.DataDeploymentListener`. The listener needs to be manually attached to the `org.bndly.ebx.data.deployment.DataDeployer`.
The listener will be called, if the data deployment has been completed before attaching the listener. This way deployment events will not be missed.