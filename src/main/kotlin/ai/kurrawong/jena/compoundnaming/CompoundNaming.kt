package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.riot.out.NodeFmtLib
import org.apache.jena.sparql.core.DatasetGraph
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.SchemaDO

data class Part(
    var rootNode: Node?,
    val ids: MutableList<Node>,
    val types: MutableList<Node>,
    var valuePredicate: Node?,
    var value: Node?,
)

typealias PartsMap = MutableMap<String, Part>

fun getCompoundNamePartsInner(
    rootId: String,
    rootNode: Node,
    focusNode: Node,
    dataset: DatasetGraph,
    partsMap: PartsMap,
) {
    val sdoParts =
        dataset
            .find(Node.ANY, focusNode, SchemaDO.hasPart.asNode(), Node.ANY)
            .asSequence()
            .toList()
            .map { it.`object` }
    if (sdoParts.isNotEmpty()) {
        for ((i, partNode) in sdoParts.withIndex()) {
            val index = i + 1
            val newRootId = "$rootId.$index"
            val part =
                partsMap.getValue(rootId).copy(
                    rootNode = partsMap.getValue(rootId).rootNode,
                    ids = partsMap.getValue(rootId).ids.toMutableList(),
                    types = partsMap.getValue(rootId).types.toMutableList(),
                )

            partsMap[newRootId] = part
            getCompoundNamePartsInner(newRootId, rootNode, partNode, dataset, partsMap)
        }

        partsMap.remove(rootId)
        return
    }

    val sdoValues =
        dataset
            .find(Node.ANY, focusNode, SchemaDO.value.asNode(), Node.ANY)
            .asSequence()
            .toList()
            .map { it.`object` }
    if (sdoValues.isNotEmpty()) {
        val value = sdoValues[0]
        val partAdditionalType = dataset.find(Node.ANY, focusNode, SchemaDO.additionalType.asNode(), Node.ANY).asSequence().toList()
        val part = partsMap.getValue(rootId)
        part.rootNode = rootNode
        part.ids.add(focusNode)
        part.types.add(partAdditionalType.map { it.`object` }.first())
        partsMap[rootId] = part

        if (value.isURI) {
            getCompoundNamePartsInner(rootId, rootNode, value, dataset, partsMap)
            return
        }

        part.valuePredicate = SchemaDO.value.asNode()
        part.value = value
        return
    }

    val skosPrefLabels =
        dataset
            .find(Node.ANY, focusNode, SKOS.prefLabel.asNode(), Node.ANY)
            .asSequence()
            .toList()
            .map { it.`object` }
    if (skosPrefLabels.isNotEmpty()) {
        val part = partsMap.getValue(rootId)
        part.rootNode = rootNode
        part.ids.add(focusNode)
        part.valuePredicate = SKOS.prefLabel.asNode()
        part.value = skosPrefLabels[0]
        return
    }

    val rdfsLabels =
        dataset
            .find(Node.ANY, focusNode, RDFS.label.asNode(), Node.ANY)
            .asSequence()
            .toList()
            .map { it.`object` }
    if (rdfsLabels.isNotEmpty()) {
        val part = partsMap.getValue(rootId)
        part.rootNode = rootNode
        part.ids.add(focusNode)
        part.valuePredicate = RDFS.label.asNode()
        part.value = rdfsLabels[0]
        return
    }

    // Reaching here means we didn't find a value. Save the value as the focus node instead.
    val part = partsMap.getValue(rootId)
    part.rootNode = rootNode
    part.valuePredicate = NodeFactory.createLiteralString("")
    part.value = focusNode
}

fun getCompoundNameParts(
    dataset: DatasetGraph,
    topLevelParts: List<Pair<Node, Node>>,
): MutableSet<Quintuple<Node, Node, Node, Node, Node>> {
    val partsMap: PartsMap =
        mutableMapOf<String, Part>().withDefault {
            Part(null, mutableListOf(), mutableListOf(), null, null)
        }

    for ((index, partPair) in topLevelParts.withIndex()) {
        val rootId = index.toString()
        getCompoundNamePartsInner(rootId, partPair.first, partPair.second, dataset, partsMap)
    }

    val retValue = mutableSetOf<Quintuple<Node, Node, Node, Node, Node>>()
    for (part in partsMap.values) {
        val rootNode = part.rootNode ?: throw Exception("rootNode is null.")
        val ids = NodeFactory.createLiteralString(part.ids.map { NodeFmtLib.strNT(it) }.joinToString(","))
        val types = NodeFactory.createLiteralString(part.types.map { NodeFmtLib.strNT(it) }.joinToString(","))
        val valuePredicate = part.valuePredicate
        val value = part.value
        if (valuePredicate == null || value == null) {
            throw Exception("valuePredicate or value is null.")
        }
        retValue.add(Quintuple(rootNode, ids, types, valuePredicate, value))
    }

    return retValue
}
