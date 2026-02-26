import ai.kurrawong.jena.compoundnaming.Quintuple
import ai.kurrawong.jena.compoundnaming.getCompoundNameParts
import ai.kurrawong.jena.compoundnaming.loadModelFromResource
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.apache.jena.vocabulary.RDF
import org.apache.jena.sparql.core.DatasetGraphFactory
import org.apache.jena.vocabulary.SchemaDO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestCompoundNaming {
    @Test
    fun `test2 compound naming`() {
        val model = loadModelFromResource("test.ttl")
        val subject = NodeFactory.createURI("https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213")
        val topLevelParts =
            model.graph
                .find(subject, SchemaDO.hasPart.asNode(), Node.ANY)
                .toList()
                .map { Pair(it.subject, it.`object`) }
        val parts = getCompoundNameParts(DatasetGraphFactory.wrap(model.graph), topLevelParts)
        assertEquals(12, parts.size)

        val modifiedParts =
            parts
                .map {
                    Quintuple(
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(""),
                        it.third,
                        it.fourth,
                        it.fifth,
                    )
                }.toSet()
        val testSet =
            setOf(
                Quintuple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(
                        "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadGivenName>",
                    ),
                    NodeFactory.createURI("https://schema.org/value"),
                    NodeFactory.createLiteralString("Gold Coast"),
                ),
                Quintuple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(
                        "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadType>",
                    ),
                    NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                    NodeFactory.createLiteralLang("Highway", "en"),
                ),
                Quintuple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(
                        "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadSuffix>",
                    ),
                    NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                    NodeFactory.createLiteralLang("East", "en"),
                ),
                Quintuple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString("<https://linked.data.gov.au/def/addr-part-types/subaddressType>"),
                    NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                    NodeFactory.createLiteralLang("Unit", "en"),
                ),
                Quintuple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString("<https://linked.data.gov.au/def/addr-part-types/buildingLevelNumber>"),
                    NodeFactory.createURI("https://schema.org/value"),
                    NodeFactory.createLiteralString("2"),
                ),
            )
        assertTrue(modifiedParts.containsAll(testSet), "modifiedParts: $modifiedParts\ntestSet: $testSet")
    }

    @Test
    fun `fallback uses focus node when no value predicate is found`() {
        val subject = NodeFactory.createURI("https://example.org/name")
        val part = NodeFactory.createBlankNode()
        val fallbackValue = NodeFactory.createURI("https://example.org/no-label-or-value")
        val partType = NodeFactory.createURI("https://example.org/type")

        val model = loadModelFromResource("test.ttl").removeAll()
        model.graph.add(Triple.create(subject, RDF.type.asNode(), NodeFactory.createURI("https://linked.data.gov.au/def/cn/CompoundName")))
        model.graph.add(Triple.create(subject, SchemaDO.hasPart.asNode(), part))
        model.graph.add(Triple.create(part, SchemaDO.additionalType.asNode(), partType))
        model.graph.add(Triple.create(part, SchemaDO.value.asNode(), fallbackValue))

        val topLevelParts = listOf(Pair(subject, part))
        val parts = getCompoundNameParts(DatasetGraphFactory.wrap(model.graph), topLevelParts)

        assertEquals(1, parts.size)
        val only = parts.first()
        assertEquals(NodeFactory.createLiteralString(""), only.fourth)
        assertEquals(fallbackValue, only.fifth)
    }
}
