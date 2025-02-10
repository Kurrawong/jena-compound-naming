/**
 * Example usage.
 */
import ai.kurrawong.jena.compoundnaming.GetPartsPropertyFunctionFactory
import ai.kurrawong.jena.compoundnaming.loadModelFromResource
import org.apache.jena.query.DatasetFactory
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.ResultSetFormatter
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry

fun main() {
    PropertyFunctionRegistry.get().put(
        "https://linked.data.gov.au/def/cn/func/getParts",
        GetPartsPropertyFunctionFactory(),
    )

    val dataset = DatasetFactory.createTxnMem()
    val model = loadModelFromResource("data.ttl")
    dataset.addNamedModel("urn:graph:address", model)

    val queryString =
        """
PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
PREFIX sdo: <https://schema.org/>

SELECT *
WHERE {
    GRAPH ?graph {
        BIND(<https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> AS ?iri)
        ?iri cnf:getParts (?partIds ?partTypes ?partValuePredicate ?partValue) .
    }
}
limit 100
        """.trimIndent()

    val query = QueryFactory.create(queryString)

    QueryExecutionFactory.create(query, dataset).use { qexec ->
        val results = qexec.execSelect()
        ResultSetFormatter.out(System.out, results, query)
    }
}
