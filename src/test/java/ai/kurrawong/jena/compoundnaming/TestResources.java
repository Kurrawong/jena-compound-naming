package ai.kurrawong.jena.compoundnaming;

import java.io.IOException;
import java.io.InputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

final class TestResources {
    private TestResources() {}

    static Model loadModelFromResource(String resourceName) {
        try (InputStream stream = ClassLoader.getSystemResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource: " + resourceName);
            }
            Model model = ModelFactory.createDefaultModel();
            model.read(stream, null, "TURTLE");
            return model;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read resource: " + resourceName, exception);
        }
    }
}
