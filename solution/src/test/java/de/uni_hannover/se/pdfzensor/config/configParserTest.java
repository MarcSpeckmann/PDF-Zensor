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
		String fileNAme = "censoredFile.pdf";
		var file = new File("src/test/resources/testConfig.json");
		// Verbosity level as string
		assertSame(ConfigParser.fromFile(file).getVerbosity(), Level.DEBUG);
		// Check output not null
		assertNotNull(Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).toString());
		// Check output file name == "censoredFile.pdf"
		assertEquals(fileNAme, Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).toString());

		// Verbosity level as integer
		file = new File("src/test/resources/testConfig2.json");
		// Level Debug as integer in json file
		assertSame(ConfigParser.fromFile(file).getVerbosity(), Level.DEBUG);

		// Verbosity level as integer higher than the sum of all levels
		file = new File("src/test/resources/testConfigHighVerbosity.json");
		assertSame(ConfigParser.fromFile(file).getVerbosity(), Level.ALL);

		// False verbosity string in configurations json file
		file = new File("src/test/resources/testConfigFalseVerbosityString.json");
		assertNull(ConfigParser.fromFile(file).getVerbosity());

	}
}
