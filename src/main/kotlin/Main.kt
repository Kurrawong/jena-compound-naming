/**
 * Example usage.
 */
import ai.kurrawong.jena.compoundnaming.GetPartsPropertyFunctionFactory
import ai.kurrawong.jena.compoundnaming.loadModelFromResource

import org.apache.jena.query.*
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry

fun main() {
    PropertyFunctionRegistry.get().put(
        "https://linked.data.gov.au/def/cn/func/getParts",
        GetPartsPropertyFunctionFactory()
    )

    val dataset = DatasetFactory.createTxnMem()
    val model = loadModelFromResource("data.ttl")
    val model2 = loadModelFromResource("data2.ttl")
    dataset.addNamedModel("urn:graph:address", model)
    dataset.addNamedModel("urn:graph:address2", model2)

    val queryString = """
PREFIX cnf: <https://linked.data.gov.au/def/cn/func/>
PREFIX sdo: <https://schema.org/>

SELECT *
WHERE {
    GRAPH ?graph {
        BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-2522774> AS ?iri)
        # BIND(<https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> AS ?iri)
        ?iri cnf:getParts (?partId ?partType ?partValuePredicate ?partValue) .
    }
}
limit 10
            """.trimIndent()

    val query = QueryFactory.create(queryString)

    QueryExecutionFactory.create(query, dataset).use { qexec ->
        val results = qexec.execSelect()
        ResultSetFormatter.out(System.out, results, query)
    }
}