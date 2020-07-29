# eBX Virtual Attribute Adapters

## Introduction
Some attributes in the domain of eBX are declared as virtual. This means that the values of such attributes are not persisted in the database. Instead the values are computed upon attribute access. All of the adapter implementations need to be explicitly defined with an OSGI configuration. If the configuration is missing, then the adapter will not be activated and the virtual attribute will not be resolved. You may also want to not define a configuration, if you want to use your own implementation for the given virtual attribute.

## Adapters by attribute holder
### PurchaseOrder
The attribute `totalTax` defines the total contained taxes within the order. It is computed by the `org.bndly.ebx.adapter.TotalTaxValueAdapter` implementation by adding up the taxes of the ordered items and the taxes of the shipment.

The attribute `merchandiseValue` defines the net value of the ordered items. It is computed by the `org.bndly.ebx.adapter.PurchaseOrderMerchandiseValueGrossAdapter` implementation. The same implementation also handles the `merchandiseValueGross` attribute, which defines the gross value of the ordered items. The values are computed by iterating over all ordered items and adding up `LineItem.getQuantity()*LineItem.getPrice()` or `LineItem.getQuantity()*LineItem.getPriceGross()`.

The attribute `total` defines the net value of the ordered items and the side costs (e.g. shipment). It is computed by `org.bndly.ebx.adapter.PurchaseOrderTotalValueAdapter` by reading the `merchandiseValue` and adding `PurchaseOrder.getShipmentOffer().getPrice()`.

The attribute `totalGross` defines the gross value of the ordered items and the gross side costs (e.g. shipment). It is computed by `org.bndly.ebx.adapter.PurchaseOrderTotalGrossValueAdapter` by reading the `merchandiseValueGross` and adding `PurchaseOrder.getShipmentOffer().getPriceGross()`.

The attribute `taxQuotaInfo` gives an object, that lists all the quota for the different contained tax rates in the order.

### TaxRelatedItemsContainer (Cart, PurchaseOrder)
The attribute `taxableItems` returns all items from the container, that are mixed with `TaxRelatedItem`. The implementation `org.bndly.ebx.adapter.TaxableItemsValueAdapter` gets `Cart.getCartItems()` or `PurchaseOrder.getItems()` depending on the container.

### Cart
The attribute `taxQuotaInfo` gives an object, that lists all the quota for the different contained tax rates in the cart. It is computed by `org.bndly.ebx.adapter.TaxQuotaValueAdapter`.

The attribute `merchandiseValueGross` defines the gross value of all items in the cart without side costs. The value is computed by `org.bndly.ebx.adapter.CartMerchandiseValueGrossAdapter` by iterating over `Cart.getCartItems()` and adding up `CartItem.getQuantity()*CartItem.getPriceNow()`.

The attribute `totalItemCount` defines the quantity of all items. If there is one cart item with a quantity of 1 and another cart item with a quantity of 4, then the `totalItemCount` will be 5. The value is computed by `org.bndly.ebx.adapter.CartTotalItemCountValueAdapter`.

### QuantifiedItem (CartItem, LineItem)
The attribute `quantifiedItemQuantity` defines a quantity for an item. The item can be a `CartItem` or a `LineItem` in a `PurchaseOrder`. The value is computed by `org.bndly.ebx.adapter.QuantifiedItemValueAdapter` and is basically just a delegation to the proprietary `quantity` attributes within `CartItem` and `LineItem`.

### LineItem
The attribute `total` defines the total net value of this line item considering the quantity. The value is computed by `org.bndly.ebx.adapter.LineItemTotalValueAdapter` by performing `LinteItem.getQuantity()*LinteItem.getPrice()`.

The attribute `totalGross` defines the total gross value of this line item considering the quantity. The value is computed by `org.bndly.ebx.adapter.LineItemTotalGrossValueAdapter` by performing `LinteItem.getQuantity()*LinteItem.getPriceGross()`.

### TaxableItem (CartItem)
The attribute `taxableItemPriceNetto` defines the net value of the taxable item ignoring any quantity. The value is computed by `org.bndly.ebx.adapter.CartItemTaxableItemValueAdapter`.

### TaxRelatedItem (CartItem)
The attribute `taxRelatedItemTaxRate` defines the tax rate in percent for the given item. The value is computed by `org.bndly.ebx.adapter.CartItemTaxRateValueAdapter` by looking up the `Purchasable` via its `sku` attribute. If such a purchasable exists, then the lowest valid price for the given `CartItem.getQuantity()` will used to access the tax rate.

The attribute `taxRelatedItemCurrency` defines the current for the given item. The value is computed by `org.bndly.ebx.adapter.CartItemCurrencyValueAdapter` by delegating to `CartItem.getCurreny()`.

The attribute `container` defines the container of the tax relate items. The value is computed by `org.bndly.ebx.adapter.CartItemContainerValueAdapter` by delegating to `CartItem.getCart()`.

### TaxedItem (CartItem, AbstractProduct)
The attribute `taxedItemPriceGross` defines the gross value of a taxed item without considering a quantity. The value is computed by `org.bndly.ebx.adapter.CartItemPriceGrossValueAdapter` and `org.bndly.ebx.adapter.AbstractProductAsTaxedItemValueAdapter` for the individual entity types. `CartItemPriceGrossValueAdapter` simply delegates to `CartItem.getPriceGrossNow()`. `AbstractProductAsTaxedItemValueAdapter` gets all available prices and if there is a `SimplePrice` with a `netValue` and `taxModel`, then the gross value will be computed by:

```
BigDecimal multiplicant = rate.add(HUNDRED).divide(HUNDRED, 2, RoundingMode.HALF_UP);
return netValue.multiply(multiplicant);
```
