package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import java.io.Serializable

/**
 * Represents a 4-tuple.
 *
 * Same as a Pair or a Triple in Kotlin stdlib but for 4 values.
 */
data class Quadruple<A,B,C,D>(var first: A, var second: B, var third: C, var fourth: D): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

class CompoundName(private val graph: Graph, private var componentQueue: List<Node>) {
    val data = mutableSetOf<Quadruple<Node, Node, Node, Node>>()

    init {
        while (componentQueue.isNotEmpty()) {
            val startingNode = componentQueue[0]
            componentQueue = componentQueue.drop(1)

            val value = getComponentLiteral(startingNode)
            data.add(value)

            if (componentQueue.isEmpty()) {
                break
            }
        }
    }

    /**
     * Return a Quadruple data structure containing:
     * - first - the component identifier - an IRI or internal system identifier as a blank node
     * - second - the component type - an IRI
     * - third - the component's predicate which contains the value
     * - fourth - the component's value
     */
    private fun getComponentLiteral(focusNode: Node): Quadruple<Node, Node, Node, Node> {
        val result = graph.find(focusNode, hasValue, Node.ANY).toList()
        var sdoNames = graph.find(focusNode, sdoName, Node.ANY).toList().map { triple -> triple.`object` }
        var hasParts = graph.find(focusNode, hasPart, Node.ANY).toList().map { triple -> triple.`object` }

        if (hasParts.isNotEmpty()) {
            val hasPartNode = hasParts[0]
            if (hasParts.size > 1) {
                hasParts = hasParts.drop(1)
                componentQueue += hasParts
            }
            return getComponentLiteral(hasPartNode)
        }

        if (sdoNames.isNotEmpty()) {
            val sdoNameNode = sdoNames[0]
            if (sdoNames.size > 1) {
                sdoNames = sdoNames.drop(1)
                componentQueue += sdoNames
            }
            return getComponentLiteral(sdoNameNode)
        }

        if (result.isEmpty()) {
            throw Exception("Focus node $focusNode does not have any values for rdf:value.")
        }

        val value = result[0]

        if (value.`object`.isURI) {
            return getComponentLiteral(value.`object`)
        }

        val componentTypes = graph.find(focusNode, additionalType, Node.ANY).toList()

        if (componentTypes.isEmpty()) {
            throw Exception("Focus node $focusNode does not have a component type.")
        }

        return Quadruple(value.subject, componentTypes[0].`object`,  value.predicate, value.`object`)
    }
}