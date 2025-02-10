package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.riot.out.NodeFmtLib
import org.apache.jena.sparql.core.DatasetGraph
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
                    ids = partsMap.getValue(rootId).ids.toMutableList(),
                    types = partsMap.getValue(rootId).types.toMutableList(),
                )

            partsMap[newRootId] = part
            getCompoundNamePartsInner(newRootId, partNode, dataset, partsMap)
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
        part.ids.add(focusNode)
        part.types.add(partAdditionalType.map { it.`object` }.first())
        partsMap[rootId] = part

        if (value.isURI) {
            getCompoundNamePartsInner(rootId, value, dataset, partsMap)
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
        part.ids.add(focusNode)
        part.valuePredicate = RDFS.label.asNode()
        part.value = rdfsLabels[0]
        return
    }

    // Reaching here means we didn't find a value. Save the value as the focus node instead.
    val part = partsMap.getValue(rootId)
    part.valuePredicate = NodeFactory.createLiteralString("")
    part.value = focusNode
}

fun getCompoundNameParts(
    dataset: DatasetGraph,
    topLevelParts: List<Node>,
): MutableSet<Quadruple<Node, Node, Node, Node>> {
    val partsMap: PartsMap =
        mutableMapOf<String, Part>().withDefault {
            Part(mutableListOf(), mutableListOf(), null, null)
        }

    for ((index, partNode) in topLevelParts.withIndex()) {
        val rootId = index.toString()
        getCompoundNamePartsInner(rootId, partNode, dataset, partsMap)
    }

    val retValue = mutableSetOf<Quadruple<Node, Node, Node, Node>>()
    for (part in partsMap.values) {
        val ids = NodeFactory.createLiteralString(part.ids.map { NodeFmtLib.strNT(it) }.joinToString(","))
        val types = NodeFactory.createLiteralString(part.types.map { NodeFmtLib.strNT(it) }.joinToString(","))
        val valuePredicate = part.valuePredicate
        val value = part.value
        if (valuePredicate == null || value == null) {
            throw Exception("valuePredicate or value is null.")
        }
        retValue.add(Quadruple(ids, types, valuePredicate, value))
    }

    return retValue
}
