package ai.kurrawong.jena.compoundnaming

import java.io.Serializable

/**
 * Represents a 4-tuple.
 *
 * Same as a Pair or a Triple in Kotlin stdlib but for 4 values.
 */
data class Quadruple<A,B,C,D>(var first: A, var second: B, var third: C, var fourth: D): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}