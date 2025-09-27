package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test to isolate text reconstruction fidelity issues.
 */
public class TextReconstructionTest {

    @Test(description = "Debug text reconstruction differences")
    public void debugTextReconstruction() {
        String source = """
            /**
             * Main class documentation.
             */
            public class CommentExample {
                // Single line comment
                private int field1;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        String reconstructed = parser.getNodeText(rootId);

        assertEquals(reconstructed, source, "Text reconstruction should be identical");
    }
}