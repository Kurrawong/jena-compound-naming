package ai.kurrawong.jena.compoundnaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SchemaDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompoundNamingTest {
    @Test
    @DisplayName("a known Compound Name returns the expected part rows (partTypes, partValuePredicate, partValue)")
    void knownSubjectReturnsExpectedPartRows() {
        Model model = TestResources.loadModelFromResource("test.ttl");
        Node subject = NodeFactory.createURI(
                "https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213");
        List<HasPart> topLevelParts = model.getGraph()
                .find(subject, SchemaDO.hasPart.asNode(), Node.ANY)
                .toList()
                .stream()
                .map(triple -> new HasPart(triple.getSubject(), triple.getObject()))
                .toList();
        Set<CompoundNamePart> parts =
                CompoundNaming.getCompoundNameParts(DatasetGraphFactory.wrap(model.getGraph()), topLevelParts);
        assertEquals(12, parts.size());

        Set<CompoundNamePart> modifiedParts = parts.stream()
                .map(part -> new CompoundNamePart(
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(""),
                        part.types(),
                        part.valuePredicate(),
                        part.value()))
                .collect(Collectors.toSet());
        Set<CompoundNamePart> testSet = Set.of(
                new CompoundNamePart(
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(
                                "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadGivenName>"),
                        NodeFactory.createURI("https://schema.org/value"),
                        NodeFactory.createLiteralString("Gold Coast")),
                new CompoundNamePart(
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(
                                "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadType>"),
                        NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                        NodeFactory.createLiteralLang("Highway", "en")),
                new CompoundNamePart(
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(
                                "<https://linked.data.gov.au/def/addr-part-types/road>,<https://linked.data.gov.au/def/road-name-part-types/RoadSuffix>"),
                        NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                        NodeFactory.createLiteralLang("East", "en")),
                new CompoundNamePart(
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(
                                "<https://linked.data.gov.au/def/addr-part-types/subaddressType>"),
                        NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                        NodeFactory.createLiteralLang("Unit", "en")),
                new CompoundNamePart(
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(""),
                        NodeFactory.createLiteralString(
                                "<https://linked.data.gov.au/def/addr-part-types/buildingLevelNumber>"),
                        NodeFactory.createURI("https://schema.org/value"),
                        NodeFactory.createLiteralString("2")));
        assertTrue(
                modifiedParts.containsAll(testSet),
                "modifiedParts: " + modifiedParts + "\ntestSet: " + testSet);
    }

    @Test
    @DisplayName("fallback uses focus node when no value predicate is found")
    void fallbackUsesFocusNodeWhenNoValuePredicateIsFound() {
        Node subject = NodeFactory.createURI("https://example.org/name");
        Node part = NodeFactory.createBlankNode();
        Node fallbackValue = NodeFactory.createURI("https://example.org/no-label-or-value");
        Node partType = NodeFactory.createURI("https://example.org/type");

        Model model = ModelFactory.createDefaultModel();
        model.getGraph()
                .add(Triple.create(
                        subject,
                        RDF.type.asNode(),
                        NodeFactory.createURI("https://linked.data.gov.au/def/cn/CompoundName")));
        model.getGraph().add(Triple.create(subject, SchemaDO.hasPart.asNode(), part));
        model.getGraph().add(Triple.create(part, SchemaDO.additionalType.asNode(), partType));
        model.getGraph().add(Triple.create(part, SchemaDO.value.asNode(), fallbackValue));

        List<HasPart> topLevelParts = List.of(new HasPart(subject, part));
        Set<CompoundNamePart> parts =
                CompoundNaming.getCompoundNameParts(DatasetGraphFactory.wrap(model.getGraph()), topLevelParts);

        assertEquals(1, parts.size());
        CompoundNamePart only = parts.iterator().next();
        assertEquals(NodeFactory.createLiteralString(""), only.valuePredicate());
        assertEquals(fallbackValue, only.value());
    }
}
