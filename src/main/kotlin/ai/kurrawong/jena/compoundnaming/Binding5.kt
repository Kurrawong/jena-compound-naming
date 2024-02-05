package ai.kurrawong.jena.compoundnaming

import org.apache.jena.graph.Node
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.engine.binding.Binding
import org.apache.jena.sparql.engine.binding.BindingBase
import java.util.*
import java.util.function.BiConsumer

/**
 * A binding implementation that supports 5 binding pairs.
 */
class Binding5 (
    parent: Binding?,
    var1: Var,
    value1: Node,
    var2: Var,
    value2: Node,
    var3: Var,
    value3: Node,
    var4: Var,
    value4: Node,
    var5: Var,
    value5: Node
) : BindingBase(parent) {
    private val var1: Var = Objects.requireNonNull(var1)
    private val value1: Node = Objects.requireNonNull(value1)

    private val var2: Var = Objects.requireNonNull(var2)
    private val value2: Node = Objects.requireNonNull(value2)

    private val var3: Var = Objects.requireNonNull(var3)
    private val value3: Node = Objects.requireNonNull(value3)

    private val var4: Var = Objects.requireNonNull(var4)
    private val value4: Node = Objects.requireNonNull(value4)

    private val var5: Var = Objects.requireNonNull(var5)
    private val value5: Node = Objects.requireNonNull(value5)

    override fun vars1(): Iterator<Var> {
        return Itr.iter5(var1, var2, var3, var4, var5)
    }

    override fun forEach1(action: BiConsumer<Var, Node>) {
        action.accept(var1, value1)
        action.accept(var2, value2)
        action.accept(var3, value3)
        action.accept(var4, value4)
        action.accept(var5, value5)
    }


    protected fun getVar1(idx: Int): Var? {
        if (idx == 0) return var1
        if (idx == 1) return var2
        if (idx == 2) return var3
        if (idx == 3) return var4
        if (idx == 4) return var5
        return null
    }

    override fun size1(): Int {
        if (var1 == null) return 0
        if (var2 == null) return 1
        if (var3 == null) return 2
        if (var4 == null) return 3
        if (var5 == null) return 4
        return 5
    }

    override fun isEmpty1(): Boolean {
        return var1 == null
    }

    override fun contains1(`var`: Var): Boolean {
        if (`var` == null) return false

        if (var1 == null) return false
        if (`var` == var1) return true

        if (var2 == null) return false
        if (`var` == var2) return true

        if (var3 == null) return false
        if (`var` == var3) return true

        if (var4 == null) return false
        if (`var` == var4) return true

        if (var5 == null) return false
        if (`var` == var5) return true

        return false
    }

    override fun get1(`var`: Var): Node? {
        if (`var` == null) return null

        if (var1 == null) return null
        if (`var` == var1) return value1

        if (var2 == null) return null
        if (`var` == var2) return value2

        if (var3 == null) return null
        if (`var` == var3) return value3

        if (var4 == null) return null
        if (`var` == var4) return value4

        if (var5 == null) return null
        if (`var` == var5) return value5

        return null
    }
}