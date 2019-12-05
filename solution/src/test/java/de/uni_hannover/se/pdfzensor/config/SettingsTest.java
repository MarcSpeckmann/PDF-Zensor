package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.CLArgumentProvider;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.SettingsProvider;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.testing.LoggingUtility.getRootLogger;
import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.*;
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
	
	/** Checks if the arguments are parsed into the corresponding expected values. */
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
	 * @param configName The path to the configuration file.
	 * @param args       The command-line arguments.
	 * @param input      The input file.
	 * @param output     The output file.
	 * @param verbosity  The verbosity level of the logger.
	 * @param mode       The mode to use when censoring.
	 * @throws IOException If the configuration file could not be parsed.
	 */
	@ParameterizedTest(name = "Run {index}: config: {0}, args: {1} => in: {2}, out: {3}, verbosity: {4}, mode: {5}")
	@ArgumentsSource(SettingsProvider.class)
	void testValidConfigurations(@Nullable String configName, @NotNull final String[] args, @NotNull File input,
								 @Nullable File output, @Nullable Level verbosity,
								 @Nullable Mode mode) throws IOException {
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
	}
	
	/** dummy Unit-tests for function getLinkColor */
	@Test
	void testLinkColor() throws IOException {
		final var settings = new Settings(null, getResource("/pdf-files/sample.pdf").getAbsolutePath());
		assertEquals(Color.BLUE, settings.getLinkColor());
	}
	
	/** dummy Unit-tests for function getExpressions */
	@Test
	void getExpressions() throws IOException {
		final var settings = new Settings(null, getResource("/pdf-files/sample.pdf").getAbsolutePath());
		for (int i = 0; i < settings.getExpressions().length; i++) {
			assertEquals(new Expression[]{new Expression(".", "#000000")}[0].getColor(),
						 settings.getExpressions()[i].getColor());
		}
	}
}