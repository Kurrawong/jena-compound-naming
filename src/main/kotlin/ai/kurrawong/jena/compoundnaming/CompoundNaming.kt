package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory

val hasPart: Node = NodeFactory.createURI("https://schema.org/hasPart")
val hasValue: Node = NodeFactory.createURI("https://schema.org/value")
val sdoName: Node = NodeFactory.createURI("https://schema.org/name")
val additionalType: Node = NodeFactory.createURI("https://schema.org/additionalType")

class CompoundName(private val graph: Graph, private var topLevelParts: List<Node>) {
    val data = mutableSetOf<Quadruple<Node, Node, Node, Node>>()
    val partsQueue = topLevelParts.toMutableList()

    init {
        while (partsQueue.isNotEmpty()) {
            val partNode = partsQueue.removeFirst()

            val value = getComponentLiteral(partNode)
            data.add(value)

            if (partsQueue.isEmpty()) {
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
                partsQueue.addAll(hasParts)
            }
            return getComponentLiteral(hasPartNode)
        }

        if (sdoNames.isNotEmpty()) {
            val sdoNameNode = sdoNames[0]
            if (sdoNames.size > 1) {
                sdoNames = sdoNames.drop(1)
                partsQueue.addAll(sdoNames)
            }
            return getComponentLiteral(sdoNameNode)
        }

        if (result.isEmpty()) {
            throw Exception("Focus node $focusNode does not have any values for rdf:value.")
        }

        // Always get just one value. Multiple values found is undefined behaviour.
        val value = result[0]

        if (value.`object`.isURI) {
            try {
                return getComponentLiteral(value.`object`)
            }
            catch (_: Exception) {
                // Catch the exception thrown when no rdf:value is found on value.object.
                // Use this as the value instead.
                // Applications using this function will need to check whether any componentValue values are IRIs and
                // handle it by deriving the label for the object in some other way.
            }
        }

        val componentTypes = graph.find(focusNode, additionalType, Node.ANY).toList()

        if (componentTypes.isEmpty()) {
            throw Exception("Focus node $focusNode does not have a component type.")
        }

        return Quadruple(value.subject, componentTypes[0].`object`,  value.predicate, value.`object`)
    }
}