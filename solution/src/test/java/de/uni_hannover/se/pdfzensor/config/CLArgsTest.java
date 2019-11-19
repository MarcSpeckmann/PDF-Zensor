package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;

class CLArgsTest {
	
	@Test
	void fromStringArray() {
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(null));
		assertNotNull(CLArgs.fromStringArray());
		assertEquals(CLArgs.class, CLArgs.fromStringArray().getClass());
		//Todo: add more tests
	}
	
	@Test
	void getVerbosity() {
		//TODO: add tests
		var cla = CLArgs.fromStringArray();
		//assertEquals(Level.OFF, cla.getVerbosity());
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