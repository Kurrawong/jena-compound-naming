package ai.kurrawong.jena.compoundnaming

import org.apache.jena.sys.JenaSubsystemLifecycle

/**
 * Registers compound naming extensions during Jena subsystem initialization.
 */
class InitCompoundNaming : JenaSubsystemLifecycle {
    override fun start() {
        getParts.init()
    }

    override fun stop() {}

    override fun level(): Int = 550
}
