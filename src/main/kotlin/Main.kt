/* Proof of concept of a Jena ARQ Compound Naming SPARQL Property Function
*
* An initial working version of a Compound Naming Property Function `getLiteralComponents` working on an in-memory
* Jena dataset.
* */

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.query.*
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.engine.ExecutionContext
import org.apache.jena.sparql.engine.QueryIterator
import org.apache.jena.sparql.engine.binding.Binding
import org.apache.jena.sparql.engine.binding.BindingFactory
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper
import org.apache.jena.sparql.pfunction.*

val hasAddress: Node = NodeFactory.createURI("https://w3id.org/profile/anz-address/hasAddress")
val hasPart: Node = NodeFactory.createURI("https://schema.org/hasPart")
val hasValue: Node = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#value")
val sdoName: Node = NodeFactory.createURI("https://schema.org/name")
val additionalType: Node = NodeFactory.createURI("https://schema.org/additionalType")

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
            throw Exception("Focus node $focusNode did not have any values for rdf:value.")
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

class GetLiteralComponentsPropertyFunction : PropertyFunctionFactory {
    override fun create(uri: String): PropertyFunction {
        return object : PFuncSimpleAndList() {
            override fun build(
                argSubject: PropFuncArg?,
                predicate: Node?,
                argObject: PropFuncArg?,
                execCxt: ExecutionContext?
            ) {
                super.build(argSubject, predicate, argObject, execCxt)
                if (argObject?.argListSize != 3) {
                    throw Exception("A call to function <https://linked.data.gov.au/def/cn/func/getLiteralComponents> must contain 3 arguments.")
                }
            }

            override fun execEvaluated(
                binding: Binding?,
                subject: Node?,
                predicate: Node?,
                `object`: PropFuncArg?,
                execCxt: ExecutionContext?
            ): QueryIterator {
                if (execCxt?.activeGraph == null) {
                    throw Exception("Active graph is null.")
                }

                val graph = execCxt.activeGraph
                val result = graph.find(subject, hasAddress, Node.ANY).toList()

                if (result.size < 1) {
                    return QueryIterNullIterator(execCxt)
                }

                val address = result[0].`object`
                val addrComponents = graph.find(address, hasPart, Node.ANY).toList().map { it.`object` }
                val compoundName = CompoundName(graph, addrComponents)

                var subjectVar: Var? = null
                val vars = binding?.vars()
                if (vars != null) {
                    while (vars.hasNext()) {
                        subjectVar = vars.next()
                        break
                    }
                }
                if (subjectVar == null) {
                    throw Exception("The subject variable is null.")
                }

                val bindings = mutableListOf<Binding>()
                for (triple in compoundName.data.iterator()) {
                    val componentId =
                        if (triple.third.isBlank) "_:${triple.third.blankNodeLabel}" else "<${triple.third.uri}>"
                    val rowBinding = BindingFactory.binding(
                        Var.alloc(subjectVar),
                        binding?.get(subjectVar),
                        Var.alloc(`object`?.getArg(0)?.name),
                        triple.first,
                        Var.alloc(`object`?.getArg(1)?.name),
                        triple.second,
                        Var.alloc(`object`?.getArg(2)?.name),
                        NodeFactory.createLiteral(componentId),
                    )
                    bindings.add(rowBinding)
                }

                return QueryIterPlainWrapper.create(bindings.iterator(), execCxt)
            }
        }
    }
}

fun main() {
    val propertyFunctionRegistry = PropertyFunctionRegistry.chooseRegistry(ARQ.getContext())
    propertyFunctionRegistry.put(
        "https://linked.data.gov.au/def/cn/func/getLiteralComponents",
        GetLiteralComponentsPropertyFunction()
    )
    PropertyFunctionRegistry.set(ARQ.getContext(), propertyFunctionRegistry)

    val reader = object {}.javaClass.getResourceAsStream("data.ttl")?.bufferedReader()
    val reader2 = object {}.javaClass.getResourceAsStream("data2.ttl")?.bufferedReader()

    val dataset = DatasetFactory.createTxnMem()
    val model = ModelFactory.createDefaultModel()
    model.read(reader, "https://example.com/", "TURTLE")
    dataset.addNamedModel("urn:graph:address", model)

    val model2 = ModelFactory.createDefaultModel().read(
        "https://cdn.jsdelivr.net/gh/kurrawong/exem-ont@bf22aa548c635f43f221df7362d8ddc2d99edcc7/exem.ttl",
        "TURTLE"
    )
    dataset.addNamedModel("urn:graph:other", model2)

    val model3 = dataset.defaultModel
    model3.read(reader2, "https://example.com/", "TURTLE")

//    val queryString = """
//PREFIX func: <https://linked.data.gov.au/def/cn/func/>
//
//SELECT *
//WHERE {
//    GRAPH ?g {
//        BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-obj-1075435> AS ?iri)
//        ?iri func:getLiteralComponents (?componentType ?componentValue ?componentId) .
//    }
//}
//limit 10
//            """.trimIndent()

    val queryString = """
PREFIX func: <https://linked.data.gov.au/def/cn/func/>

SELECT *
WHERE {
    BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-obj-33254> AS ?iri)
    ?iri func:getLiteralComponents (?componentType ?componentValue ?componentId) .
}
limit 10
            """.trimIndent()

    val query = QueryFactory.create(queryString)

    QueryExecutionFactory.create(query, dataset).use { qexec ->
        val results = qexec.execSelect()
        ResultSetFormatter.out(System.out, results, query)
    }

//    val query2 = QueryFactory.create(
//        """
//        DESCRIBE <https://linked.data.gov.au/dataset/qld-addr/addr-1075435>
//    """.trimIndent()
//    )
//
//    QueryExecutionFactory.create(query2, dataset).use { queryExecution ->
//        val results = queryExecution.execDescribe()
//        results.write(System.out, "TURTLE")
//    }
}