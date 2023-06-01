/* Proof of concept of a Jena ARQ Compound Naming SPARQL Property Function
*
* An initial working version of a Compound Naming Property Function `getLiteralComponents` working on an in-memory
* Jena dataset.
* */

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.query.*
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.engine.ExecutionContext
import org.apache.jena.sparql.engine.QueryIterator
import org.apache.jena.sparql.engine.binding.Binding
import org.apache.jena.sparql.engine.binding.BindingFactory
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper
import org.apache.jena.sparql.pfunction.*

val hasAddress = NodeFactory.createURI("https://w3id.org/profile/anz-address/hasAddress")
val hasPart = NodeFactory.createURI("https://schema.org/hasPart")
val hasValue = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#value")
val sdoName = NodeFactory.createURI("https://schema.org/name")
val additionalType = NodeFactory.createURI("https://schema.org/additionalType")

class CompoundName(private val graph: Graph, private var componentQueue: List<Node>) {
    val data = mutableSetOf<Pair<Node, Node>>()

    init {
        while (componentQueue.isNotEmpty()) {
            val startingNode = this.componentQueue[0]
            componentQueue = this.componentQueue.drop(1)

            val value = getComponentLiteral(startingNode)
            data.add(value)

            if (this.componentQueue.isEmpty()) {
                break
            }
        }
    }

    private fun getComponentLiteral(focusNode: Node) : Pair<Node, Node> {
        val result = graph.find(focusNode, hasValue, Node.ANY).toList()
        var sdoNames = graph.find(focusNode, sdoName, Node.ANY).toList().map { triple -> triple.`object` }
        var hasParts = graph.find(focusNode, hasPart, Node.ANY).toList().map { triple -> triple.`object` }

        if (hasParts.isNotEmpty()) {
            val hasPartNode = hasParts[0]
            if (hasParts.size > 1) {
                hasParts = hasParts.drop(1)
                this.componentQueue = componentQueue + hasParts
            }
            return getComponentLiteral(hasPartNode)
        }

        if (sdoNames.isNotEmpty()) {
            val sdoNameNode = sdoNames[0]
            if (sdoNames.size > 1) {
                sdoNames = sdoNames.drop(1)
                this.componentQueue = componentQueue + sdoNames
            }
            return getComponentLiteral(sdoNameNode)
        }

        if (result.isEmpty()) {
            throw Exception("Focus node $focusNode did not have any values for sdo:value.")
        }

        val value = result[0]

        if (value.`object`.isURI) {
            return getComponentLiteral(value.`object`)
        }

        val componentTypes = graph.find(focusNode, additionalType, Node.ANY).toList()

        if (componentTypes.isEmpty()) {
            throw Exception("Focus node $focusNode does not have a component type.")
        }

        return componentTypes[0].`object` to value.`object`
    }
}

class GetLiteralComponentsPropertyFunction: PropertyFunctionFactory {
    override fun create(uri: String): PropertyFunction {
        return object : PFuncListAndSimple() {
            override fun execEvaluated(
                binding: Binding?,
                subject: PropFuncArg?,
                predicate: Node?,
                `object`: Node?,
                execCxt: ExecutionContext?
            ): QueryIterator {
                val graph = execCxt?.dataset?.defaultGraph

                if (graph == null) {
                    throw Exception("Graph $graph is null.")
                }

                val focusNode = subject?.arg
                val vars = binding?.vars()?.asSequence()?.toList() ?: throw Exception("Vars is null.")

                if (vars.size != 1) {
                    throw Exception("Vars is null or does not have a length of 1.")
                }

                val focusNodeVar = vars[0]

                val result = graph.find(focusNode, hasAddress, Node.ANY).toList()

                if (result.size != 1) {
                    return QueryIterNullIterator(execCxt)
                }

                val address = result[0].`object`
                val addrComponents = graph.find(address, hasPart, Node.ANY).toList().map { it.`object` }
                val compoundName = CompoundName(graph, addrComponents)

                val bindings = mutableListOf<Binding>()
                for (pair in compoundName.data.iterator()) {
                    // TODO: Figure out how to bind to vars used in the SPARQL query. Write now it is hardcoded to componentType and componentValue.
                    // It is definitely possible because it is working in the apf:splitIRI implementation.
                    // https://github.com/apache/jena/blob/main/jena-arq/src/main/java/org/apache/jena/sparql/pfunction/library/splitIRI.java
                    val rowBinding = BindingFactory.binding(Var.alloc(focusNodeVar), focusNode, Var.alloc("componentType"), pair.first, Var.alloc("componentValue"), pair.second)
                    bindings.add(rowBinding)
                }

                return QueryIterPlainWrapper.create(bindings.iterator(), execCxt)
            }
        }
    }
}

fun main() {
    val propertyFunctionRegistry = PropertyFunctionRegistry.chooseRegistry(ARQ.getContext())
    propertyFunctionRegistry.put("urn:func/getLiteralComponents", GetLiteralComponentsPropertyFunction())
    PropertyFunctionRegistry.set(ARQ.getContext(), propertyFunctionRegistry)

    val reader = object {}.javaClass.getResourceAsStream("data.ttl")?.bufferedReader()

    val dataset = DatasetFactory.createTxnMem()
    val model = dataset.defaultModel
    model.read(reader, "https://example.com/", "TURTLE")

    val queryString = """
PREFIX func: <urn:func/>

SELECT ?iri ?componentType ?componentValue
WHERE {
    BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-obj-1075435> AS ?iri)
    ?iri func:getLiteralComponents (?componentType ?componentValue) .
}
limit 10
            """.trimIndent()
    val query = QueryFactory.create(queryString)

    QueryExecutionFactory.create(query, dataset).use { qexec ->
        val results = qexec.execSelect()
        ResultSetFormatter.out(System.out, results, query)
    }
}