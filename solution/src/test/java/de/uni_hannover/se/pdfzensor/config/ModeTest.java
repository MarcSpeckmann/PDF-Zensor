package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for mode. The method {@link Mode#stringToMode(String)} does not require testing since it only calls EnumUtils,
 * which is assumed to be thoroughly tested.
 */
class ModeTest {
	/** Tests for {@link Mode} constructor. */
	@Test
	void testMode() {
		assertDoesNotThrow(() -> Mode.ALL);
		assertDoesNotThrow(() -> Mode.MARKED);
		assertDoesNotThrow(() -> Mode.UNMARKED);
	}
}