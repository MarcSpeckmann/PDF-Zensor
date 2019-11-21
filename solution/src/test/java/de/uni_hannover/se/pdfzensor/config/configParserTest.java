package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class configParserTest {

	@Test
	void testFromFile() throws IOException {
		// If file not found throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(new File("/no/path")));

		// File correct
		// Configuration file with verbosity Level.DEBUG
		String fileName = "censoredFile.pdf";
		var file = new File("src/test/resources/testConfig.json");
		// Verbosity level as string
		assertSame(Level.DEBUG, ConfigParser.fromFile(file).getVerbosity() );
		// Check output not null
		assertNotNull(Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).toString());
		// Check output file name == "censoredFile.pdf"
		assertEquals(fileName, Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).toString());

		// Verbosity level as integer
		file = new File("src/test/resources/testConfig2.json");
		// Level Debug as integer in json file
		assertSame(Level.DEBUG, ConfigParser.fromFile(file).getVerbosity());

		// Invalid config-files
		// Verbosity level as integer higher than the highest possible value ( > 7)
		file = new File("src/test/resources/testConfigHighVerbosity.json");
		assertSame(Level.ALL, ConfigParser.fromFile(file).getVerbosity());

		// Verbosity level as integer below zero (negative value)
		file = new File("src/test/resources/testConfigNegativeVerbosity.json");
		assertSame(Level.OFF, ConfigParser.fromFile(file).getVerbosity());

		// False verbosity string in configurations json file
		file = new File("src/test/resources/testConfigFalseVerbosityString.json");
		assertNull(ConfigParser.fromFile(file).getVerbosity());

	}
}
