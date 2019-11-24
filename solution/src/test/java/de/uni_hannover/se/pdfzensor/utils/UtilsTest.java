package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.TestUtility;
import org.junit.jupiter.api.Test;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static org.junit.jupiter.api.Assertions.*;

/** UtilsTest should contain all unit-tests related to {@link Utils}. */
class UtilsTest {
	
	/** Multiple tests related of using fitToArray. */
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
	
	/** Multiple tests related to method call fitToArray with an empty array. */
	@Test
	void fitToArrayEmptyArray() {
		//test empty array
		String[] emptyarray = new String[0];
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyarray, 1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyarray, -1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyarray, 0));
	}
	
	/** Multiple tests related to method call fitToArray with null instead an array. */
	@Test
	void fitToArrayNullArray() {
		//test null array
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, 1));
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, -1));
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, 0));
	}
	
	/** Multiple tests related to method call clamp with null. */
	@Test
	void clampNull() {
		//test null
		assertThrows(NullPointerException.class, () -> Utils.clamp(null, null, null));
		assertThrows(NullPointerException.class, () -> Utils.clamp(null, 0, 0));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, 0, null));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, null, 0));
		assertThrows(NullPointerException.class, () -> Utils.clamp(null, 0, 0));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, 0, null));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, null, 0));
	}
	
	/** Multiple tests related to using fitToArray. */
	@Test
	void clamp() {
		assertThrows(IllegalArgumentException.class, () -> Utils.clamp(0, 1, 0));
		
		assertEquals(1, Utils.clamp(0, 1, 6));
		assertEquals(4, Utils.clamp(6, 2, 4));
		assertEquals(2, Utils.clamp(2, 1, 3));
	}
}