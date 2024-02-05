/**
 * Example usage.
 */
import ai.kurrawong.jena.compoundnaming.GetComponentsPropertyFunctionFactory

import org.apache.jena.query.*
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry

fun main() {
    val propertyFunctionRegistry = PropertyFunctionRegistry.chooseRegistry(ARQ.getContext())
    propertyFunctionRegistry.put(
        "https://linked.data.gov.au/def/cn/func/getComponents",
        GetComponentsPropertyFunctionFactory()
    )
    PropertyFunctionRegistry.set(ARQ.getContext(), propertyFunctionRegistry)

    val reader = object {}.javaClass.getResourceAsStream("data.ttl")?.bufferedReader()
    val reader2 = object {}.javaClass.getResourceAsStream("data2.ttl")?.bufferedReader()

    val dataset = DatasetFactory.createTxnMem()
    val model = ModelFactory.createDefaultModel()
    model.read(reader, "https://example.com/", "TURTLE")
    dataset.addNamedModel("urn:graph:address", model)

    val model2 = ModelFactory.createDefaultModel().read(
        "https://cdn.jsdelivr.net/gh/kurrawong/exem-ont@bf22aa548c635f43f221df7362d8ddc2d99edcc7/exem.ttl",
        "TURTLE"
    )
    dataset.addNamedModel("urn:graph:other", model2)

    val model3 = dataset.defaultModel
    model3.read(reader2, "https://example.com/", "TURTLE")

    val queryString = """
PREFIX func: <https://linked.data.gov.au/def/cn/func/>

SELECT *
WHERE {
    GRAPH ?g {
        BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-obj-1837741> AS ?iri)
        ?iri <java:ai.kurrawong.jena.compoundnaming.getComponents> (?componentId ?componentType ?componentValuePredicate ?componentValue) .
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