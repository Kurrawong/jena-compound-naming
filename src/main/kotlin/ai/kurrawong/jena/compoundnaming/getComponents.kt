package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.engine.ExecutionContext
import org.apache.jena.sparql.engine.QueryIterator
import org.apache.jena.sparql.engine.binding.Binding
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper
import org.apache.jena.sparql.pfunction.*

val hasPart: Node = NodeFactory.createURI("https://schema.org/hasPart")
val hasValue: Node = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#value")
val sdoName: Node = NodeFactory.createURI("https://schema.org/name")
val additionalType: Node = NodeFactory.createURI("https://schema.org/additionalType")

/**
 * A SPARQL property function to retrieve the leaf nodes of a CompoundName object.
 *
 * Example usage in SPARQL:
 *      ?iri <java:ai.kurrawong.jena.compoundnaming.getComponents> (?componentId ?componentType ?componentValuePredicate ?componentValue) .
 */
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
        if (argObject?.argListSize != 4) {
            throw Exception("A call to function ai.kurrawong.jena.compoundnaming.getComponents must contain 4 arguments.")
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

        if (binding == null)
            throw Exception("The binding is null.")

        val graph = execCxt.activeGraph
        val addrComponents = graph.find(subject, hasPart, Node.ANY).toList().map { it.`object` }
        val compoundName = CompoundName(graph, addrComponents)

        var subjectVar: Var? = null
        val vars = binding.vars()
        if (vars != null) {
            while (vars.hasNext()) {
                subjectVar = vars.next()
            }
        }
        if (subjectVar == null) {
            throw Exception("The subject variable is null.")
        }

        val bindings = mutableListOf<Binding>()
        for (quadruple in compoundName.data.iterator()) {
            val componentId =
                if (quadruple.first.isBlank) "_:B${quadruple.first.blankNodeLabel}" else "<${quadruple.first.uri}>"
            val rowBinding = Binding5(
                binding,
                Var.alloc(subjectVar),
                binding.get(subjectVar),
                Var.alloc(`object`?.getArg(0)?.name),
                NodeFactory.createLiteral(componentId),
                Var.alloc(`object`?.getArg(1)?.name),
                quadruple.second,
                Var.alloc(`object`?.getArg(2)?.name),
                quadruple.third,
                Var.alloc(`object`?.getArg(3)?.name),
                quadruple.fourth
            )
            bindings.add(rowBinding)
        }

        return QueryIterPlainWrapper.create(bindings.iterator(), execCxt)
    }
}