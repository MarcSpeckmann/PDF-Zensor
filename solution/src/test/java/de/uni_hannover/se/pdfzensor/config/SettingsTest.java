package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.TestUtility;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.TestUtility.*;
import static org.junit.jupiter.api.Assertions.*;


/** SettingsTest should contain all unit-tests related to {@link Settings}. */
class SettingsTest {
	
	private final String CONFIG_PATH = "/configparser-test/configs/";
	
	/**
	 * Creates arguments for a function call of {@link #testSettingsNoConfig(String[], File, File, Level)} from the
	 * provided data.
	 */
	@NotNull
	private static Arguments createArgument(@NotNull String in, @NotNull String out, final int lvl) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		arguments.add("-o");
		arguments.add(out);
		if (lvl > 0)
			arguments.add("-" + "v".repeat(lvl));
		var inFile = new File(in);
		var outFile = new File(out);
		Level verbosity = Level.OFF;
		if (lvl > 0 && lvl < VERBOSITY_LEVELS.length) verbosity = VERBOSITY_LEVELS[lvl];
		else if (lvl >= VERBOSITY_LEVELS.length) verbosity = Level.ALL;
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile, verbosity);
	}
	
	/**
	 * Provides a stream with the arguments for thorough testing of {@link #testSettingsNoConfig(String[], File, File,
	 * Level)}.
	 */
	private static Stream<Arguments> testArguments() {
		String[] inputFiles = {"/pdf-files/sample.pdf", "/pdf-files/sample.bla.pdf"};
		String[] outputFiles = {"file.pdf", "src/test/resources/sample.pdf", "weirdSuffix.bla.pdf"};
		int[] verbosityLevels = IntStream.range(0, VERBOSITY_LEVELS.length + 1)
										 .toArray();
		var list = new ArrayList<Arguments>();
		for (String in : inputFiles)
			for (String out : outputFiles)
				for (int lvl : verbosityLevels)
					list.add(createArgument(in, out, lvl));
		return list.stream();
	}
	
	/** Unit-tests for {@link Settings} constructor Settings */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testSettings() {
		// if the command line argument is not given or has a faulty structure
		assertThrows(NullPointerException.class, () -> new Settings(null, (String[]) null));
		assertThrows(IllegalArgumentException.class, () -> new Settings(null, (String) null));
		// if the command line argument is given but invalid -- it is not invalid anymore if the file does not exist
		//assertThrows(IllegalArgumentException.class, () -> new Settings(null, "this_file_does_not_exist.pdf"));
	}
	
	/** Checks if the arguments are passed into the corresponding expected values. */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}")
	@MethodSource("testArguments")
	void testSettingsNoConfig(String[] args, File input, File output, Level verbosity) throws IOException {
		Logging.deinit();
		final var settings = new Settings(null, args);
		assertEquals(input, settings.getInput());
		assertEquals(output, settings.getOutput());
		var rootLogger = TestUtility.getRootLogger();
		assertTrue(rootLogger.isPresent());
		assertEquals(verbosity, rootLogger.get().getLevel());
	}
	
	/** checks the {@link Settings} constructor with there are no arguments but just a config File. */
	@Test
	void testSettingsNoArgs() {
		// the Paths to a test config files
		String configPath = getResourcePath(CONFIG_PATH + "testVerbosityAsStringValidConfig.json");
		String configPath2 = getResourcePath(CONFIG_PATH + "testConfigNegativeVerbosity.json");
		String invalidConfigPath2 = getResourcePath(CONFIG_PATH + "valid/still_a_json.txt");
		
		// if there are no arguments but just a config File
		assertThrows(IllegalArgumentException.class, () -> new Settings(configPath));
		assertThrows(IllegalArgumentException.class, () -> new Settings(configPath2));
		assertThrows(IllegalArgumentException.class, () -> new Settings(invalidConfigPath2));
	}
	
	/** Checks if it works correctly when both of config and arguments are Passed. */
	@Test
	void testSettingsWithBoth() throws IOException {
		// the Paths to a test config files
		String configPath = getResourcePath(CONFIG_PATH + "testVerbosityAsStringValidConfig.json");
		String configPath2 = getResourcePath(CONFIG_PATH + "testConfigNegativeVerbosity.json");
		String invalidConfigPath2 = getResourcePath(CONFIG_PATH + "valid/still_a_json.txt");
		
		// if there are just arguments
		var settings = new Settings(configPath, getResourcePath("/pdf-files/sample.pdf"));
		assertEquals("censoredFile.pdf", settings.getOutput().getName());
		assertEquals("sample.pdf", settings.getInput().getName());
		
		settings = new Settings(configPath2, getResourcePath("/pdf-files/sample.bla.pdf"));
		assertEquals("censoredFile.pdf", settings.getOutput().getName());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		// if there are a config file also
		settings = new Settings(configPath, getResourcePath("/pdf-files/sample.pdf"), "-o", "morePriority.pdf");
		assertEquals("morePriority.pdf", settings.getOutput().getName());
		assertEquals("sample.pdf", settings.getInput().getName());
		
		settings = new Settings(configPath2, getResourcePath("/pdf-files/sample.bla.pdf"), "-o", "tooMuchPriority.pdf");
		assertEquals("tooMuchPriority.pdf", settings.getOutput().getName());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		var path = getResourcePath("/pdf-files/");
		settings = new Settings(configPath2, getResourcePath("/pdf-files/sample.bla.pdf"), "-o", path);
		assertEquals(new File(path + "sample.bla_cens.pdf"), settings.getOutput());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		settings = new Settings(configPath2, getResourcePath("/pdf-files/sample.bla.pdf"), "-o",
								"a/path-that-does-not-exist/");
		assertEquals(new File("a/path-that-does-not-exist/sample.bla_cens.pdf").getAbsoluteFile(),
					 settings.getOutput());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		settings = new Settings(configPath2, getResourcePath("/pdf-files/sample.bla.pdf"), "-o", configPath2);
		assertEquals(new File(configPath2), settings.getOutput());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		//if the config is invalid
		settings = new Settings(invalidConfigPath2, getResourcePath("/pdf-files/sample.bla.pdf"));
		assertEquals("censoredFile.pdf", settings.getOutput().getName());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		// if config is overwritten correctly by the CLArgs with less specific level (config has Level.DEBUG)
		Logging.deinit();
		settings = new Settings(configPath, getResourcePath("/pdf-files/sample.bla.pdf"), "-vvvvvvv");
		var rootLogger = TestUtility.getRootLogger();
		assertTrue(rootLogger.isPresent());
		assertEquals(Level.ALL, rootLogger.get().getLevel());
		
		// if config is overwritten correctly by the CLArgs with more specific level (config has Level.DEBUG)
		Logging.deinit();
		settings = new Settings(configPath, getResourcePath("/pdf-files/sample.bla.pdf"), "-vv");
		rootLogger = TestUtility.getRootLogger();
		assertTrue(rootLogger.isPresent());
		assertEquals(Level.ERROR, rootLogger.get().getLevel());
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