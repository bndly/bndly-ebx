# eBX Price Util
## Introduction
The eBX Price Util offers utility classes an services to work with `PriceModel` and `Price` entity instances. The bundle provides a static `org.bndly.ebx.price.PriceUtil`, that will work fine with the default eBX domain model. If you want to customize the price model of eBX, then the service `org.bndly.ebx.price.api.PriceAdapterManager` should be used as it allows to hook up custom adapters for and evaluators for the price model. The recommendation is to always use the `PriceAdapterManager`. The `PriceUtil` only exists for backwards compatibility.

## Beginners trap
In order to use the `PriceAdapterManager` a `org.bndly.ebx.price.api.PriceDataInspector` service needs to be available. These services create string keys from passed in `Price` object instances. If you are working with generated client code classes, then you need to make the `org.bndly.ebx.price.impl.client.AbstractEntityPriceDataInspector` available. If you are working with schema beans, then you need to make `org.bndly.ebx.price.impl.schema.SchemaBeanPriceDataInspector` available. Both inspectors are implemented in independent bundles:
- `org.bndly.ebx:org.bndly.ebx.price-util-schema` for `SchemaBeanPriceDataInspector` and
- `org.bndly.ebx:org.bndly.ebx.price-util-client` for `AbstractEntityPriceDataInspector`.

Neither of these inspectors needs to be explicitly activated.

## Get a concrete price from a `Price` or `PriceModel`
The `PriceAdapterManager` offers methods for getting the immutable price objects from passed in `Price` entity instances. The methods are named `getPrice`, `getPrices` and `getValidPrices`. Each of these methods will take a `PriceContext` parameter. The `PriceContext` provides context information for the evaluation of an immutable price. For instance the context defines the desired quantity or it defines the rounding mode or the _current_ date or the _current_ location. For convenience there is a `PriceContext` with the quantity `1` available via `org.bndly.ebx.price.api.PriceContext.ONE_ITEM_HALF_UP`.

Let's say you have a single `Price` entity instance and want to get the immutable price object for a quantity of `1`:

```
org.bndly.ebx.model.Price price = ...;
org.bndly.ebx.price.api.ComputedPrice immutablePrice = priceAdapterManager.getPrice(price, PriceContext.ONE_ITEM_HALF_UP);
```

The `ComputedPrice` allows you to access the net and gross price information:

```
ComputedPrice immutablePrice = ...;
Price netPrice = immutablePrice.getNetPrice();
Price grossPrice = immutablePrice.getGrossPrice();
Price containedTax = immutablePrice.getContainingTax();
```

The computation of the net, gross and tax values is done by a `PriceAdapter`. eBX ships with adapters for the built in `Price` sub-types: `SimplePrice` and `StaggeredPrice`. The adapters need to be explicitly activated by providing their configurations:
- `org.bndly.ebx.price.impl.SimplePriceAdapter` and
- `org.bndly.ebx.price.impl.StaggeredPriceAdapter`

The `SimplePriceAdapter` takes the `netValue` of a `SimplePrice` and multiplies it with the quantity from the `PriceContext`. The tax rate is taken from `Price.taxModel`. The contained tax value is determined by multiplying the `netValue` with the tax rate and dividing by `100`. During the division the `MathContext.DECIMAL128` will be used for managing the precision. The immutable price objects will then be created using the rounding mode, that was provided by `PriceContext.getRoundingMode()`. The gross value is the sum of the contained tax and the net value.

The `StaggeredPriceAdapter` uses the `SimplePriceAdapter` for the computation of net, gross and contained tax values. The key difference is, that the `StaggeredPriceAdapter` will select the net value from the `StaggeredPriceItem`, that has the lowest net value and for which `PriceContext.getQuantity()` is in the `StaggeredPriceItem` bounds.

### Example for `SimplePriceAdapter`
Let's assume we have the following `SimplePrice`:

```
{
	"netValue": 3.35,
	"taxModel": {
		"value": 19
	}
}
```

The the resulting `ComputedPrice` will look like this, if we use `PriceContext.ONE_ITEM_HALF_UP` for evaluation:

```
netPrice = 3.35
grossPrice = 3.99
containingTax = 0.64
```

### Example for `StaggeredPriceAdapter`
Let's assume we have the following `StaggeredPrice`:

```
{
	"items": [
		{
			"netValue": 3.35,
			"maxQuantity": 5,
			"minQuantity": null
		},
		{
			"netValue": 3.59,
			"maxQuantity": 20,
			"minQuantity": null
		},
		{
			"netValue": 3.00,
			"maxQuantity": 100,
			"minQuantity": 11
		}
	],
	"taxModel": {
		"value": 19
	}
}
```

The the resulting `ComputedPrice` will look like this, if we use `PriceContext.ONE_ITEM_HALF_UP` for evaluation:

```
netPrice = 3.35
grossPrice = 3.99
containingTax = 0.64
```

For `PriceContext.getQuantity() == 10` we would get the following result:

```
netPrice = 33.50
grossPrice = 39.87
containingTax = 6.37
```

As you can see, the cheapest price has been selected.

For `PriceContext.getQuantity() == 20` we would get the following result:

```
netPrice = 60.00
grossPrice = 71.40
containingTax = 11.40
```

## Evaluating constraints
Every `Price` instance can have multiple `PriceConstraint` instances. The constraints are joined with a logical `AND`. The constraints will only be evaluated if the method `PriceAdapterManager.getValidPrices` is used. `getPrice` and `getPrices` will ignore the constraints.

The constraints are evaluated by `PriceConstraintEvaluator` services. The built in implementations need to be explicitly activated by providing configurations. The following evaluators are available out of the box:
- `org.bndly.ebx.price.impl.CountryPriceConstraintEvaluator`
- `org.bndly.ebx.price.impl.EndDatePriceConstraintEvaluator`
- `org.bndly.ebx.price.impl.StartDatePriceConstraintEvaluator`

The `CountryPriceConstraintEvaluator` will declare a constraint as passed, if the constraint contains a `country` and the country from `PriceContext.getCountryCode()` is either `null` or matches with `constraint.country.isoCode2` or `constraint.country.isoCode3`.

Both `EndDatePriceConstraintEvaluator` and `StartDatePriceConstraintEvaluator` will declare a constraint as passed, if the constraint has no `startDate` or `endDate` or if `PriceContext.getDate()` returns `null`. The date borders will be compared inclusive. This means, that the following conditions need to be true in order to declare a constraint as passed:
1. `PriceContext.getDate() >= constraint.startDate`
2. `PriceContext.getDate() <= constraint.endDate`

## Add a custom adapter
If your version of eBX comes with a custom sub-type of `Price`, then you also need to provide a custom `PriceAdapter`. The adapter class should be declared as an OSGI service:

```
@Component(immediate = true)
@Service
public class CustomPriceAdapter implements PriceAdapter<CustomPrice> {
	// your implementation goes here
}
```

The `PriceAdapterManager` will automatically track the adapters. So you do not need to manually add them to a manager.
