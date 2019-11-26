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
	
	/**
	 * Tests if trying to parse invalid configuration files (not a file, not a ".json" file, invalid syntax) throws the
	 * correct exception.
	 */
	@Test
	void testIllegalArguments() {
		// file not found / not a file: throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(new File("/no/path")));
		// file not a ".json": throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class,
					 () -> ConfigParser.fromFile(getResource(CONFIG_PATH + "invalid/not_a_json.txt")));
		
		// file has JSON-String with invalid syntax
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(getResource(
				CONFIG_PATH + "invalid/invalid_json.json")));
		// file has no JSON-String
		assertThrows(IllegalArgumentException.class, () -> ConfigParser.fromFile(getResource(
				CONFIG_PATH + "invalid/empty_config.json")));
	}
	
	/**
	 * Tests if the values given in the configuration file are parsed correctly.
	 *
	 * @throws IOException If the configuration file couldn't be found.
	 */
	@Test
	void testValidConfigurations() throws IOException {
		// Configuration file with verbosity specified as a string ("DEBUG") and output specified as "censoredFile.pdf"
		var file = getResource(CONFIG_PATH + "testVerbosityAsStringValidConfig.json");
		assertSame(Level.DEBUG, ConfigParser.fromFile(file).getVerbosity());
		assertEquals("censoredFile.pdf", Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).getName());
		
		// Configuration file with verbosity specified as an integer (5) and output specified as "censoredFile.pdf"
		file = getResource(CONFIG_PATH + "testVerbosityAsIntegerValidConfig.json");
		assertSame(Level.DEBUG, ConfigParser.fromFile(file).getVerbosity());
		assertEquals("censoredFile.pdf", Objects.requireNonNull(ConfigParser.fromFile(file).getOutput()).getName());
		
		// null as file to retrieve the default config (everything set to null)
		assertSame(null, ConfigParser.fromFile(null).getVerbosity());
		assertSame(null, ConfigParser.fromFile(null).getOutput());
	}
	
	/**
	 * Tests if theoretically invalid values for verbosity are either clamped correctly or discarded.
	 *
	 * @throws IOException If the configuration file couldn't be found.
	 */
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