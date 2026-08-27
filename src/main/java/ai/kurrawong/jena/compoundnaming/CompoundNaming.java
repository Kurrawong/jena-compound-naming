package ai.kurrawong.jena.compoundnaming;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.SchemaDO;

/**
 * Recursively resolves compound-name parts from a dataset.
 *
 * <p>Value resolution precedence is {@code schema:hasPart}, {@code schema:value},
 * {@code skos:prefLabel}, {@code rdfs:label}, then the focus-node identifier.
 */
public final class CompoundNaming {
    private CompoundNaming() {}

    public static Set<CompoundNamePart> getCompoundNameParts(
            DatasetGraph dataset, List<HasPart> topLevelParts) {
        Map<String, Part> partsMap = new LinkedHashMap<>();

        for (int index = 0; index < topLevelParts.size(); index++) {
            HasPart hasPart = topLevelParts.get(index);
            getCompoundNamePartsInner(
                    Integer.toString(index), hasPart.compoundName(), hasPart.part(), dataset, partsMap);
        }

        Set<CompoundNamePart> retValue = new LinkedHashSet<>();
        for (Part part : partsMap.values()) {
            if (part.rootNode == null) {
                throw new RuntimeException("rootNode is null.");
            }
            Node ids = NodeFactory.createLiteralString(
                    part.ids.stream().map(NodeFmtLib::strNT).collect(Collectors.joining(",")));
            Node types = NodeFactory.createLiteralString(
                    part.types.stream().map(NodeFmtLib::strNT).collect(Collectors.joining(",")));
            if (part.valuePredicate == null || part.value == null) {
                throw new RuntimeException("valuePredicate or value is null.");
            }
            retValue.add(new CompoundNamePart(part.rootNode, ids, types, part.valuePredicate, part.value));
        }

        return retValue;
    }

    private static void getCompoundNamePartsInner(
            String rootId, Node rootNode, Node focusNode, DatasetGraph dataset, Map<String, Part> partsMap) {
        List<Node> sdoParts = objects(dataset, focusNode, SchemaDO.hasPart.asNode());
        if (!sdoParts.isEmpty()) {
            for (int i = 0; i < sdoParts.size(); i++) {
                String newRootId = rootId + "." + (i + 1);
                partsMap.put(newRootId, copyPart(partFor(partsMap, rootId)));
                getCompoundNamePartsInner(newRootId, rootNode, sdoParts.get(i), dataset, partsMap);
            }
            partsMap.remove(rootId);
            return;
        }

        List<Node> sdoValues = objects(dataset, focusNode, SchemaDO.value.asNode());
        if (!sdoValues.isEmpty()) {
            Node value = sdoValues.getFirst();
            List<Node> partAdditionalType = objects(dataset, focusNode, SchemaDO.additionalType.asNode());
            Part part = partFor(partsMap, rootId);
            part.rootNode = rootNode;
            part.ids.add(focusNode);
            part.types.add(partAdditionalType.getFirst());
            partsMap.put(rootId, part);

            if (value.isURI()) {
                getCompoundNamePartsInner(rootId, rootNode, value, dataset, partsMap);
                return;
            }

            part.valuePredicate = SchemaDO.value.asNode();
            part.value = value;
            return;
        }

        if (bindLabel(rootId, rootNode, focusNode, dataset, partsMap, SKOS.prefLabel.asNode())) {
            return;
        }

        if (bindLabel(rootId, rootNode, focusNode, dataset, partsMap, RDFS.label.asNode())) {
            return;
        }

        Part part = partFor(partsMap, rootId);
        part.rootNode = rootNode;
        part.valuePredicate = NodeFactory.createLiteralString("");
        part.value = focusNode;
    }

    private static boolean bindLabel(
            String rootId,
            Node rootNode,
            Node focusNode,
            DatasetGraph dataset,
            Map<String, Part> partsMap,
            Node predicate) {
        List<Node> labels = objects(dataset, focusNode, predicate);
        if (labels.isEmpty()) {
            return false;
        }
        Part part = partFor(partsMap, rootId);
        part.rootNode = rootNode;
        part.ids.add(focusNode);
        part.valuePredicate = predicate;
        part.value = labels.getFirst();
        return true;
    }

    private static Part partFor(Map<String, Part> partsMap, String rootId) {
        return partsMap.computeIfAbsent(rootId, ignored -> new Part());
    }

    private static Part copyPart(Part original) {
        Part copy = new Part();
        copy.rootNode = original.rootNode;
        copy.ids.addAll(original.ids);
        copy.types.addAll(original.types);
        copy.valuePredicate = original.valuePredicate;
        copy.value = original.value;
        return copy;
    }

    private static List<Node> objects(DatasetGraph dataset, Node subject, Node predicate) {
        List<Node> nodes = new ArrayList<>();
        Iterator<Quad> iterator = dataset.find(Node.ANY, subject, predicate, Node.ANY);
        while (iterator.hasNext()) {
            nodes.add(iterator.next().getObject());
        }
        return nodes;
    }

    private static final class Part {
        private Node rootNode;
        private final List<Node> ids = new ArrayList<>();
        private final List<Node> types = new ArrayList<>();
        private Node valuePredicate;
        private Node value;
    }
}
