package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
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

class getComponents : PFuncSimpleAndList() {
    companion object {
        @JvmStatic
        fun init() {
            println("Initializing compoundnaming.getComponents property function")
        }
    }

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