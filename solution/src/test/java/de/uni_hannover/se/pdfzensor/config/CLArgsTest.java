package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;
import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;

/**
 * CLArgsTest should contain all unit-tests related to {@link CLArgs}.
 */
class CLArgsTest {
	
	/**
	 * Multiple tests related to using fromStringArray
	 */
	@Test
	void fromStringArray() {
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(null));
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(new String[0]));
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray());
		assertThrows(CommandLine.UnmatchedArgumentException.class, () -> CLArgs.fromStringArray(""));
	}
	
	/**
	 * Multiple tests related to using getVerbosity
	 */
	@Test
	void getVerbosity() {
		//return null because of priority problem with config
		var cla = CLArgs.fromStringArray();
		assertNull(cla.getVerbosity());
		
		for (int i = 1; i < VERBOSITY_LEVELS.length; i++) {
			cla = CLArgs.fromStringArray("-"+"v".repeat(i));
			assertEquals(VERBOSITY_LEVELS[i], cla.getVerbosity());
		}
		
		cla = CLArgs.fromStringArray("-"+"v".repeat(VERBOSITY_LEVELS.length-1));
		assertEquals(Level.ALL, cla.getVerbosity());
		
		cla = CLArgs.fromStringArray("-"+"v".repeat(VERBOSITY_LEVELS.length));
		assertEquals(Level.ALL, cla.getVerbosity());
	}
}