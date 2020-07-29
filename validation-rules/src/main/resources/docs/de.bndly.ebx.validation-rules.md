# eBX Validation Rules
## Introduction
eBX allows to define dynamic validation rules for exchanged messages. These rules are stored as rule sets per message type. The validation rules will evaluated within the eBX client before invocations of `DefaultDao.create(entity)`, `DefaultDao.createLocation(entity)`, `DefaultDao.update(entity)` and `DefaultDao.updateLocation(entity)`. If there are validation rules defined for the entity message class and if at least on rule is violated, then a `RestBeanValidationException` will be thrown by the client code.

## Discovery of validation rules
The eBX client will discover validation rules by looking for a link called `rules` within the root resource of the entity type. For instance take the entity `Product`. Then the client code will look for a link `rules` within the response of the the root resource `ebx/Product`. If there is no such link, then the client code will consider the product message as not being validated. If there is a `rules` link, then the rules will be loaded and cached by the client. The rules will then be evaluated against the message. Getting back to the `Product` example the validated message class would be `org.bndly.rest.beans.ebx.ProductRestBean`.

Here is an example response of the root resource for products:

```xml
<productList>
	<!-- many other links may be in here -->
	<lnk>
		<rel>rules</rel>
		<href>http://localhost:53923/bndly/ebx/Product/rules.xml</href>
		<method>GET</method>
	</lnk>
	<page>
		<start>0</start>
		<size>10</size>
		<totalRecords>0</totalRecords>
	</page>
</productList>
```

When the `rules` link is followed, then the response will return the rule set for the message class `org.bndly.rest.beans.ebx.ProductRestBean` of the entity `Product`:

```xml
<?xml version="1.0"?>
<ruleSet>
	<lnk>
		<rel>update</rel>
		<href>
http://localhost:53923/bndly/validationRules/ProductRestBean.xml
</href>
		<method>PUT</method>
	</lnk>
	<lnk>
		<rel>self</rel>
		<href>
http://localhost:53923/bndly/validationRules/ProductRestBean.xml
</href>
		<method>GET</method>
	</lnk>
	<lnk>
		<rel>list</rel>
		<href>
http://localhost:53923/bndly/validationRules.xml
</href>
		<method>GET</method>
	</lnk>
	<name>ProductRestBean</name>
	<rules>
		<rule>
			<pos>0</pos>
			<parameters>
				<not>
					<pos>0</pos>
					<parameters>
						<empty>
							<pos>0</pos>
							<parameters>
								<value>
									<pos>0</pos>
									<field>name</field>
								</value>
							</parameters>
						</empty>
					</parameters>
				</not>
				<maxSize>
					<pos>1</pos>
					<parameters>
						<value>
							<pos>0</pos>
							<name>value</name>
							<field>name</field>
						</value>
						<value>
							<pos>1</pos>
							<name>size</name>
							<numeric>255</numeric>
						</value>
					</parameters>
				</maxSize>
			</parameters>
			<field>name</field>
		</rule>
	</rules>
</ruleSet>
```

The example shows, that the property `name` within `org.bndly.rest.beans.ebx.ProductRestBean` is not allowed to be empty and it should be limited to contain a maximum of 255 characters.

## Predefined validation rules
eBX provides the following validation rules out of the box:

### org.bndly.rest.beans.ebx.AddressRestBean
- `additionalInfo`: maximum of 100 characters
- `city`: not empty, minimum of 2 characters
- `salutation`: not empty
- `firstName`: not empty, minimum of 2 characters and maximum of 50 characters
- `lastName`: not empty, minimum of 2 characters and maximum of 50 characters
- `street`: not empty, minimum of 2 characters
- `houseNumber`: maximum of 10 characters
- `postCode`: not empty, exactly 5 characters
- `country`: not empty


### org.bndly.rest.beans.ebx.AvailabilityRegistrationRestBean
- `createdOn`: not empty
- `email`: not empty
- `user`: not empty
- `externalObject`: not empty

### org.bndly.rest.beans.ebx.CartRestBean
- `lastInteraction`: not empty
- `userIdentifier`: not empty

### org.bndly.rest.beans.ebx.CheckoutRequestRestBean
- `order`: not empty
- `createdOn`: not empty

### org.bndly.rest.beans.ebx.ClassicBankPaymentDetailsRestBean
- `owner`: not empty, maximum of 255 characters
- `bankName`: not empty, maximum of 255 characters
- `bankCode`: not empty, maximum of 20 characters
- `accountNumber`: not empty, maximum of 20 characters

### org.bndly.rest.beans.ebx.CountryRestBean
- `isoCode2`: not empty, exactly 2 characters
- `isoCode3`: not empty, exactly 3 characters
- `name`: not empty

### org.bndly.rest.beans.ebx.CreditCardBrandRestBean
- `name`: not empty

### org.bndly.rest.beans.ebx.CreditCardPaymentDetailsRestBean
- `cardHolderName`: not empty, maximum of 255 characters
- `cardBrand`: not empty, maximum of 50 characters
- `creditCardNumber`: not empty, maximum of 16 characters
- `expiry`: not empty

### org.bndly.rest.beans.ebx.CurrencyRestBean
- `decimalPlaces`: not empty, positive number
- `code`: not empty

### org.bndly.rest.beans.ebx.ExternalObjectCentricExternalObjectListRestBean
- `object`: not empty
- `type`: not empty

### org.bndly.rest.beans.ebx.ExternalObjectListTypeRestBean
- `name`: not empty, maximum of 128 characters

### org.bndly.rest.beans.ebx.ExternalObjectRestBean
- `identifier`: not empty, maximum of 128 characters

### org.bndly.rest.beans.ebx.IBANBankPaymentDetailsRestBean
- `owner`: not empty, maximum of 255 characters
- `bankName`: not empty, maximum of 255 characters
- `iban`: not empty, maximum of 34 characters
- `bic`: not empty, maximum of 11 characters

### org.bndly.rest.beans.ebx.PaymentConfigurationRestBean
- `successLink`: not empty
- `failureLink`: not empty
- `cancelLink`: not empty

### org.bndly.rest.beans.ebx.PersonRestBean
- `externalUserId`: maximum of 50 characters

### org.bndly.rest.beans.ebx.PriceAlarmRegistrationRestBean
- `createdOn`: not empty
- `email`: not empty
- `user`: not empty
- `externalObject`: not empty
- `priceLimit`: not empty, positive number
- `currency`: not empty

### org.bndly.rest.beans.ebx.PriceRequestRestBean
- `user`: not empty
- `currency`: not empty
- `sku`: not empty
- `createdOn`: not empty

### org.bndly.rest.beans.ebx.ProductRestBean
- `manufacturer`: not empty
- `model`: maximum of 100 characters
- `priceModel`: not empty
- `name`: not empty, maximum of 255 characters
- `gtin`: maximum of 14 characters, characters can only be `0123456789`

### org.bndly.rest.beans.ebx.PropertySetRestBean
- `name`: not empty

### org.bndly.rest.beans.ebx.PurchaseOrderBillingFailureRestBean
- `purchaseOrder`: not empty
- `createdOn`: not empty
- `paymentConfiguration`: not empty
- `reason`: maximum of 255 characters

### org.bndly.rest.beans.ebx.PurchaseOrderCancelationRestBean
- `purchaseOrder`: not empty
- `createdOn`: not empty
- `reason`: maximum of 255 characters

### org.bndly.rest.beans.ebx.PurchaseOrderPaymentFulfillmentRestBean
- `purchaseOrder`: not empty
- `createdOn`: not empty
- `paymentConfiguration`: not empty
- `reason`: maximum of 255 characters

### org.bndly.rest.beans.ebx.PurchaseOrderRestBean
- `note`: maximum of 1000 characters
- `shipmentOffer`: not empty
- `shipmentDate`: only before now

### org.bndly.rest.beans.ebx.QuantityUnitRestBean
- `quantity`: not empty
- `abbrevation`: not empty, maximum of 100 characters
- `description`: maximum of 100 characters

### org.bndly.rest.beans.ebx.ShipmentModeRestBean
- `name`: not empty

### org.bndly.rest.beans.ebx.ShipmentOfferRequestRestBean
- `user`: not empty
- `cart`: not empty
- `currency`: not empty

### org.bndly.rest.beans.ebx.ShipmentOfferRestBean
- `shipmentMode`: not empty
- `shipmentOfferRequest`: not empty
- `price`: not empty, positive number
- `priceGross`: not empty, positive number
- `createdOn`: not empty
- `currency`: not empty

### org.bndly.rest.beans.ebx.UserAttributeRestBean
- `name`: not empty

### org.bndly.rest.beans.ebx.UserAttributeValueRestBean
- `user`: not empty
- `attribute`: not empty

### org.bndly.rest.beans.ebx.UserCentricExternalObjectListRestBean
- `user`: not empty
- `type`: not empty

### org.bndly.rest.beans.ebx.UserRestBean
- `identifier`: not empty, maximum of 128 characters

### org.bndly.rest.beans.ebx.ValueAddedTaxRestBean
- `name`: not empty, minimum of 2 characters and maximum of 50 characters
- `value`: not empty, precision of 2 integer digits and 2 floating point digits
- `description`: maximum of 255 characters

### org.bndly.rest.beans.ebx.WishListItemRestBean
- `addedOn`: not empty
- `coremediaProductName`: not empty
- `coremediaUUID`: not empty, maximum of 255 characters
- `sku`: not empty
- `desiredAmount`: not empty, positive number
- `priceGrossOnAddition`: not empty, positive number
- `priceOnAddition`: not empty, positive number
- `currency`: not empty

### org.bndly.rest.beans.ebx.WishListRestBean
- `name`: not empty, maximum of 100 characters
- `profilePictureVisible`: not empty
- `createDate`: not empty
- `securityToken`: maximum of 128 characters
- `userNickName`: maximum of 255 characters
- `email`: maximum of 255 characters
- `city`: maximum of 255 characters
