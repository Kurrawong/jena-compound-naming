package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node

class CompoundName(private val graph: Graph, private var componentQueue: List<Node>) {
    val data = mutableSetOf<Triple<Node, Node, Node>>()

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

    private fun getComponentLiteral(focusNode: Node): Triple<Node, Node, Node> {
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

        return Triple(componentTypes[0].`object`, value.`object`, value.subject)
    }
}