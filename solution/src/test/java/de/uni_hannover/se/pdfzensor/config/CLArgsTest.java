package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CLArgsTest {
	
	@Test
	void fromStringArray() {
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(null));
		assertNotNull(CLArgs.fromStringArray(new String[0]));
		//Todo: add more tests
	}
	
	@Test
	void getVerbosity() {
		//TODO: add tests
		fail("No tests implemented yet");
	}
}