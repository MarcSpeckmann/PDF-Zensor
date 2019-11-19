package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.TestUtility;
import org.junit.jupiter.api.Test;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UtilsTest should contain all unit-tests related to {@link Utils}.
 */
class UtilsTest {
	
	/**
	 * Multiple tests related of using fitToArray
	 */
	@Test
	void fitToArray() {
		TestUtility.assertIsUtilityClass(Utils.class);
		
		assertEquals(0, Utils.fitToArray(VERBOSITY_LEVELS, -1));
		for (int i = 0; i < VERBOSITY_LEVELS.length; i++) {
			assertEquals(i, Utils.fitToArray(VERBOSITY_LEVELS, i));
		}
		
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length + 1));
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length));
		
		
	}
	
	/**
	 * Multiple tests related to method call with an empty array
	 */
	@Test
	void fitToArrayEmptyArray() {
		//test empty array
		String[] emptyarray = new String[0];
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyarray, 1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyarray, -1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyarray, 0));
	}
	
	/**
	 * Multiple tests related to method call with null instead an array
	 */
	@Test
	void fitToArrayNullArray() {
		//test null array
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(null, 1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(null, -1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(null, 0));
	}
}