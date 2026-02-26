package ai.kurrawong.jena.compoundnaming

/**
 * Ensures custom property functions are registered before Fuseki starts.
 */
fun main(args: Array<String>) {
    getParts.init()

    val fusekiMain =
        Class
            .forName("org.apache.jena.fuseki.main.cmds.FusekiServerCmd")
            .getMethod("main", Array<String>::class.java)
    fusekiMain.invoke(null, args)
}
