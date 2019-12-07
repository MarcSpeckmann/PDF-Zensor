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
	
	/** Tests if trying to parse a non existing file throws the expected exception. */
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
	 * Tests whether or not parsing the given configuration file results in the expected config.
	 *
	 * @param configFile The config file which will be used in this test.
	 * @param output     The expected output file.
	 * @param verbosity  The expected logger verbosity.
	 * @param mode       The expected censor mode.
	 * @param defColors  The default colors to assign to color-less expressions.
	 */
	@ParameterizedTest(name = "Run {index}: config: {0} => output: {1}, verbosity: {2}, mode: {3}, defColors: {4}")
	@ArgumentsSource(ConfigProvider.class)
	void testValidConfigurations(@Nullable File configFile, @Nullable File output, @Nullable Level verbosity,
								 @Nullable Mode mode, @Nullable Color[] defColors) {
		var config = Config.fromFile(configFile);
		
		assertEquals(output, config.getOutput());
		
		assertEquals(verbosity, config.getVerbosity());
		
		assertEquals(mode, config.getMode());
		
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
}