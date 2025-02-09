package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.riot.out.NodeFmtLib
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.SchemaDO

data class Part(
    val ids: MutableList<Node>,
    val types: MutableList<Node>,
    var valuePredicate: Node?,
    var value: Node?,
)

typealias PartsMap = MutableMap<String, Part>

fun getCompoundNamePartsInner(
    rootId: String,
    focusNode: Node,
    graph: Graph,
    partsMap: PartsMap,
) {
    val sdoParts = graph.find(focusNode, SchemaDO.hasPart.asNode(), Node.ANY).toList().map { it.`object` }
    if (sdoParts.isNotEmpty()) {
        for ((i, partNode) in sdoParts.withIndex()) {
            val index = i + 1
            val newRootId = "$rootId.$index"
            val part =
                partsMap.getValue(rootId).copy(
                    ids = partsMap.getValue(rootId).ids.toMutableList(),
                    types = partsMap.getValue(rootId).types.toMutableList(),
                )

            partsMap[newRootId] = part
            getCompoundNamePartsInner(newRootId, partNode, graph, partsMap)
        }

        partsMap.remove(rootId)
        return
    }

    val sdoValues = graph.find(focusNode, SchemaDO.value.asNode(), Node.ANY).toList().map { it.`object` }
    if (sdoValues.isNotEmpty()) {
        val value = sdoValues[0]
        val partAdditionalType = graph.find(focusNode, SchemaDO.additionalType.asNode(), Node.ANY).toList()
        val part = partsMap.getValue(rootId)
        part.ids.add(focusNode)
        part.types.add(partAdditionalType.map { it.`object` }.first())
        partsMap[rootId] = part

        if (value.isURI) {
            getCompoundNamePartsInner(rootId, value, graph, partsMap)
            return
        }

        part.valuePredicate = SchemaDO.value.asNode()
        part.value = value
        return
    }

    val skosPrefLabels = graph.find(focusNode, SKOS.prefLabel.asNode(), Node.ANY).toList().map { it.`object` }
    if (skosPrefLabels.isNotEmpty()) {
        val part = partsMap.getValue(rootId)
        part.ids.add(focusNode)
        part.valuePredicate = SKOS.prefLabel.asNode()
        part.value = skosPrefLabels[0]
        return
    }

    val rdfsLabels = graph.find(focusNode, RDFS.label.asNode(), Node.ANY).toList().map { it.`object` }
    if (rdfsLabels.isNotEmpty()) {
        val part = partsMap.getValue(rootId)
        part.ids.add(focusNode)
        part.valuePredicate = RDFS.label.asNode()
        part.value = rdfsLabels[0]
        return
    }
}

fun getCompoundNameParts(
    graph: Graph,
    topLevelParts: List<Node>,
): MutableSet<Quadruple<Node, Node, Node, Node>> {
    val partsMap: PartsMap =
        mutableMapOf<String, Part>().withDefault {
            Part(mutableListOf(), mutableListOf(), null, null)
        }

    for ((index, partNode) in topLevelParts.withIndex()) {
        val rootId = index.toString()
        getCompoundNamePartsInner(rootId, partNode, graph, partsMap)
    }

    val retValue = mutableSetOf<Quadruple<Node, Node, Node, Node>>()
    for (part in partsMap.values) {
        val ids = NodeFactory.createLiteral(part.ids.map { NodeFmtLib.strNT(it) }.joinToString(","))
        val types = NodeFactory.createLiteral(part.types.map { NodeFmtLib.strNT(it) }.joinToString(","))
        val valuePredicate = part.valuePredicate
        val value = part.value
        if (valuePredicate == null || value == null) {
            throw Exception("valuePredicate or value is null.")
        }
        retValue.add(Quadruple(ids, types, valuePredicate, value))
    }

    return retValue
}
