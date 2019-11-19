package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.TestUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static org.junit.jupiter.api.Assertions.*;

class PDFUtilsTest {
	
	@BeforeEach
	void setUp() {
	}
	
	@AfterEach
	void tearDown() {
	}
	
	@Test
	void fitToArray() {
		TestUtility.assertIsUtilityClass(PDFUtils.class);
		assertEquals(0, PDFUtils.fitToArray(VERBOSITY_LEVELS, 0));
		assertEquals(0, PDFUtils.fitToArray(VERBOSITY_LEVELS, -1));
		assertEquals(VERBOSITY_LEVELS.length, PDFUtils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length + 1));
		assertEquals(VERBOSITY_LEVELS.length, PDFUtils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length));
		assertEquals(3, PDFUtils.fitToArray(VERBOSITY_LEVELS, 3));
		//TODO: Add tests for empty array
		//TODO: assert max >= min
		assertThrows(IllegalArgumentException.class, () -> PDFUtils.fitToArray(null, 1));
		assertThrows(IllegalArgumentException.class, () -> PDFUtils.fitToArray(null, -1));
		assertThrows(IllegalArgumentException.class, () -> PDFUtils.fitToArray(null, 0));
	}
}