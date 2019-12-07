package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.CLArgumentProvider;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.SettingsProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.testing.LoggingUtility.getRootLogger;
import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.*;
import static de.uni_hannover.se.pdfzensor.utils.Utils.colorToString;
import static org.junit.jupiter.api.Assertions.*;

/** SettingsTest should contain all unit-tests related to {@link Settings}. */
class SettingsTest {
	
	/** Unit-tests for {@link Settings} constructor Settings */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testFaultyArguments() {
		assertThrows(NullPointerException.class, () -> new Settings(null, (String[]) null));
		assertThrows(IllegalArgumentException.class, () -> new Settings(null, (String) null));
		assertThrows(IllegalArgumentException.class, () -> new Settings(null));
	}
	
	/**
	 * Checks if the arguments are parsed into the corresponding expected values.
	 *
	 * @param args      The arguments from which the Settings constructed.
	 * @param input     The input file.
	 * @param output    The expected output file.
	 * @param verbosity The expected logger verbosity level.
	 * @param mode      The expected censoring mode.
	 * @throws IOException If the configuration file could not be parsed.
	 */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}, mode: {4}")
	@ArgumentsSource(CLArgumentProvider.class)
	void testSettingsNoConfig(String[] args, File input, File output, Level verbosity, Mode mode) throws IOException {
		Logging.deinit();
		final var settings = new Settings(null, args);
		assertEquals(input, settings.getInput());
		if (output != null)
			assertEquals(output, settings.getOutput());
		else
			assertNotNull(settings.getOutput());
		var rootLogger = getRootLogger();
		assertTrue(rootLogger.isPresent());
		if (verbosity != null)
			assertEquals(verbosity, rootLogger.get().getLevel());
		else
			assertNotNull(rootLogger.get().getLevel());
		assertEquals(Objects.requireNonNullElse(mode, Mode.ALL), settings.getMode());
	}
	
	/**
	 * Tests the correctness of the settings when a configuration file and command-line arguments are present.
	 *
	 * @param configName  The path to the configuration file.
	 * @param args        The command-line arguments.
	 * @param input       The input file.
	 * @param output      The output file.
	 * @param verbosity   The verbosity level of the logger.
	 * @param mode        The mode to use when censoring.
	 * @param expressions The expressions which are expected to be parsed (exclusive the fallback Expression).
	 * @param defColors   The default colors from which one will be added to an Expression without a color.
	 * @throws IOException If the configuration file could not be parsed.
	 */
	@ParameterizedTest(name = "Run {index}: config: {0}, args: {1} => in: {2}, out: {3}, verbosity: {4}, mode: {5}, expressions: {6}, defColors: {7}")
	@ArgumentsSource(SettingsProvider.class)
	void testValidConfigurations(@Nullable String configName, @NotNull final String[] args, @NotNull File input,
								 @Nullable File output, @Nullable Level verbosity,
								 @Nullable Mode mode,
								 @NotNull ArrayList<ImmutablePair<String, String>> expressions,
								 @Nullable Color[] defColors) throws IOException {
		Logging.deinit();
		var configPath = configName == null ? null : getResourcePath(CONFIG_PATH + configName);
		var settings = new Settings(configPath, args);
		
		assertEquals(input.getName(), settings.getInput().getName());
		
		if (output != null)
			assertEquals(output.getName(), settings.getOutput().getName());
		else
			assertNotNull(settings.getOutput());
		
		var rootLogger = getRootLogger();
		assertTrue(rootLogger.isPresent());
		if (verbosity != null)
			assertEquals(verbosity, rootLogger.get().getLevel());
		else
			assertNotNull(rootLogger.get().getLevel());
		
		if (mode != null)
			assertEquals(mode, settings.getMode());
		else
			assertNotNull(settings.getMode());
		
		var actualExpressions = settings.getExpressions();
		// the default fallback Expression is expected to be appended
		expressions.add(new ImmutablePair<>(".", colorToString(Settings.DEFAULT_CENSOR_COLOR)));
		assertNotNull(actualExpressions);
		assertEquals(expressions.size(), actualExpressions.length);
		for (int i = 0, usedDefColors = 0; i < actualExpressions.length; i++) {
			var expectedExp = new Expression(expressions.get(i).getLeft(), expressions.get(i).getRight());
			var actualExp = actualExpressions[i];
			assertEquals(expectedExp.getRegex(), actualExp.getRegex());
			
			// color is correct (set by value, from default colors array or the default censor color)
			if (expressions.get(i).getRight() != null)
				assertEquals(expectedExp.getColor(), actualExp.getColor());
			else if (defColors != null && usedDefColors < defColors.length)
				assertEquals(defColors[usedDefColors++], actualExp.getColor());
			else
				assertEquals(Settings.DEFAULT_CENSOR_COLOR, actualExp.getColor());
		}
	}
	
	/**
	 * dummy Unit-tests for function getLinkColor
	 *
	 * @throws IOException If the configuration file could not be parsed.
	 */
	@Test
	void testLinkColor() throws IOException {
		final var settings = new Settings(null, getResource("/pdf-files/sample.pdf").getAbsolutePath());
		assertEquals(Color.BLUE, settings.getLinkColor());
	}
}