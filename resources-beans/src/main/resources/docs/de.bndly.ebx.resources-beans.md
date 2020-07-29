# eBX Resource Beans

## Introduction
The eBX Resource Beans are JAXB message classes to perform complex operations on the REST API of eBX without having to orchestrate the persistence logic in the client code. The complex operations are:
- track a user event for an external object (e.g. that a user has seen a specific product in the browser)
- add an external object in a list of related other external objects (e.g. that a product is bought together with another product)
- send a wishlist via e-mail

## Track a user event
A user tracking event is defined by the `org.bndly.rest.beans.ebx.misc.UserTrackingRestBean` message class.

It refers to the user by an identifier string stored in `userIdentifier`. The identifier will be matched to `User.identifier` in the eBX domain model.

The external object (usually a content item) is referenced in `externalObjectIdentifier` and maps to `ExternalObject.identifier`

The association is described by the `listTypeName` element. It will map to `ExternalObjectListType.name`

Here is an example message:

```
<userTracking>
	<userIdentifier>foo@cybercon.de</userIdentifier>
	<externalObjectIdentifier>coremedia:///cap/content/86018</externalObjectIdentifier>
	<listTypeName>VIEWED</listTypeName>
</userTracking>
```

## Track external object relation
A relation between external objects is stored in the entity `ExternalObjectCentricExternalObjectList`. It is a list of external objects, that is related to another single external object. The semantic of the relation is expressed in the `type` attribute of the list. In order to easily create such lists and add items to it, the message class `org.bndly.rest.beans.ebx.misc.ExternalObjectTrackingRestBean` can be used.

The element `rootExternalObjectIdentifier` is the identifier of the owner external object of the list. It maps to `ExternalObjectCentricExternalObjectList.externalObject`.

The element `externalObjectIdentifier` is the identifier of the external object, that shall be placed within the list. It maps to `ExternalObject.identifier`.

The association is described by the `listTypeName` element. It will map to `ExternalObjectListType.name`

Here is an example message:

```
<externalObjectTracking>
	<rootExternalObjectIdentifier>coremedia:///cap/content/4711</rootExternalObjectIdentifier>
	<externalObjectIdentifier>coremedia:///cap/content/86018</externalObjectIdentifier>
	<listTypeName>BOUGHT_IN_CONJUNCTION</listTypeName>
</externalObjectTracking>
```

## Send a wishlist via e-mail
In order to send a wishlist via e-mail eBX needs to know the text and receiver of the e-mail. Furthermore eBX needs to know the link, under which the receiver will be able to view the wishlist. Here is an example of the message class `org.bndly.rest.beans.ebx.misc.WishListMailRestBean`:

```
<wishListMail>
	<wishListRef>
		<id>4711</id>
	</wishListRef>
	<text>Hey. Checkout my christmas wishes! Greetings!</text>
	<wishListLink>http://your.shop/wishlists/4711</wishListLink>
	<receiver>foo@cybercon.de</receiver>
	<receiver>bar@cybercon.de</receiver>
	<receiver>bingo@cybercon.de</receiver>
</wishListMail>
```
