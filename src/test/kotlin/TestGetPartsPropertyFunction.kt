import ai.kurrawong.jena.compoundnaming.GetPartsPropertyFunctionFactory
import ai.kurrawong.jena.compoundnaming.loadModelFromResource
import org.apache.jena.query.DatasetFactory
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.QuerySolution
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestGetPartsPropertyFunction {
    private val getPartsIri = "https://linked.data.gov.au/def/cn/func/getParts"
    private val subjectIri = "https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213"

    private fun runSelect(queryString: String): List<QuerySolution> {
        PropertyFunctionRegistry.get().put(getPartsIri, GetPartsPropertyFunctionFactory())

        val dataset = DatasetFactory.createTxnMem()
        dataset.addNamedModel("urn:graph:address", loadModelFromResource("test.ttl"))
        val query = QueryFactory.create(queryString.trimIndent())

        QueryExecutionFactory.create(query, dataset).use { qexec ->
            val rows = mutableListOf<QuerySolution>()
            val resultSet = qexec.execSelect()
            while (resultSet.hasNext()) {
                rows.add(resultSet.nextSolution())
            }
            return rows
        }
    }

    @Test
    fun `property function returns expected rows for known subject`() {
        val rows =
            runSelect(
                """
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<$subjectIri> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes ?partValuePredicate ?partValue) .
                    }
                }
                """,
            )

        assertEquals(12, rows.size)
    }

    @Test
    fun `repeated variable in object list rejects incompatible rows`() {
        val rows =
            runSelect(
                """
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<$subjectIri> AS ?iri)
                        ?iri cnf:getParts (?same ?same ?partValuePredicate ?partValue) .
                    }
                }
                """,
            )

        assertEquals(0, rows.size)
    }

    @Test
    fun `concrete object args only match compatible values`() {
        val matchingRows =
            runSelect(
                """
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<$subjectIri> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes <https://schema.org/value> "2") .
                    }
                }
                """,
            )
        assertEquals(1, matchingRows.size)
        assertEquals(
            "<https://linked.data.gov.au/def/addr-part-types/buildingLevelNumber>",
            matchingRows.single().getLiteral("partTypes").string,
        )

        val nonMatchingRows =
            runSelect(
                """
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<$subjectIri> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes <https://schema.org/value> "definitely-not-a-part-value") .
                    }
                }
                """,
            )
        assertEquals(0, nonMatchingRows.size)
    }

    @Test
    fun `subject with no parts returns no rows`() {
        val rows =
            runSelect(
                """
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<https://example.org/missing-subject> AS ?iri)
                        ?iri cnf:getParts (?partIds ?partTypes ?partValuePredicate ?partValue) .
                    }
                }
                """,
            )

        assertEquals(0, rows.size)
    }

    @Test
    fun `invalid object argument count raises an error`() {
        assertFailsWith<Exception> {
            runSelect(
                """
                PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
                SELECT *
                WHERE {
                    GRAPH ?graph {
                        BIND(<$subjectIri> AS ?iri)
                        ?iri cnf:getParts (?a ?b ?c) .
                    }
                }
                """,
            )
        }
    }
}
