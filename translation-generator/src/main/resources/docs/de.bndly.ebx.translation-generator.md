# eBX Translation Generator

## Introduction
The eBX translation generator is used to store ordinary Java property file contents as `Translation`, `TranslatedObject` and `Language` instances.

## Domain model
In order to store the data the entity types `Translation`, `TranslatedObject` and `Language` are used. The `Language` holds the language code such as _de_ or _en_. The `TranslatedObject` holds the key of the property. The `Translation` holds a specific value for a given key and language. Short: We have a classic m:n relation, while the `Translation` is the associative type in the middle.

## Configuration
The generator will only insert values from the configured property files, if a `Translation` does not exist yet for the given `Language` and `TranslatedObject`. In order to add or remove property files, the `org.bndly.ebx.translation.generator.PropertiesFilesToTranslationsGeneratorImpl` component needs to be configured. Here is an example configuration:

```
propertiesFileNames=example,foo
propertyFileLocation=./framework/translations
```

This configuration will take `example_de.properties`, `example_en.properties`, `foo_de.properties` and `foo_en.properties` from the folder `./framework/translations` if such files exist. If the files do not exist, they are ignored.
