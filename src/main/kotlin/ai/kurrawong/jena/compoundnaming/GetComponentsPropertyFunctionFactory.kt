package ai.kurrawong.jena.compoundnaming

import org.apache.jena.sparql.pfunction.PropertyFunction
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory

class GetComponentsPropertyFunctionFactory : PropertyFunctionFactory {
    override fun create(uri: String): PropertyFunction {
        return getComponents()
    }
}