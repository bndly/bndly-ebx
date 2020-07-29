# eBX CORS Filter
## Introduction
The _eBX CORS Filter_ uses a `org.bndly.common.cors.api.OriginService` to verify, if a CORS request is allowed or not. eBX allows to store instances of the `CORSOrigin` entity type. If a `CORSOrigin` instance exists, that matches with the scheme, domain and port of the other domain, then the CORS request will be permitted.

All available `CORSOrigin` instances will be cached in memory by the `OriginService`.

## Creating a CORSOrigin
In order to add an allowed CORSOrigin, the REST API of eBX can be used. Simply use the generated CRUD API. Here is an example:
Send a `POST` on `/bndly/ebx/CORSOrigin` with the following payload:

```
<cORSOrigin>
  <protocol>https</protocol>
  <domainName>localhost</domainName>
  <port>443</port>
</cORSOrigin>
```