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
import static de.uni_hannover.se.pdfzensor.utils.Utils.*;
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
		
		assertNotNull(settings.getOutput());
		if (output != null)
			assertEquals(output, settings.getOutput());
		
		var rootLogger = getRootLogger();
		assertTrue(rootLogger.isPresent());
		assertNotNull(rootLogger.get().getLevel());
		if (verbosity != null)
			assertEquals(verbosity, rootLogger.get().getLevel());
		
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
	@SuppressWarnings("unchecked")
	@ParameterizedTest(name = "Run {index}: config: {0}, args: {1} => in: {2}, out: {3}, verbosity: {4}, mode: {5}, expressions: {6}, defColors: {7}")
	@ArgumentsSource(SettingsProvider.class)
	void testSettingsValidConfig(@Nullable String configName, @NotNull final String[] args, @NotNull File input,
								 @Nullable File output, @Nullable Level verbosity,
								 @Nullable Mode mode,
								 @NotNull ArrayList<ImmutablePair<String, String>> expressions,
								 @Nullable Color[] defColors) throws IOException {
		Logging.deinit();
		var configPath = configName == null ? null : getResourcePath(CONFIG_PATH + configName);
		var settings = new Settings(configPath, args);
		
		assertEquals(input.getName(), settings.getInput().getName());
		
		assertNotNull(settings.getOutput());
		if (output != null)
			assertEquals(output.getName(), settings.getOutput().getName());
		
		var rootLogger = getRootLogger();
		assertTrue(rootLogger.isPresent());
		assertNotNull(rootLogger.get().getLevel());
		if (verbosity != null)
			assertEquals(verbosity, rootLogger.get().getLevel());
		
		assertNotNull(settings.getMode());
		if (mode != null)
			assertEquals(mode, settings.getMode());
		
		// the default fallback Expression is expected to be appended
		expressions.add(new ImmutablePair<>(".", colorToString(Settings.DEFAULT_CENSOR_COLOR)));
		assertEqualExpressions(expressions.toArray(new ImmutablePair[0]), settings.getExpressions(), defColors);
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
	
	/**
	 * Asserts that the expected expressions list equals the actual {@link Expression} array. <code>expected</code> may
	 * not be an {@link Expression} array to preserve the null values for colors (to test the validity of
	 * <code>defColors</code> which requires knowing when colors from <code>defColors</code> are expected to be used,
	 * which is when the color equals null / none was given; {@link Expression#getColor()} never returns null and is
	 * therefore not usable for this distinction).
	 * <br>
	 * The color (of each actual {@link Expression}) can be one of the following:
	 * <ul>
	 *     <li>The color which was directly specified alongside the regex of the {@link Expression}.</li>
	 *     <li>The next unused color from the <code>defColors</code> array if there is one.</li>
	 *     <li>{@link Settings#DEFAULT_CENSOR_COLOR} if none of the previous cases applied.</li>
	 * </ul>
	 *
	 * @param expected  The expected array of expressions as a string-string pair.
	 * @param actual    The actual {@link Expression}s array.
	 * @param defColors The containing the given default colors.
	 * @see Expression#getColor()
	 */
	private void assertEqualExpressions(@NotNull ImmutablePair<String, String>[] expected, @NotNull Expression[] actual,
										@Nullable Color[] defColors) {
		assertNotNull(expected);
		assertNotNull(actual);
		assertEquals(expected.length, actual.length);
		for (int i = 0, usedColors = 0; i < expected.length; i++) {
			assertEquals(expected[i].getLeft(), actual[i].getRegex());
			
			Color expColor = Settings.DEFAULT_CENSOR_COLOR; // default color if no other case applies
			if (expected[i].getRight() != null) // color was set alongside the regex
				expColor = getColorOrNull(expected[i].getRight());
			else if (defColors != null && usedColors < defColors.length) // color was assigned from defaults
				expColor = defColors[usedColors++];
			assertEquals(expColor, actual[i].getColor());
		}
	}
}