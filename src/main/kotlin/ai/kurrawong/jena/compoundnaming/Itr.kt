package ai.kurrawong.jena.compoundnaming


import java.util.*

/** Iterator of 0 objects  */
class Itr0<X> : Iterator<X> {
    override fun hasNext(): Boolean {
        return false
    }

    override fun next(): X {
        throw NoSuchElementException()
    }

    companion object {
        // Same as Iter.nullIterator but named for tracking and development usage.
        var NULL: Itr0<*> = Itr0<Any>()
        fun <T> itr0(): Itr0<*> {
            return Itr0.Companion.NULL
        }
    }
}

/** Iterator of 1 objects  */
class Itr1<X>(x1: X) : Iterator<X> {
    private var idx = 0
    private val elt1: X = Objects.requireNonNull(x1)

    override fun hasNext(): Boolean {
        return idx < 1
    }

    override fun next(): X {
        idx++
        if (idx == 1) return elt1
        throw NoSuchElementException()
    }
}

/** Iterator of 2 objects  */
class Itr2<X>(x1: X, x2: X) : Iterator<X> {
    private var idx = 0
    private val elt1: X = Objects.requireNonNull(x1)
    private val elt2: X = Objects.requireNonNull(x2)

    override fun hasNext(): Boolean {
        return idx < 2
    }

    override fun next(): X {
        idx++
        if (idx == 1) return elt1
        if (idx == 2) return elt2
        throw NoSuchElementException()
    }
}

/** Iterator of 3 objects  */
class Itr3<X>(x1: X, x2: X, x3: X) : Iterator<X> {
    private var idx = 0
    private val elt1: X = Objects.requireNonNull(x1)
    private val elt2: X = Objects.requireNonNull(x2)
    private val elt3: X = Objects.requireNonNull(x3)

    override fun hasNext(): Boolean {
        return idx < 3
    }

    override fun next(): X {
        idx++
        if (idx == 1) return elt1
        if (idx == 2) return elt2
        if (idx == 3) return elt3
        throw NoSuchElementException()
    }
}

/** Iterator of 4 objects  */
class Itr4<X>(x1: X, x2: X, x3: X, x4: X) : Iterator<X> {
    private var idx = 0
    private val elt1: X = Objects.requireNonNull(x1)
    private val elt2: X = Objects.requireNonNull(x2)
    private val elt3: X = Objects.requireNonNull(x3)
    private val elt4: X = Objects.requireNonNull(x4)

    override fun hasNext(): Boolean {
        return idx < 4
    }

    override fun next(): X {
        idx++
        if (idx == 1) return elt1
        if (idx == 2) return elt2
        if (idx == 3) return elt3
        if (idx == 4) return elt4
        throw NoSuchElementException()
    }
}

/** Iterator of 5 objects  */
class Itr5<X>(x1: X, x2: X, x3: X, x4: X, x5: X) : Iterator<X> {
    private var idx = 0
    private val elt1: X = Objects.requireNonNull(x1)
    private val elt2: X = Objects.requireNonNull(x2)
    private val elt3: X = Objects.requireNonNull(x3)
    private val elt4: X = Objects.requireNonNull(x4)
    private val elt5: X = Objects.requireNonNull(x5)

    override fun hasNext(): Boolean {
        return idx < 5
    }

    override fun next(): X {
        idx++
        if (idx == 1) return elt1
        if (idx == 2) return elt2
        if (idx == 3) return elt3
        if (idx == 4) return elt4
        if (idx == 5) return elt5
        throw NoSuchElementException()
    }
}

/**
 * Emulates the Itr class with static iterN methods from Jena.
 */
object Itr {
    fun <X> iter0(): Iterator<X> {
        return Itr0()
    }

    fun <X> iter1(x1: X): Iterator<X> {
        return Itr1(x1)
    }

    fun <X> iter2(x1: X, x2: X): Iterator<X> {
        return Itr2(x1, x2)
    }

    fun <X> iter3(x1: X, x2: X, x3: X): Iterator<X> {
        return Itr3(x1, x2, x3)
    }

    fun <X> iter4(x1: X, x2: X, x3: X, x4: X): Iterator<X> {
        return Itr4(x1, x2, x3, x4)
    }

    fun <X> iter5(x1: X, x2: X, x3: X, x4: X, x5: X): Iterator<X> {
        return Itr5(x1, x2, x3, x4, x5)
    }
}