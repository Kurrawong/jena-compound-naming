package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.engine.ExecutionContext
import org.apache.jena.sparql.engine.QueryIterator
import org.apache.jena.sparql.engine.binding.Binding
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper
import org.apache.jena.sparql.pfunction.*
import org.apache.jena.query.*

/**
 * A SPARQL property function to retrieve the leaf nodes of a CompoundName object.
 *
 * Example usage in SPARQL:
 *      ?iri <https://linked.data.gov.au/def/cn/func/getParts> (?partId ?partType ?partValuePredicate ?partValue) .
 */
class getParts : PFuncSimpleAndList() {
    companion object {

        @JvmStatic
        fun init() {
            println("Initializing ai.kurrawong.jena.compoundnaming.getParts property function")
            
            // Register the property function with the IRI
            val PROPERTY_FUNCTION_IRI = "https://linked.data.gov.au/def/cn/func/getParts"
            val registry = PropertyFunctionRegistry.chooseRegistry(ARQ.getContext())
            registry.put(PROPERTY_FUNCTION_IRI, GetPartsPropertyFunctionFactory())
            PropertyFunctionRegistry.set(ARQ.getContext(), registry)
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
            throw Exception("A call to function ai.kurrawong.jena.compoundnaming.getParts must contain 4 arguments.")
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
        val topLevelParts = graph.find(subject, hasPart, Node.ANY).toList().map { it.`object` }
        val compoundName = CompoundName(graph, topLevelParts)

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
            val partId =
                if (quadruple.first.isBlank) "_:B${quadruple.first.blankNodeLabel}" else "<${quadruple.first.uri}>"
            val rowBinding = Binding5(
                binding,
                Var.alloc(subjectVar),
                binding.get(subjectVar),
                Var.alloc(`object`?.getArg(0)?.name),
                NodeFactory.createLiteral(partId, null, null),
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