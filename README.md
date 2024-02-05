# Jena ARQ Compound Naming Property Functions Library

A library of functions for working with data in the Compound Name structure. See the [Compound Naming Model](https://agldwg.github.io/compound-naming-model/model.html) for more information.

## Example code usage

This repo contains a [Main.kt](src/main/kotlin/Main.kt) that loads in [src/main/resources/data.ttl](src/main/resources/data.ttl) into a dataset and registers the Compound Naming Property Functions with Jena.

We can run the following SPARQL query which utilises the `getComponents` property function. This function recursively fetches all the component parts of the compound name instance and returns its type, the value and the component identifier.

Where component identifiers are blank nodes, the internal Jena system identifier is returned. This can be used in tandem with RDF Delta Patch logs to provide updates to blank node objects.

```sparql
SELECT *
WHERE {
    GRAPH ?g {
        BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> AS ?iri)
        ?iri <java:ai.kurrawong.jena.compoundnaming.getComponents> (?componentId ?componentType ?componentValuePredicate ?componentValue) .
    }
}
limit 10
```

And get the following result.

```
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| iri                                                            | componentId                                                            | componentType                                                                 | componentValuePredicate                            | componentValue  | g                   |
========================================================================================================================================================================================================================================================================================================================
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "_:Bfda6114ad800c1af0288290785e39a6d"                                  | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/numberFirst>   | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "2342"          | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "_:Beabaa5eb2b97d6c462bad07a6f93c3a7"                                  | <https://linked.data.gov.au/def/roads/ct/RoadType>                            | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "HWY (Y)"       | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "<https://linked.data.gov.au/dataset/qld-addr/locality-MERMAID-BEACH>" | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/locality>      | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "MERMAID BEACH" | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "_:B6b29dbbe6752d412e5b6b49b22155f40"                                  | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/numberLast>    | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "2358"          | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "<https://linked.data.gov.au/dataset/gnaf/code/levelType/G>"           | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/levelTypeCode> | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "G"             | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "_:Bbafed18729ee5b64b2d70c7a1571a12a"                                  | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/flatNumber>    | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "14"            | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "<https://example.com/flatTypeCode/U>"                                 | <https://w3id.org/profile/anz-address/AnzAddressComponentTypes/flatTypeCode>  | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "U"             | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> | "_:Bca1972342d0c18281993925563061a18"                                  | <https://linked.data.gov.au/def/roads/ct/RoadName>                            | <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> | "Gold Coast"    | <urn:graph:address> |
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
