package ai.kurrawong.jena.compoundnaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetPartsPropertyFunctionTest {
    private static final String SUBJECT_IRI =
            "https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213";

    private static List<QuerySolution> runSelect(String queryString) {
        getParts.init();

        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.addNamedModel("urn:graph:address", TestResources.loadModelFromResource("test.ttl"));
        Query query = QueryFactory.create(queryString.stripIndent());

        try (QueryExecution qexec = QueryExecution.create(query, dataset)) {
            List<QuerySolution> rows = new ArrayList<>();
            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {
                rows.add(resultSet.nextSolution());
            }
            return rows;
        }
    }

    @Test
    @DisplayName("property function returns expected rows for known subject")
    void propertyFunctionReturnsExpectedRowsForKnownSubject() {
        List<QuerySolution> rows = runSelect("""
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<%s> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes ?partValuePredicate ?partValue) .
                    }
                }
                """.formatted(SUBJECT_IRI));

        assertEquals(12, rows.size());
    }

    @Test
    @DisplayName("repeated variable in object list rejects incompatible rows")
    void repeatedVariableInObjectListRejectsIncompatibleRows() {
        List<QuerySolution> rows = runSelect("""
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<%s> AS ?iri)
                        ?iri cnf:getParts (?same ?same ?partValuePredicate ?partValue) .
                    }
                }
                """.formatted(SUBJECT_IRI));

        assertEquals(0, rows.size());
    }

    @Test
    @DisplayName("concrete object args only match compatible values")
    void concreteObjectArgsOnlyMatchCompatibleValues() {
        List<QuerySolution> matchingRows = runSelect("""
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<%s> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes <https://schema.org/value> "2") .
                    }
                }
                """.formatted(SUBJECT_IRI));
        assertEquals(1, matchingRows.size());
        assertEquals(
                "<https://linked.data.gov.au/def/addr-part-types/buildingLevelNumber>",
                matchingRows.getFirst().getLiteral("partTypes").getString());

        List<QuerySolution> nonMatchingRows = runSelect("""
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<%s> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes <https://schema.org/value> "definitely-not-a-part-value") .
                    }
                }
                """.formatted(SUBJECT_IRI));
        assertEquals(0, nonMatchingRows.size());
    }

    @Test
    @DisplayName("subject with no parts returns no rows")
    void subjectWithNoPartsReturnsNoRows() {
        List<QuerySolution> rows = runSelect("""
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<https://example.org/missing-subject> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes ?partValuePredicate ?partValue) .
                    }
                }
                """);

        assertEquals(0, rows.size());
    }

    @Test
    @DisplayName("invalid object argument count raises an error")
    void invalidObjectArgumentCountRaisesAnError() {
        assertThrows(Exception.class, () -> runSelect("""
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<%s> AS ?iri)
                        ?iri cnf:getParts (?a ?b ?c) .
                    }
                }
                """.formatted(SUBJECT_IRI)));
    }
}
