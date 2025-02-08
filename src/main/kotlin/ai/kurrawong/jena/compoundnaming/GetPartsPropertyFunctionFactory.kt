package ai.kurrawong.jena.compoundnaming

import org.apache.jena.sparql.pfunction.PropertyFunction
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory

/**
 * Factory to register getParts SPARQL property function in Jena.
 */
class GetPartsPropertyFunctionFactory : PropertyFunctionFactory {
    override fun create(uri: String): PropertyFunction {
        return getParts()
    }
}