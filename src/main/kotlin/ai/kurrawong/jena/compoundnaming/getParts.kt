package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Node
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.engine.ExecutionContext
import org.apache.jena.sparql.engine.QueryIterator
import org.apache.jena.sparql.engine.binding.Binding
import org.apache.jena.sparql.engine.binding.BindingBuilder
import org.apache.jena.sparql.engine.binding.BindingFactory
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList
import org.apache.jena.sparql.pfunction.PropFuncArg
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry
import org.apache.jena.vocabulary.SchemaDO

/**
 * A SPARQL property function to retrieve the leaf nodes of a CompoundName object.
 *
 * Example usage in SPARQL:
 *      ?iri <https://linked.data.gov.au/def/cn/func/getParts> (?partId ?partType ?partValuePredicate ?partValue) .
 */
class getParts : PFuncSimpleAndList() {
    private fun bindOrMatch(
        builder: BindingBuilder,
        target: Node?,
        value: Node,
    ): Boolean {
        if (target == null) {
            return false
        }

        if (target.isVariable) {
            val variable = Var.alloc(target)
            val existing = builder.get(variable)
            if (existing == null) {
                builder.add(variable, value)
                return true
            }
            return existing == value
        }

        return target == value
    }

    companion object {
        @JvmStatic
        fun init() {
            println("Initializing ai.kurrawong.jena.compoundnaming.getParts property function")

            // Register the property function with the IRI
            PropertyFunctionRegistry.get().put(
                "https://linked.data.gov.au/def/cn/func/getParts",
                GetPartsPropertyFunctionFactory(),
            )
        }
    }

    override fun build(
        argSubject: PropFuncArg?,
        predicate: Node?,
        argObject: PropFuncArg?,
        execCxt: ExecutionContext?,
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
        execCxt: ExecutionContext?,
    ): QueryIterator {
        if (execCxt?.activeGraph == null) {
            throw Exception("Active graph is null.")
        }

        if (binding == null) {
            throw Exception("The binding is null.")
        }

        var subjectVar: Var? = null
        val vars = binding.vars()
        if (vars != null) {
            while (vars.hasNext()) {
                subjectVar = vars.next()
            }
        }

        val graph = execCxt.activeGraph
        val dataset = execCxt.dataset
        val topLevelParts =
            graph
                .find(
                    if (subjectVar !=
                        null
                    ) {
                        subject
                    } else {
                        Node.ANY
                    },
                    SchemaDO.hasPart.asNode(),
                    Node.ANY,
                ).toList()
                .map { Pair(it.subject, it.`object`) }
        val parts = getCompoundNameParts(dataset, topLevelParts)

        subjectVar = subjectVar ?: (subject as? Var)
        val objectArgs =
            listOf(
                `object`?.getArg(0),
                `object`?.getArg(1),
                `object`?.getArg(2),
                `object`?.getArg(3),
            )
        if (objectArgs.any { it == null }) {
            throw Exception("A call to function ai.kurrawong.jena.compoundnaming.getParts must contain 4 arguments.")
        }

        val bindings = mutableListOf<Binding>()
        for (part in parts) {
            val rowBuilder = BindingFactory.builder(binding)
            var isCompatible = true

            if (subjectVar != null) {
                val subjectValue = binding.get(subjectVar) ?: part.first
                isCompatible = bindOrMatch(rowBuilder, subjectVar, subjectValue)
            }

            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[0], part.second)
            }
            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[1], part.third)
            }
            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[2], part.fourth)
            }
            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[3], part.fifth)
            }

            if (isCompatible) {
                bindings.add(rowBuilder.build())
            }
        }

        return QueryIterPlainWrapper.create(bindings.iterator(), execCxt)
    }
}
