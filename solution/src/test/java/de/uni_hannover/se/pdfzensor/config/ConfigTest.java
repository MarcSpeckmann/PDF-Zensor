package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ConfigProvider;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
	
	@Test
	void testErroneousConfigPath() {
		assertThrows(IllegalArgumentException.class, () -> Config.fromFile(new File("/no/path")));
	}
	
	/**
	 * Tests whether an error while parsing throws an exception.
	 *
	 * @param path The path to an invalid JSON-file.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"invalid/invalid_json.json", "invalid/empty_config.json", "invalid/defaultColorsAllInvalid.json", "invalid/defaultColorsSomeInvalid.json"})
	void testInvalidJson(String path) {
		assertThrows(IllegalArgumentException.class, () -> Config.fromFile(getResource(CONFIG_PATH + path)));
	}
	
	/**
	 * Tests whether or not parsing the given configuration file results in the expected settings.
	 *
	 * @param configFile The config file which will be used in this test.
	 * @param output     The expected output file.
	 * @param verbosity  The expected logger verbosity.
	 * @param defColors  The default colors to assign to color-less expressions.
	 * @throws IOException If the configuration file couldn't be found.
	 */
	@ParameterizedTest(name = "Run {index}: config: {0} => output: {1}, verbosity: {2}, defColors: {3}")
	@ArgumentsSource(ConfigProvider.class)
	void testValidConfigurations(@Nullable File configFile, @Nullable File output, @Nullable Level verbosity,
								 @Nullable Color[] defColors) throws IOException {
		var config = Config.fromFile(configFile);
		if (output != null)
			assertEquals(output, config.getOutput());
		else
			assertNull(config.getOutput());
		assertEquals(verbosity, config.getVerbosity());
		
		var actualDefColors = config.getDefaultColors();
		if (defColors != null) {
			assertNotNull(actualDefColors);
			assertEquals(defColors.length, actualDefColors.length);
			for (var i = 0; i < defColors.length; i++)
				assertEquals(defColors[i], actualDefColors[i]);
		} else {
			assertNull(actualDefColors);
		}
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