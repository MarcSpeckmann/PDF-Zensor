package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ConfigProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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
	@ValueSource(strings = {"invalid/invalid_json.json", "invalid/empty_config.json", "invalid/defaultColorsAllInvalid.json", "invalid/defaultColorsSomeInvalid.json", "invalid/expressionsInvalidColor.json", "invalid/expressionsInvalidSyntax.json"})
	void testInvalidJson(String path) {
		assertThrows(IllegalArgumentException.class, () -> Config.fromFile(getResource(CONFIG_PATH + path)));
	}
	
	/**
	 * Tests whether or not parsing the given configuration file results in the expected config.
	 *
	 * @param configFile      The config file which will be used in this test.
	 * @param output          The expected output file.
	 * @param verbosity       The expected logger verbosity.
	 * @param mode            The expected censor mode.
	 * @param intersectImages The expected intersecting image behavior.
	 * @param expressions     The expected expressions as a list of string-string pairs.
	 * @param defColors       The expected default colors to assign to color-less expressions.
	 */
	@ParameterizedTest(name = "Run {index}: config: {0} => output: {1}, verbosity: {2}, mode: {3}, intersectImages: {4}, expressions: {5}, defColors: {6}")
	@ArgumentsSource(ConfigProvider.class)
	void testValidConfigurations(@Nullable File configFile, @Nullable File output, @Nullable Level verbosity,
								 @Nullable Mode mode, boolean intersectImages,
								 @Nullable ArrayList<ImmutablePair<String, String>> expressions,
								 @Nullable Color[] defColors) {
		var config = Config.fromFile(configFile);
		
		assertEquals(output, config.getOutput());
		
		assertEquals(verbosity, config.getVerbosity());
		
		assertEquals(mode, config.getMode());
		
		assertEquals(intersectImages, config.getIntersectImages());
		
		var actualExpressions = config.getExpressions();
		if (expressions != null) {
			assertNotNull(actualExpressions);
			assertEquals(expressions.size(), actualExpressions.length);
			for (var i = 0; i < expressions.size(); i++) {
				var expectedExp = new Expression(expressions.get(i).getLeft(), expressions.get(i).getRight());
				var actualExp = actualExpressions[i];
				assertEquals(expectedExp.getRegex(), actualExp.getRegex());
				assertEquals(expectedExp.getColor(), actualExp.getColor());
			}
		} else {
			System.out.println(Arrays.toString(actualExpressions));
			assertNull(actualExpressions);
		}
		
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
	 * Refreshes the existing default configuration file and tests if its content is parsed (and was therefore written)
	 * as expected.
	 */
	@Test
	void testGetDefaultConfigFile() {
		var defaultConfig = Config.getDefaultConfigFile(true);
		assertNotNull(defaultConfig);
		final var content = Config.fromFile(defaultConfig);
		assertNull(content.getOutput());
		assertEquals(Level.WARN, content.getVerbosity());
		assertEquals(Mode.ALL, content.getMode());
		final var actualExp = content.getExpressions();
		assertNotNull(actualExp);
		assertEquals(1, actualExp.length);
		assertEquals(".", actualExp[0].getRegex());
		assertEquals(Settings.DEFAULT_CENSOR_COLOR, actualExp[0].getColor());
		final var actualColors = content.getDefaultColors();
		assertNotNull(actualColors);
		assertEquals(Settings.DEFAULT_COLORS.length, actualColors.length);
		for (var i = 0; i < actualColors.length; i++)
			assertEquals(Settings.DEFAULT_COLORS[i], actualColors[i]);
	}
}