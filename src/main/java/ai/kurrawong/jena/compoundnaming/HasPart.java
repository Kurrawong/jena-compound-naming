package ai.kurrawong.jena.compoundnaming;

import org.apache.jena.graph.Node;

/**
 * One {@code schema:hasPart} link from a Compound Name to a part.
 */
public record HasPart(Node compoundName, Node part) {}
