package ai.kurrawong.jena.compoundnaming;

import org.apache.jena.graph.Node;

/**
 * One flattened compound-name part row: the root subject, the identifier path,
 * the type path, the leaf value predicate, and the leaf value.
 */
public record CompoundNamePart(
        Node rootNode, Node ids, Node types, Node valuePredicate, Node value) {}
