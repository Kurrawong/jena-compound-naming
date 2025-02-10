# Jena ARQ Compound Naming Property Functions Library

A library of Jena ARQ SPARQL functions for working with the [Compound Naming Model](https://agldwg.github.io/compound-naming-model/model.html).

## Example code usage

See [Main.kt](src/main/kotlin/Main.kt) that loads in [src/main/resources/data.ttl](src/main/resources/data.ttl) as an example of how to call the `getParts` function.

Given a subject that's a `cn:CompoundName`, recursively retrieve all of its parts that make up the compounded name. The result is a flattened set of "part" rows.

Where part identifiers are blank nodes, the internal Jena system identifier is returned. This can be used in tandem with RDF Delta Patch logs to provide updates to blank node objects.

The following example executes the SPARQL query on the data file [src/test/resources/test.ttl](src/test/resources/test.ttl).

```sparql
PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
PREFIX sdo: <https://schema.org/>

SELECT *
WHERE {
    GRAPH ?graph {
        BIND(<https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> AS ?iri)
        ?iri cnf:getParts (?partIds ?partTypes ?partValuePredicate ?partValue) .
    }
}
limit 100
```

And get the following result.

```
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| iri                                                                                        | partIds                                                                                                                       | partTypes                                                                                                                        | partValuePredicate                              | partValue          | graph               |
==============================================================================================================================================================================================================================================================================================================================================================================================================================================================
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:B59a79be87f24b1277e2bb931a0f8eeb9,<https://sws.geonames.org/2152274/>"                                                     | "<https://linked.data.gov.au/def/addr-part-types/stateOrTerritory>"                                                              | <http://www.w3.org/2004/02/skos/core#prefLabel> | "Queensland"       | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:B5d16eed0500317876b5d3a5fa65d78cb,<https://linked.data.gov.au/def/building-level-types/level>"                             | "<https://linked.data.gov.au/def/addr-part-types/buildingLevelType>"                                                             | <http://www.w3.org/2004/02/skos/core#prefLabel> | "Level"@en         | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:Ba0b0ef77958454f56aa8ec9984ea8ad9,<https://sws.geonames.org/2077456/>"                                                     | "<https://linked.data.gov.au/def/addr-part-types/countryName>"                                                                   | <http://www.w3.org/2004/02/skos/core#prefLabel> | "Australia"        | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:Bb17d99a769979cef91ad8a0f72190f7d"                                                                                         | "<https://linked.data.gov.au/def/addr-part-types/addressNumberFirst>"                                                            | sdo:value                                       | "2342"             | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:B4d1761a1a45e72f230fabd8408c35471"                                                                                         | "<https://linked.data.gov.au/def/addr-part-types/buildingLevelNumber>"                                                           | sdo:value                                       | "2"                | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:B4e0f4c6affcd704d2e4cde9f9b9d9941,_:B3e31b36c120cd161fde88a18bae21ef2"                                                     | "<https://linked.data.gov.au/def/addr-part-types/locality>,<https://linked.data.gov.au/def/gn-part-types/geographicalGivenName>" | sdo:value                                       | "Mermaid Beach"@en | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:Bc8096e4ab5eb56f2f3f7a2fe618cef5f"                                                                                         | "<https://linked.data.gov.au/def/addr-part-types/subaddressNumber>"                                                              | sdo:value                                       | "138"              | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:B9d0329cf15b3ce6d9392ab3d2ecb2a8c,_:B5f8caaa63667550846c3635b1d21fca4,<https://linked.data.gov.au/def/gn-affix/east>"      | "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadSuffix>"         | <http://www.w3.org/2004/02/skos/core#prefLabel> | "East"@en          | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:B9d0329cf15b3ce6d9392ab3d2ecb2a8c,_:B41f8bdd23fb026ae74f07176ad04dd54,<https://linked.data.gov.au/def/road-types/highway>" | "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadType>"           | <http://www.w3.org/2004/02/skos/core#prefLabel> | "Highway"@en       | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:B9d0329cf15b3ce6d9392ab3d2ecb2a8c,_:B127c3d24337b26f8a12eec4236564898"                                                     | "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadGivenName>"      | sdo:value                                       | "Gold Coast"       | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:Bcec1397ddf415108c02e9946edc58a84,<https://linked.data.gov.au/def/subaddress-types/unit>"                                  | "<https://linked.data.gov.au/def/addr-part-types/subaddressType>"                                                                | <http://www.w3.org/2004/02/skos/core#prefLabel> | "Unit"@en          | <urn:graph:address> |
| <https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> | "_:Bd22078ba7459a5450c12ea21924e3c3a"                                                                                         | "<https://linked.data.gov.au/def/addr-part-types/addressNumberLast>"                                                             | sdo:value                                       | "2358"             | <urn:graph:address> |
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
```

- `iri` - the top-level compound name.
- `partIds` - the path of each resource's identifier to the leaf node.
- `partTypes` - the path of each resource's part type to the leaf node.
- `partValuePredicate` - the predicate of the leaf node value.
  - The algorithm searches for `sdo:hasPart`, `sdo:value`, `skos:prefLabel`, and `rdfs:label` on each node recursively.
  - If no values were found, it sets itself to an empty literal string and sets the `partValue` to the focus node's identifier.
- `partValue` - the leaf node value. Always a literal unless a valid part value predicate was not found, then it returns the focus node identifier.

## Build

```shell
task build
```

## Running with Fuseki in Docker

After building, copy the uber jar in `build/libs` to the `docker` directory.

Within the context of the `docker` directory...

Build the docker compose images.

```shell
task docker:build
```

Run the docker compose services.

```shell
task docker:up
```

See [Taskfile.yml](Taskfile.yml) for more details.
