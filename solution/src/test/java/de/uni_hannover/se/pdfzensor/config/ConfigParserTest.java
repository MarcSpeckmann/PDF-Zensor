package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {
	private static final String CONFIG_PATH = "/configparser-test/configs/";
	
	@Test
	void testIllegalArguments() {
		// If file not found throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(new File("/no/path")));
		// If path points to a directory throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(getResource("/configparser-test/")));
		
		// invalid content of file
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(getResource(
				CONFIG_PATH + "invalid/invalid_json.json")));
		// empty file
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(getResource(
				CONFIG_PATH + "invalid/empty_config.json")));
	}
	
	@Test
	void testValidConfigurations() throws IOException {
		// File correct
		// Configuration file with verbosity Level.DEBUG
		var file = getResource(CONFIG_PATH + "testVerbosityAsStringValidConfig.json");
		// Verbosity level as string
		assertSame(Level.DEBUG, ConfigParser.fromFile(file).getVerbosity());
		// Check output not null
		assertNotNull(Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).toString());
		// Check output file name == "censoredFile.pdf"
		assertEquals("censoredFile.pdf", Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).toString());
		
		// Verbosity level as integer
		file = getResource(CONFIG_PATH + "testVerbosityAsIntegerValidConfig.json");
		// Level Debug as integer in json file
		assertSame(Level.DEBUG, ConfigParser.fromFile(file).getVerbosity());
		
		// null as file to retrieve the default config (everything set null)
		assertSame(null, ConfigParser.fromFile(null).getVerbosity());
		assertSame(null, ConfigParser.fromFile(null).getOutput());
	}
	
	@Test
	void testInvalidValueFallbacks() throws IOException {
		// Verbosity level as integer higher than the highest possible value (> 7)
		var file = getResource(CONFIG_PATH + "testConfigHighVerbosity.json");
		assertSame(Level.ALL, ConfigParser.fromFile(file).getVerbosity());
		
		// Verbosity level as integer below zero (negative value)
		file = getResource(CONFIG_PATH + "testConfigNegativeVerbosity.json");
		assertSame(Level.OFF, ConfigParser.fromFile(file).getVerbosity());
		
		// False verbosity string in configurations json file
		file = getResource(CONFIG_PATH + "testConfigFalseVerbosityString.json");
		assertNull(ConfigParser.fromFile(file).getVerbosity());
		
	}
}
