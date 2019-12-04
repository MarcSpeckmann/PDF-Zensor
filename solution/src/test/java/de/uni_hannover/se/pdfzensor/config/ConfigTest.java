package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
	
	@Test
	void testErroneousConfigPath() {
		assertThrows(IllegalArgumentException.class, () -> Config.fromFile(new File("/no/path")));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"invalid/invalid_json.json", "invalid/empty_config.json"})
	void testInvalidJson(String path) {
		assertThrows(IllegalArgumentException.class, () -> Config.fromFile(getResource(CONFIG_PATH + path)));
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
		assertSame(Level.DEBUG, Config.fromFile(file).getVerbosity());
		assertEquals("censoredFile.pdf", Objects.requireNonNull(Config.fromFile(file).getOutput()).getName());
		
		// Configuration file with verbosity specified as an integer (5) and output specified as "censoredFile.pdf"
		file = getResource(CONFIG_PATH + "testVerbosityAsIntegerValidConfig.json");
		assertSame(Level.DEBUG, Config.fromFile(file).getVerbosity());
		assertEquals("censoredFile.pdf", Objects.requireNonNull(Config.fromFile(file).getOutput()).getName());
		
		// null as file to retrieve the default config (everything set to null)
		assertSame(null, Config.fromFile(null).getVerbosity());
		assertSame(null, Config.fromFile(null).getOutput());
	}
	
	/**
	 * Tests if theoretically invalid values for verbosity are either clamped correctly or discarded.
	 *
	 * @throws IOException If the configuration file couldn't be found.
	 */
	@Test
	void testInvalidValueFallbacks() throws IOException {
		// Verbosity level as integer higher than the highest possible value (> 7)
		var file = getResource(CONFIG_PATH + "valid/high_verbosity.json");
		assertSame(Level.ALL, Config.fromFile(file).getVerbosity());
		
		// Verbosity level as integer below zero (negative value)
		file = getResource(CONFIG_PATH + "valid/negative_verbosity.json");
		assertSame(Level.OFF, Config.fromFile(file).getVerbosity());
		
		// False verbosity string in configurations json file
		file = getResource(CONFIG_PATH + "valid/unknown_verbosity.json");
		assertNull(Config.fromFile(file).getVerbosity());
	}
}