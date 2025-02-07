/**
 * Example usage.
 */
import ai.kurrawong.jena.compoundnaming.GetComponentsPropertyFunctionFactory

import org.apache.jena.query.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry

fun loadModelFromResource(resourceName: String): Model {
    val stream = ClassLoader.getSystemResourceAsStream(resourceName)
    val model = ModelFactory.createDefaultModel()
    model.read(stream, null, "TURTLE")
    return model
}

fun main() {
    val propertyFunctionRegistry = PropertyFunctionRegistry.chooseRegistry(ARQ.getContext())
    propertyFunctionRegistry.put(
        "https://linked.data.gov.au/def/cn/func/getComponents",
        GetComponentsPropertyFunctionFactory()
    )
    PropertyFunctionRegistry.set(ARQ.getContext(), propertyFunctionRegistry)

    val dataset = DatasetFactory.createTxnMem()
    val model = loadModelFromResource("data.ttl")
    val model2 = loadModelFromResource("data2.ttl")
    dataset.addNamedModel("urn:graph:address", model)
    dataset.addNamedModel("urn:graph:address2", model2)

    val queryString = """
PREFIX func: <https://linked.data.gov.au/def/cn/func/>
PREFIX sdo: <https://schema.org/>

SELECT *
WHERE {
    GRAPH ?g {
        BIND(<https://linked.data.gov.au/dataset/qld-addr/addr-2522774> AS ?iri)
        # BIND(<https://linked.data.gov.au/dataset/qld-addr/address/e37309a2-3916-506e-b334-30ebb444c213> AS ?iri)
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