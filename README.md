# Jena ARQ Compound Naming Property Functions Library

A library of functions for working with data in the Compound Naming structure. See the [Compound Naming Model](https://agldwg.github.io/compound-naming-model/model.html) for more information.

## Example code usage

This repo contains a [Main.kt](src/main/kotlin/Main.kt) that loads in [src/main/resources/data.ttl](src/main/resources/data.ttl) into a dataset and registers the Compound Naming Property Functions with Jena.

We can run the following SPARQL query which utilises the `getComponents` property function. This function recursively fetches all the component parts of the compound name instance and returns its type, the value and the component identifier.

Where component identifiers are blank nodes, the internal Jena system identifier is returned. This can be used in tandem with RDF Delta Patch logs to provide updates to blank node instances.

```sparql
SELECT *
WHERE {
    GRAPH ?g {
        BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> AS ?iri)
        ?iri <java:ai.kurrawong.jena.compoundnaming.getComponents> (?componentType ?componentValue ?componentId) .
    }
}
limit 10
```

And get the following result.

```
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| iri                                                            | componentType                                                                 | componentValue  | componentId                                                            | g                   |
===================================================================================================================================================================================================================================================================
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/flatTypeCode>  | "U"             | "<https://example.com/flatTypeCode/U>"                                 | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://linked.data.gov.au/def/roads/ct/RoadType>                            | "HWY (Y)"       | "_:e28c67b9f6971165cc8b9baf15e9be01"                                   | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/flatNumber>    | "14"            | "_:1a6454c0258fb72a999604b64c9d36a3"                                   | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/locality>      | "MERMAID BEACH" | "<https://linked.data.gov.au/dataset/qld-addr/locality-MERMAID-BEACH>" | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/numberFirst>   | "2342"          | "_:7ffd45b7bf489525c34c62e48f8cdc7a"                                   | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/levelTypeCode> | "G"             | "<https://linked.data.gov.au/dataset/gnaf/code/levelType/G>"           | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/numberLast>    | "2358"          | "_:976136004ffaa60901d4429608366808"                                   | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | <https://linked.data.gov.au/def/roads/ct/RoadName>                            | "Gold Coast"    | "_:1ba3a39c7c1d53e1a8ab145852ec3db3"                                   | <urn:graph:address> |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
```

## Build

```shell
./gradlew clean
./gradlew uberJar
```

## Running with Fuseki in Docker

After building, copy the uber jar in `build/libs` to the `docker` directory.

Within the context of the `docker` directory...

Build the docker compose images.

```shell
docker compose build
```

Run the docker compose services.

```shell
docker compose up -d
```
