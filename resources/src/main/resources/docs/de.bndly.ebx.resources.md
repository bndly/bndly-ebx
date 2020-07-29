# eBX Resources

## Introduction
The eBX Resources bundle contains several different OSGI services:
- Activiti process Java bindings
- REST API controllers, exception mappers for providing custom logic
- REST API controllers for integrating with an embedded Solr 4 webapp
- Atom link constraints for representing application state in HATEOAS
- Persistence listeners for event based processing of entities


## Activiti processes
In order to be easier to integrate in Java code, the eBX Activiti processes have a Java interface binding, that allows to interact with the processes via a strongly typed API. Rather than passing maps of parameters to a process instance, the Java interface is annotated so that the process variables names can be taken from the interface method invocation. Here is an example of the `CartBusinessProcesses`:

```
public interface CartBusinessProcesses {

	public PriceRequest runPriceRequest(
			@ProcessVariable(name = "priceRequest") PriceRequest priceRequest,
			@Context RecordContext recordContext
	);
}
```

As you can see, the process `PriceRequest` will be started with a variable `priceRequest`, that points to a `PriceRequest` entity. In order to allow the process to integrate with _Schema API_, the `RecordContext` for loading entities will be passed into the process invocation. The returned item will be the process variable, that holds a `PriceRequest` instance.

The implementation of this process is not done by having a `CartBusinessProcessesImpl` but by creating a proxy object, that delegates the invocations on the interface towards the Activiti engine within eBX. The instance is created in the `org.bndly.ebx.resources.bpm.Activator` component, which automatically registeres Java bindings for the following processes:
- `org.bndly.ebx.resources.bpm.CartBusinessProcesses`: Get the current price for an incoming `PriceRequest` entity. The BPMN file has to be named `PriceRequest.bpmn`.
- `org.bndly.ebx.resources.bpm.CheckoutBusinessProcesses`: Start a new checkout and allow deferred resuming of the checkout once a payment result is getting available. The BPMN file has to be named `Checkout.bpmn`.
- `org.bndly.ebx.resources.bpm.ShipmentOfferBusinessProcessses`: Create a list of `ShipmentOffer` instances, from which the user can pick one for his purchase order. The BPMN file has to be named `ListShipmentOffers.bpmn`.
- `org.bndly.ebx.resources.bpm.StockBusinessProcesses`: Similar to the `PriceRequest` the `StockRequest` will get the stock for a product by starting an Activiti process. The BPMN file has to be named `StockRequest.bpmn`.


## Controllers
### org.bndly.ebx.resources.custom.BatchResource
The purpose of this resource is to manipulate`ExternalObjectList` instances in a batch way.

For instance if two external objects with the ids `33` and `4` shall be associated to the `ExternalObjectCentricExternalObjectList` with the id `1`, the following `POST` on `batch/create/ExternalObjectListAssocation` will add the items:

```
<externalObjectListAssocationList>
	<externalObjectListAssocation>
		<externalObjectExternalObjectRef>
			<id>33</id>
		</externalObjectExternalObjectRef>
		<listExternalObjectCentricExternalObjectListRef>
			<id>1</id>
		</listExternalObjectCentricExternalObjectListRef>
		<quantity>2</quantity>
	</externalObjectListAssocation>
	<externalObjectListAssocation>
		<externalObjectExternalObjectRef>
			<id>4</id>
		</externalObjectExternalObjectRef>
		<listExternalObjectCentricExternalObjectListRef>
			<id>1</id>
		</listExternalObjectCentricExternalObjectListRef>
		<quantity>1</quantity>
	</externalObjectListAssocation>
</externalObjectListAssocationList>
```

For updating multiple external objects the `POST` needs to be sent to `batch/update/ExternalObjectListAssocation` and the individual `<externalObjectListAssocation>` elements need to contain the `id` within the DB. If the `id` is missing, then the update will cause an exception.
Similar to the update multiple external objects can be deleted by sending a `POST` on `batch/delete/ExternalObjectListAssocation`. Please note, that here the `<externalObjectListAssocation>` elements also need to contain the DB `id`.


### org.bndly.ebx.resources.custom.BinaryDataResource
The purpose of this resource is to easily upload and download of BinaryData instance data. The data of entity is stored in the attribute `bytes`. 

#### Download
In order to download the data via a `GET`, the resource offers a URL such as `ebx/BinaryData/1/download` to download the data of the `BinaryData` instance with the DB id `1`. 

#### Upload
The resource also allows to upload data into a `BinaryData` instance by `POST`ing to `ebx/BinaryData/1`. Given this example the `bytes` attribute of the `BinaryData` entity with the DB id `1` would be updated with the posted data.


### org.bndly.ebx.resources.custom.CartResource
The purpose of this resource is to refresh price information for a `Cart` instance with a single REST API call. This is done by sending the `Cart` via `POST` to `Cart/1`, while `1` is the DB id of the cart. The URL is also linked into `CartRestBean` via the `refresh` link.

Upon invocation the resource will map the incoming `CartRestBean` to a `Cart`. Then the `User` will be loaded, that owns the `Cart` by performing a query using `Cart.userIdentifier` as `User.identifier`. Then for all items within the `Cart` a new `PriceRequest` will be created while the mappings are as follows:

```
PriceRequest.sku=CartItem.sku
PriceRequest.user=User(identifier=Cart.userIdentifier)
PriceRequest.quantity=CartItem.quantity
```

After the Activiti process for price resolution has finished the old price information from `CartItem.priceNow` will be moved to `CartItem.priceBeforeRefresh` and from `CartItem.priceGrossNow` will be moved to `CartItem.priceGrossBeforeRefresh`. The new `CartItem.priceNow` will be taken from `PriceRequest.discountedNetValue` and the new `CartItem.priceGrossNow` will be taken from `PriceRequest.discountedGrossValue`.

In other words: the price information shifts from the `PriceRequest.discountedNetValue` through the `CartItem.price` and `CartItem.priceBeforeRefresh` attributes for each refresh.

### org.bndly.ebx.resources.custom.CheckoutRequestResource
This resource is used to send payment results to a running `CheckoutRequest`. This is done by following one of the links:
- `paymentSuccess`
- `paymentCanceled`
- `paymentFailed`

These links are injected into the `CheckoutRequestRestBean` messages. The first link will resume the `Checkout` Activiti process by invoking `CheckoutBusinessProcesses.resumeCheckout(processId, "paymentResultReceived", paymentResult, recordContext)`. Depending on the link name, the `paymentResult` variable will either be `success`, `cancelation` or `failure`.

### org.bndly.ebx.resources.custom.PropertiesFilePropertySetResource
This resource renders a `PropertySet` or all `Translation` instances of a given language as a Java property file.

To download a `PropertySet` as a properties file, follow the `properties` link within a `PropertySetRestBean` message or call the URL `PropertySet/mypropertyset.properties` (`mypropertyset` is the `PropertySet.name`).
To download all `Translation` instances as a properties file, follow the `properties` link in a `LanguageRestBean` in order to download the translations for the given language. If you want to construct the URL manually, then call `Translation/language/de.properties` while `de` is the `Language.code` to use.

### org.bndly.ebx.resources.custom.PurchaseOrderBillingFailureResource
This resource is used to assign a billing failure to a `PurchaseOrder`. This is done by following the link `addBillingFailure` and sending a `PurchaseOrderBillingFailureRestBean` as the payload. The purchase order will be set as a reference by the resource implementation. The link will only be available, if the `PurchaseOrder` has no billing failures yet.

### org.bndly.ebx.resources.custom.PurchaseOrderCancelationResource
This resource is used to assign a cancellation to a `PurchaseOrder`.  This is done by following the link `cancel`. The link will only be available, if the `PurchaseOrder` has no cancellations yet. The cancellation can _not_ be sent as a payload. It will be automatically created by the resource implementation.

### org.bndly.ebx.resources.custom.PurchaseOrderResource
This resource looks up the`Invoice` or the `Shipment` for a given `PurchaseOrder`. The `Invoice` is available by following the `invoice` link within the `PurchaseOrderRestBean` message. The `Shipment` is available by following the `shipment` link within the `PurchaseOrderRestBean` message. The most recent invoice or most recent shipment will be returned. If there is no `Invoice` or `Shipment` available, a `404` will be returned.

### org.bndly.ebx.resources.custom.TrackingResource
This resource is used to track events for a user centric or for external object centric list of external objects. Typical use cases are features such as 'persons who bought X also bought Y' (`ExternalObjectCentricExternalObjectList`) or 'Your last viewed products' (`UserCentricExternalObjectList`).
In the message classes for both kinds of lists a `trackFast` link will be injected. 

Tracking for a `ExternalObjectCentricExternalObjectList` is done by sending a `ExternalObjectTrackingRestBean` message onto the link `trackFast` inside of a `ExternalObjectCentricExternalObjectListListRestBean` message.
Here is an example of the request flow:
1. `GET` `ebx/ExternalObjectCentricExternalObjectList` (contains the `trackFast` link)

```
<externalObjectCentricExternalObjectListList>
	<lnk>
		<rel>trackFast</rel>
		<href>http://localhost:8082/bndly/ebx/ExternalObjectCentricExternalObjectList/track</href>
		<method>POST</method>
	</lnk>
	<!-- more XML -->
</externalObjectCentricExternalObjectListList>
```
2. `POST` `ebx/ExternalObjectCentricExternalObjectList/track` with a `ExternalObjectTrackingRestBean` payload

```
<externalObjectTracking>
	<rootExternalObjectIdentifier>0815</rootExternalObjectIdentifier>
	<externalObjectIdentifier>4711</externalObjectIdentifier>
	<listTypeName>BOUGHT_IN_CONJUNCTION</listTypeName>
</externalObjectTracking>
```

The response will point to the URL of the created/updated `ExternalObjectCentricExternalObjectList`.

Tracking for a `UserCentricExternalObjectList` is done by sending a `UserTrackingRestBean` message onto the link `trackFast` inside of a `UserCentricExternalObjectListListRestBean` message. The request flow looks like this:
1. `GET` `ebx/UserCentricExternalObjectList` (contains the `trackFast` link)

```
<userCentricExternalObjectListList>
	<lnk>
	<rel>trackFast</rel>
	<href>http://localhost:8082/bndly/ebx/UserCentricExternalObjectList/track</href>
	<method>POST</method>
	</lnk>
	<!-- more XML -->
</userCentricExternalObjectListList>
```
2. `POST` `ebx/UserCentricExternalObjectList/track` with a `UserTrackingRestBean` payload

```
<userTracking>
	<userIdentifier>foo@cybercon.de</userIdentifier>
	<externalObjectIdentifier>4711</externalObjectIdentifier>
	<listTypeName>BOUGHT_IN_CONJUNCTION</listTypeName>
</userTracking>
```

The response will point to the URL of the created/updated `UserCentricExternalObjectList`.


### org.bndly.ebx.resources.custom.UserPriceResource
This resource is used to retrieve a product price for a user in a cacheable way. This is done by following the `createCacheable` in the `PriceRequestListRestBean` message class. When the link is being followed, then the resource expects an incoming `PriceRequestRestBean` message. The resource then redirects to a different URL, that is cacheable. If the same `PriceRequestRestBean` is passed into the resource within a 1 minute time frame, then the response will be taken from the cache. If there is no cache entry yet, then the resource will determine the price and returns it.

Here is a request flow:
1. `GET` `ebx/PriceRequest`

```
<priceRequestList>
	<lnk>
	<rel>createCacheable</rel>
	<href>http://localhost:8082/bndly/ebx/PriceRequest/create</href>
	<method>POST</method>
	</lnk>
</priceRequestList>
```
2. `POST` `ebx/PriceRequest/create`

```
<priceRequest>
	<currencyCurrencyRef>
		<id>1</id>
	</currencyCurrencyRef>
	<userUserRef>
		<id>1815</id>
	</userUserRef>
	<sku>4711</sku>
	<quantity>1</quantity>
</priceRequest>
```

The response will be a redirect on `ebx/UserPrice/1815/4711/price.1.1.xml`.


## Solr Search Controllers
For each `EntityResource` a `org.bndly.ebx.resources.custom.SearchResource` will be deployed. In order to activate this behavior, a `org.bndly.ebx.resources.custom.SearchResourceDeployer` config needs to be available. The `SearchResource` allows to reindex entities by type or to perform a search based on a Solr query. The response of the Solr will be processed in order to fit the generated JAXB message classes for the eBX domain model.
The `org.bndly.ebx.resources.custom.WishListSearchQueryCustomizer` will hook into the search queries to only return those `WishList` instances, that have a privacy, that allows searching (`WishListPrivacy.appearsInSearchIndex=true`).

### Performing a search
POST `org.bndly.rest.search.beans.SearchParameters` onto the `SearchResource`
If the parameters contain a `query`, then the query is used. If the parameters only contain a `searchTerm`, then the `searchTerm` is transformed into a query. This is done by replacing `{searchTerm}` with the passed in `searchTerm` value in a query template string. The query template string is defined as an OSGI property on the `org.bndly.ebx.resources.custom.SearchResourceDeployer`. The property has to follow the naming convention `searchterm.format.{SCHEMA_NAME}.{TYPE_NAME}`. Here is an example configuration:

```
searchterm.format.ebx.WishList=(WishList_name:*{searchTerm}* OR WishList_email:{searchTerm} OR WishList_email:*{searchTerm}* OR WishList_securityToken:{searchTerm})
```

For a paginated search result, the `PaginationRestBean` from `SearchParameters.getPage()`. The sorting is applied by appending the data of the `SortRestBean` from `SearchParameters.getSorting()`.

Here is an example POST request payload for submitting a search:

```
<searchParameters>
	<searchTerm>4711</searchTerm>
	<page>
		<start>10</start>
		<size>5</size>
	</page>
	<sorting>
		<field>name</field>
		<ascending>true</ascending>
	</sorting>
</searchParameters>
```

The response will be a redirect to a resource, that can be loaded via GET:

```
ebx/WishList/search?q=(WishList_name:*4711* OR WishList_email:4711 OR WishList_email:*4711* OR WishList_securityToken:4711)&pageStart=10&pageSize=5&sortingField=name&sortingDirection=ASC
```

The resource will then use the `select` request handler of the Solr to submit the query. The resulting items will then be mapped to JAXB message classes. Given the example above this would lead to a response message like this:

```
<wishListList>
	<wishList>
		<name>4711</name>
		<!-- more data -->
	</wishList>
	<!-- more items -->
</wishListList>
```


## Atom Link Constraints
The `org.bndly.ebx.resources.link.PurchaseOrderLinkConstraints` class will hook into the Atom link injection process in order to only inject an `update` link into a `PurchaseOrderRestBean` if the order has no cancellations. This makes a canceled order immutable on the REST API of eBX.


## Persistence Listeners

### org.bndly.ebx.resources.listener.CheckoutRequestPersistListener
When a new`CheckoutRequest` is created, then a checkout Activiti process is started via `CheckoutBusinessProcesses.runCheckout(CheckoutRequest, RecordContext)`.

### org.bndly.ebx.resources.listener.ExternalObjectListPersistListener
When a `UserCentricExternalObjectList` or `org.bndly.ebx.model.ExternalObjectCentricExternalObjectList` is persisted or merged, then the items of the list (`ExternalObjectList.associations`) will automatically synchronized with the available `ExternalObjectListAssocation` instances.

### org.bndly.ebx.resources.listener.OrderNumberPersistListener
When a `PurchaseOrder` is persisted and there is no `PurchaseOrder.orderNumber` available, then such a number will be generated from the DB id of the purchase order.

### org.bndly.ebx.resources.listener.PriceRequestPersistListener
When a `PriceRequest` is persisted, then an Activiti process for resolving the price value is started via `CartBusinessProcesses.runPriceRequest(PriceRequest, RecordContext)`.

### org.bndly.ebx.resources.listener.PropertySetPersistListener
When a `PropertySet` is persisted or merged, then the `PropertySet.properties` will be synchronized with the available `PropertySetAssociation` instances.

### org.bndly.ebx.resources.listener.PurchasableToStockItemListener
When ever a `Purchasable` is persisted or merged, then this listener makes sure, that there is a `StockItem` available for the `Purchasable.sku` if the `sku` is not null.

### org.bndly.ebx.resources.listener.ShipmentOfferRequestPersistListener
When a `ShipmentOfferRequest` is persisted, then the individual offers are created by an Activiti process, that is started via `ShipmentOfferBusinessProcessses.runListShipmentOffers(ShipmentOfferRequest, RecordContext)`.

### org.bndly.ebx.resources.listener.StockRequestPersistListener
When the stock for a purchasable item is requested by creating a new `StockRequest` instance, then this listener delegates to Activiti by calling `StockBusinessProcesses.runStockRequest(StockRequest, RecordContext)`.