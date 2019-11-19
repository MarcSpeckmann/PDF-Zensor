package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;

/**
 * CLArgsTest should contain all unit-tests related to {@link CLArgs}.
 */
class CLArgsTest {
	
	/**
	 * Multiple tests related of using fromStringArray
	 */
	@Test
	void fromStringArray() {
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(null));
		assertNotNull(CLArgs.fromStringArray(new String[0]));
		assertNotNull(CLArgs.fromStringArray());
		assertEquals(CLArgs.class, CLArgs.fromStringArray().getClass());
		assertEquals(CLArgs.class, CLArgs.fromStringArray(new String[0]).getClass());
		
		//Müssen wir das behandeln ? Kann args einen leeren String enthalten ?
		//assertEquals(CLArgs.class, CLArgs.fromStringArray(new String()).getClass());
		//assertNotNull(CLArgs.fromStringArray(new String()));
	}
	
	/**
	 * Multiple tests related of using getVerbosity
	 */
	@Test
	void getVerbosity() {
		//return null because of priotity problem with config
		var cla = CLArgs.fromStringArray();
		assertEquals(null, cla.getVerbosity());
		
		String input = "-v";
		for (int i = 1; i < VERBOSITY_LEVELS.length; i++) {
			cla = CLArgs.fromStringArray("-v");
			assertEquals(Level.FATAL, cla.getVerbosity());
			input = input.concat("v");
		}
		
		cla = CLArgs.fromStringArray("-vvvvvvv");
		assertEquals(Level.ALL, cla.getVerbosity());
		
		cla = CLArgs.fromStringArray("-vvvvvvvv");
		assertEquals(Level.ALL, cla.getVerbosity());
	}
}