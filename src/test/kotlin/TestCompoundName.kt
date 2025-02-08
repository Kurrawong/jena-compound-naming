import ai.kurrawong.jena.compoundnaming.CompoundName
import ai.kurrawong.jena.compoundnaming.hasPart
import ai.kurrawong.jena.compoundnaming.loadModelFromResource
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory

import kotlin.test.Test
import kotlin.test.assertEquals

class TestCompoundName {
    @Test
    fun `test1 compound name`() {
        val model = loadModelFromResource("test.ttl")
        val subject = NodeFactory.createURI("https://linked.data.gov.au/dataset/qld-addr/addr-2522774")
        val topLevelParts = model.graph.find(subject, hasPart, Node.ANY).toList().map { it.`object` }
        val compoundName = CompoundName(model.graph, topLevelParts)
        assertEquals(7, compoundName.data.size)
        for (quadruple in compoundName.data.iterator()) {
            println(quadruple)
        }
    }

    @Test
    fun `test2 compound name`() {
        val model = loadModelFromResource("test2.ttl")
        val subject = NodeFactory.createURI("https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213")
        val topLevelParts = model.graph.find(subject, hasPart, Node.ANY).toList().map { it.`object` }
        val compoundName = CompoundName(model.graph, topLevelParts)
        assertEquals(12, compoundName.data.size)
        for (quadruple in compoundName.data.iterator()) {
            println(quadruple)
        }
    }
}