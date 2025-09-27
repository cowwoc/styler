package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.*;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test parser extensibility framework for future Java language versions.
 */
public class ExtensibilityFrameworkTest {

    @Test
    public void testJavaVersionSupport() {
        // Test that parser can be configured for different Java versions
        String simpleCode = "public class Test { }";

        IndexOverlayParser parser8 = new IndexOverlayParser(simpleCode, JavaVersion.JAVA_8);
        IndexOverlayParser parser17 = new IndexOverlayParser(simpleCode, JavaVersion.JAVA_17);
        IndexOverlayParser parser25 = new IndexOverlayParser(simpleCode, JavaVersion.JAVA_25);

        // Should all parse successfully
        int root8 = parser8.parse();
        int root17 = parser17.parse();
        int root25 = parser25.parse();

        assertEquals(true, root8 >= 0);
        assertEquals(true, root17 >= 0);
        assertEquals(true, root25 >= 0);
    }

    @Test
    public void testJavaVersionFromNumber() {
        assertEquals(JavaVersion.JAVA_17, JavaVersion.fromNumber(17));
        assertEquals(JavaVersion.JAVA_21, JavaVersion.fromNumber(21));
        assertEquals(JavaVersion.JAVA_25, JavaVersion.fromNumber(25));
    }

    @Test
    public void testJavaVersionComparison() {
        assertEquals(true, JavaVersion.JAVA_17.isAtLeast(JavaVersion.JAVA_11));
        assertEquals(false, JavaVersion.JAVA_11.isAtLeast(JavaVersion.JAVA_17));
        assertEquals(true, JavaVersion.JAVA_21.isAtLeast(JavaVersion.JAVA_21));
    }

    @Test
    public void testJavaVersionDisplayName() {
        assertEquals("Java 17", JavaVersion.JAVA_17.getDisplayName());
        assertEquals("Java 25", JavaVersion.JAVA_25.getDisplayName());
    }

    @Test
    public void testParseStrategyRegistryInitialization() {
        // Test that strategy registry is properly initialized
        String simpleCode = "public class Test { }";
        IndexOverlayParser parser = new IndexOverlayParser(simpleCode, JavaVersion.JAVA_25);

        // Should not throw exceptions during initialization
        assertNotNull(parser);

        // Should parse successfully
        int rootNode = parser.parse();
        assertEquals(true, rootNode >= 0);
    }

    @Test
    public void testBackwardsCompatibility() {
        // Test that newer parser versions can handle older Java code
        String java8Code = """
            public class OldStyleClass {
                private String name;

                public String getName() {
                    return this.name;
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(java8Code, JavaVersion.JAVA_25);
        int rootNode = parser.parse();
        assertEquals(true, rootNode >= 0);
    }

    @Test
    public void testDefaultJavaVersion() {
        // Test that default constructor uses latest Java version
        String simpleCode = "public class Test { }";
        IndexOverlayParser parser = new IndexOverlayParser(simpleCode);

        // Should work with default version
        int rootNode = parser.parse();
        assertEquals(true, rootNode >= 0);
    }
}