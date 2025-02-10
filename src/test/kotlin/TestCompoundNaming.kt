import ai.kurrawong.jena.compoundnaming.Quadruple
import ai.kurrawong.jena.compoundnaming.getCompoundNameParts
import ai.kurrawong.jena.compoundnaming.loadModelFromResource
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
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
                .map { it.`object` }
        val parts = getCompoundNameParts(model.graph, topLevelParts)
        assertEquals(12, parts.size)

        val modifiedParts = parts.map { it.copy(NodeFactory.createLiteralString(""), it.second, it.third, it.fourth) }.toSet()
        val testSet =
            setOf(
                Quadruple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(
                        "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadGivenName>",
                    ),
                    NodeFactory.createURI("https://schema.org/value"),
                    NodeFactory.createLiteralString("Gold Coast"),
                ),
                Quadruple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(
                        "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadType>",
                    ),
                    NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                    NodeFactory.createLiteralLang("Highway", "en"),
                ),
                Quadruple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString(
                        "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadSuffix>",
                    ),
                    NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                    NodeFactory.createLiteralLang("East", "en"),
                ),
                Quadruple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString("<https://linked.data.gov.au/def/addr-part-types/subaddressType>"),
                    NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                    NodeFactory.createLiteralLang("Unit", "en"),
                ),
                Quadruple(
                    NodeFactory.createLiteralString(""),
                    NodeFactory.createLiteralString("<https://linked.data.gov.au/def/addr-part-types/buildingLevelNumber>"),
                    NodeFactory.createURI("https://schema.org/value"),
                    NodeFactory.createLiteralString("2"),
                ),
            )
        assertTrue(modifiedParts.containsAll(testSet), "modifiedParts: $modifiedParts\ntestSet: $testSet")
    }
}
