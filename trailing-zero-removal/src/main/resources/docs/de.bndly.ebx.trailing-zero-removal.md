# eBX Trailing Zero Removal

# Introduction
eBX uses JAXB for defining message classes on the REST API. JAXB is meant for using XML representations, but modern REST APIs also often use JSON. This leads to the usage of an XML Jettison bridge to de-/serialize JAXB classes from/to JSON.

# BigDecimal issue
JAXB provides automatic conversion mechanisms for Java's BigDecimal class. The important thing to note for BigDecimals is, that they can contain explicit trailing zeros at the end. So in XML you might have a message, that looks like this:

```
<message>
	<aNumber>3.99000</aNumber>
	<anotherNumber>1.99</anotherNumber>
</message>
```

In JSON (using Jettison) the same message would look like this:

```
{
	"message": {
		"aNumber": "3.99000",
		"anotherNumber": 1.99
	}
}
```

A consumer of the REST API would expect, that the numeric type is always serialized as a JSON number. The problem here is, that trailing zeros are not allowed in a JSON number value. 

In order to offer a homogenized API, the trailing zero removal will iterate over all outbound JAXB messages and removes trailing zeros. So the examples from above would look like this:

```
<message>
	<aNumber>3.99</aNumber>
	<anotherNumber>1.99</anotherNumber>
</message>
```

or in JSON:

```
{
	"message": {
		"aNumber": 3.99,
		"anotherNumber": 1.99
	}
}
```