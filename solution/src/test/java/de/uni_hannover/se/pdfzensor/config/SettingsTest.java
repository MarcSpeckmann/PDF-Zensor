package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.TestUtility.*;
import static org.junit.jupiter.api.Assertions.*;


/** SettingsTest should contain all unit-tests related to {@link Settings}. */
class SettingsTest {
	
	private final String CONFIG_PATH = "/configparser-test/configs/";
	
	/**
	 * Creates arguments for a function call of {@link #testSettingsNoConfig(String[], File, File)} from the provided
	 * data.
	 */
	@NotNull
	private static Arguments createArgument(@NotNull String in, @NotNull String out) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		arguments.add("-o");
		arguments.add(out);
		var inFile = new File(in).getAbsoluteFile();
		var outFile = new File(out).getAbsoluteFile();
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile);
	}
	
	/**
	 * Provides a stream with the arguments for thorough testing of {@link #testSettingsNoConfig(String[], File,
	 * File)}.
	 */
	private static Stream<Arguments> testArguments() {
		String[] inputFiles = {getResource("/pdf-files/sample.pdf").getAbsolutePath(), getResource(
				"/pdf-files/sample.bla.pdf").getAbsolutePath()};
		String[] outputFiles = {"file.pdf", "src/test/resources/sample.pdf", "weirdSuffix.bla.pdf"};
		var list = new ArrayList<Arguments>();
		for (String in : inputFiles)
			for (String out : outputFiles)
				list.add(createArgument(in, out));
		return list.stream();
	}
	
	/** Unit-tests for {@link Settings} constructor Settings */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testSettings() {
		// if the command line argument is not given or has a faulty structure
		assertThrows(NullPointerException.class, () -> new Settings("", (String[]) null));
		assertThrows(IllegalArgumentException.class, () -> new Settings("", (String) null));
		// if the command line argument is given but invalid
		assertThrows(IllegalArgumentException.class, () -> new Settings("", "this_file_does_not_exist.pdf"));
	}
	
	/** Checks if the arguments are passed into the corresponding expected values. */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}")
	@MethodSource("testArguments")
	void testSettingsNoConfig(String[] args, File input, File output) throws IOException {
		final var settings = new Settings("", args);
		assertEquals(input, settings.getInput());
		assertEquals(output, settings.getOutput());
	}
	
	/** checks the {@link Settings} constructor with there are no arguments but just a config File. */
	@Test
	void testSettingsNoArgs() {
		// the Paths to a test config files
		String configPath = getResourcePath(CONFIG_PATH + "testVerbosityAsStringValidConfig.json");
		String configPath2 = getResourcePath(CONFIG_PATH + "testConfigNegativeVerbosity.json");
		String invalidConfigPath2 = getResourcePath(CONFIG_PATH + "invalid/not_a_json.txt");
		
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
		String invalidConfigPath2 = getResourcePath(CONFIG_PATH + "invalid/not_a_json.txt");
		
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
		
		settings = new Settings(configPath2, getResourcePath("/pdf-files/sample.bla.pdf"), "-o", "a/path-that-does-not-exist/");
		assertEquals(new File("a/path-that-does-not-exist/sample.bla_cens.pdf").getAbsoluteFile(), settings.getOutput());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		//not a pdf is passed as output
		settings = new Settings(configPath2, getResourcePath("/pdf-files/sample.bla.pdf"), "-o", configPath2);
		assertEquals("censoredFile.pdf", settings.getOutput().getName());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
		
		//if the config is invalid
		settings = new Settings(invalidConfigPath2, getResourcePath("/pdf-files/sample.bla.pdf"));
		assertEquals("sample.bla_cens.pdf", settings.getOutput().getName());
		assertEquals("sample.bla.pdf", settings.getInput().getName());
	}
	
	/** dummy Unit-tests for function getLinkColor */
	@Test
	void testLinkColor() throws IOException {
		final var settings = new Settings("", getResource("/pdf-files/sample.pdf").getAbsolutePath());
		assertEquals(Color.BLUE, settings.getLinkColor());
	}
	
	
	/** dummy Unit-tests for function getExpressions */
	@Test
	void getExpressions() throws IOException {
		final var settings = new Settings("", getResource("/pdf-files/sample.pdf").getAbsolutePath());
		for (int i = 0; i < settings.getExpressions().length; i++) {
			assertEquals(new Expression[]{new Expression(".", "#000000")}[0].getColor(),
						 settings.getExpressions()[i].getColor());
		}
	}
}