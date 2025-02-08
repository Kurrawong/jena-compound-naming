package ai.kurrawong.jena.compoundnaming

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

fun loadModelFromResource(resourceName: String): Model {
    val stream = ClassLoader.getSystemResourceAsStream(resourceName)
    val model = ModelFactory.createDefaultModel()
    model.read(stream, null, "TURTLE")
    return model
}