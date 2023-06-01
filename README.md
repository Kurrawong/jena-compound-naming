# Jena ARQ Property Functions for Compound Naming

Initial proof of concept implementation.

## Example

This repo contains a `Main.kt` that loads in [src/main/resources/data.ttl](src/main/resources/data.ttl) into a dataset and registers the Compound Naming Property Functions with Jena.

We can run the following SPARQL query.

```sparql
PREFIX func: <urn:func/>

SELECT ?iri ?componentType ?componentValue
WHERE {
    BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-obj-1075435> AS ?iri)
    ?iri func:getLiteralComponents (?componentType ?componentValue) .
}
limit 10
```

And yield the following result.

```
-----------------------------------------------------------------------------------------------------------------------------------------------------------------
| iri                                                            | componentType                                                               | componentValue |
=================================================================================================================================================================
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1075435> | <https://linked.data.gov.au/def/roads/ct/RoadName>                          | "Yundah"       |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1075435> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/numberFirst> | "72"           |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1075435> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/locality>    | "SHORNCLIFFE"  |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1075435> | <https://linked.data.gov.au/def/roads/ct/RoadType>                          | "ST (Y)"       |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------
```
