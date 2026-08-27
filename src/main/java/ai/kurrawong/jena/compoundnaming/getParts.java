package ai.kurrawong.jena.compoundnaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.SchemaDO;

/**
 * SPARQL property function that returns the leaf nodes of a CompoundName object.
 *
 * <p>Example usage:
 * {@code ?iri <https://linked.data.gov.au/def/cn/func/getParts> (?partId ?partType ?partValuePredicate ?partValue) .}
 *
 * <p>Fuseki loads this class with {@code ja:loadClass "ai.kurrawong.jena.compoundnaming.getParts"}.
 */
public class getParts extends PFuncSimpleAndList {
    public static final String GET_PARTS_IRI = "https://linked.data.gov.au/def/cn/func/getParts";

    private static boolean registered = false;

    static {
        register();
    }

    public static void init() {
        register();
    }

    private static synchronized void register() {
        if (registered) {
            return;
        }
        System.out.println("Initializing ai.kurrawong.jena.compoundnaming.getParts property function");
        PropertyFunctionRegistry.get().put(GET_PARTS_IRI, getParts.class);
        registered = true;
    }

    private static boolean bindOrMatch(BindingBuilder builder, Node target, Node value) {
        if (target == null) {
            return false;
        }

        if (target.isVariable()) {
            Var variable = Var.alloc(target);
            Node existing = builder.get(variable);
            if (existing == null) {
                builder.add(variable, value);
                return true;
            }
            return existing.equals(value);
        }

        return target.equals(value);
    }

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);
        if (argObject == null || argObject.getArgListSize() != 4) {
            throw new RuntimeException(
                    "A call to function ai.kurrawong.jena.compoundnaming.getParts must contain 4 arguments.");
        }
    }

    @Override
    public QueryIterator execEvaluated(
            Binding binding, Node subject, Node predicate, PropFuncArg object, ExecutionContext execCxt) {
        if (execCxt == null || execCxt.getActiveGraph() == null) {
            throw new RuntimeException("Active graph is null.");
        }

        if (binding == null) {
            throw new RuntimeException("The binding is null.");
        }

        Graph graph = execCxt.getActiveGraph();
        DatasetGraph dataset = execCxt.getDataset();
        Node subjectSearchNode;
        if (subject == null) {
            subjectSearchNode = Node.ANY;
        } else if (subject.isVariable()) {
            Node bound = binding.get(Var.alloc(subject));
            subjectSearchNode = bound == null ? Node.ANY : bound;
        } else {
            subjectSearchNode = subject;
        }

        List<HasPart> topLevelParts = new ArrayList<>();
        ExtendedIterator<Triple> triples = graph.find(subjectSearchNode, SchemaDO.hasPart.asNode(), Node.ANY);
        try {
            while (triples.hasNext()) {
                Triple triple = triples.next();
                topLevelParts.add(new HasPart(triple.getSubject(), triple.getObject()));
            }
        } finally {
            triples.close();
        }

        Set<CompoundNamePart> parts = CompoundNaming.getCompoundNameParts(dataset, topLevelParts);

        Node[] objectArgs = {
            object.getArg(0), object.getArg(1), object.getArg(2), object.getArg(3)
        };
        if (objectArgs[0] == null || objectArgs[1] == null || objectArgs[2] == null || objectArgs[3] == null) {
            throw new RuntimeException(
                    "A call to function ai.kurrawong.jena.compoundnaming.getParts must contain 4 arguments.");
        }

        List<Binding> bindings = new ArrayList<>();
        for (CompoundNamePart part : parts) {
            BindingBuilder rowBuilder = BindingFactory.builder(binding);
            boolean isCompatible = true;

            if (subject != null) {
                isCompatible = bindOrMatch(rowBuilder, subject, part.rootNode());
            }

            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[0], part.ids());
            }
            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[1], part.types());
            }
            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[2], part.valuePredicate());
            }
            if (isCompatible) {
                isCompatible = bindOrMatch(rowBuilder, objectArgs[3], part.value());
            }

            if (isCompatible) {
                bindings.add(rowBuilder.build());
            }
        }

        return QueryIterPlainWrapper.create(bindings.iterator(), execCxt);
    }
}
