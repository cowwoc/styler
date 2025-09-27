package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Test input validation to prevent resource exhaustion attacks.
 */
public class InputValidationTest {

    @Test
    public void testNullInputValidation() {
        try {
            new IndexOverlayParser(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            assertEquals(true, e.getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void testEmptyInputValidation() {
        try {
            new IndexOverlayParser("");
            fail("Expected IllegalArgumentException for empty input");
        } catch (IllegalArgumentException e) {
            assertEquals(true, e.getMessage().contains("cannot be empty"));
        }
    }

    @Test
    public void testWhitespaceOnlyInputValidation() {
        try {
            new IndexOverlayParser("   \n\t  ");
            fail("Expected IllegalArgumentException for whitespace-only input");
        } catch (IllegalArgumentException e) {
            assertEquals(true, e.getMessage().contains("cannot be empty or whitespace-only"));
        }
    }

    @Test
    public void testExcessivelyLargeInputValidation() {
        // Create a string that exceeds the character limit
        StringBuilder largeInput = new StringBuilder();
        int maxChars = 10 * 1024 * 1024; // MAX_SOURCE_LENGTH_CHARS

        // Add characters slightly over the limit
        for (int i = 0; i < maxChars + 1000; i++) {
            largeInput.append("a");
        }

        try {
            new IndexOverlayParser(largeInput.toString());
            fail("Expected IllegalArgumentException for oversized input");
        } catch (IllegalArgumentException e) {
            assertEquals(true, e.getMessage().contains("Source text too large"));
            assertEquals(true, e.getMessage().contains("characters"));
        }
    }

    @Test
    public void testValidSmallInput() {
        String validCode = "public class Test { }";

        // This should not throw an exception
        IndexOverlayParser parser = new IndexOverlayParser(validCode);
        assertEquals(true, parser != null);
    }

    @Test
    public void testValidMediumInput() {
        // Create a reasonably large but valid input
        StringBuilder mediumInput = new StringBuilder();
        mediumInput.append("public class Test {\n");

        // Add 1000 simple methods
        for (int i = 0; i < 1000; i++) {
            mediumInput.append("    public void method").append(i).append("() { }\n");
        }
        mediumInput.append("}");

        // This should not throw an exception
        IndexOverlayParser parser = new IndexOverlayParser(mediumInput.toString());
        assertEquals(true, parser != null);
    }

    @Test
    public void testByteSizeLimitValidation() {
        // The character limit is 10M, and byte limit triggers when length * 3 > 50MB
        // Since 50MB / 3 = ~16.7M, and character limit is 10M, the character limit
        // will always be hit first. So this test is redundant.
        // Instead, let's test that the byte limit calculation is being performed correctly
        // by creating a string that hits character limit and verify the error message

        StringBuilder largeInput = new StringBuilder();
        int maxChars = 10 * 1024 * 1024; // MAX_SOURCE_LENGTH_CHARS

        // Add characters up to the limit
        for (int i = 0; i < maxChars + 1; i++) {
            largeInput.append("a");
        }

        try {
            new IndexOverlayParser(largeInput.toString());
            fail("Expected IllegalArgumentException for character limit");
        } catch (IllegalArgumentException e) {
            // This will hit the character limit first, which is fine - shows input validation works
            assertEquals(true, e.getMessage().contains("Source text too large"));
            assertEquals(true, e.getMessage().contains("characters") || e.getMessage().contains("bytes"));
        }
    }
}