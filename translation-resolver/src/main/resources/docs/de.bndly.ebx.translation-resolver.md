# eBX Velocity Translation Resolver
## Introduction
The purpose of the translation resolver is to provide localized strings during Velocity template rendering.

## How can the translation resolver be used in the templates?
In order to use the resolver, a template variable will provide the resolving capabilities. The following code shows an example:

```
$translator.translate("the.technical.key")
```

As you can see, there is no need to explicitly set the locale, that will be passed to the translation resolver. This happens automatically by the template rendering framework.

If you have an object, that holds the key to translate as a variable, then you can also pass the key into the resolver via a variable. See the following example:

```
$translator.translate($model.aVariableWithTheTechnicalKey)
```

## How does the eBX Translation Resolver work?
The eBX implementation will look up translations by performing the following schema query:

```
PICK Translation t IF t.language.name=? AND t.translatedObject.translationKey=? LIMIT ?
```

The parameter `t.language.name` will be set to the current locale of the rendered template. This is a language code such as `de` or `en`. The paramter `t.translatedObject.translationKey` will be set to the technical key, that shall be resolved into a translation. The `LIMIT` will be hard set to `1`. This means, that the first hit in the stored translations will win.

If no translation can be found, then the original technical key will be returned as the resolved translation.

Given the following data, resolving the key `A_KEY` would lead to the translation `this is a key`. Resolving `ANOTHER_KEY` would lead to the translation `ANOTHER_KEY`, because there is not such translation stored in the schema. The translation `this is a key!!!` would never be rendered, because the key `A_KEY` is appearing multiple times.

1. `translationKey`=`A_KEY`; translated string = `this is a key`
2. `translationKey`=`A_KEY`; translated string = `this is a key!!!`
