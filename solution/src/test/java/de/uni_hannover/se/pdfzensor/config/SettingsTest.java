package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.CLArgumentProvider;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;

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
	
	/** Checks if the arguments are passed into the corresponding expected values. */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}")
	@ArgumentsSource(CLArgumentProvider.class)
	void testSettingsNoConfig(String[] args, File input, File output, Level verbosity) throws IOException {
		Logging.deinit();
		final var settings = new Settings(null, args);
		assertEquals(input, settings.getInput());
		assertEquals(output, settings.getOutput());
		var rootLogger = getRootLogger();
		assertTrue(rootLogger.isPresent());
		assertEquals(verbosity, rootLogger.get().getLevel());
	}
	
	/** Checks if it works correctly when both of config and arguments are Passed. */
	@Test
	void testSettingsWithBoth() throws IOException {
		// the paths to a test config files
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
		new Settings(configPath, getResourcePath("/pdf-files/sample.bla.pdf"), "-vvvvvvv");
		var rootLogger = getRootLogger();
		assertTrue(rootLogger.isPresent());
		assertEquals(Level.ALL, rootLogger.get().getLevel());
		
		// if config is overwritten correctly by the CLArgs with more specific level (config has Level.DEBUG)
		Logging.deinit();
		new Settings(configPath, getResourcePath("/pdf-files/sample.bla.pdf"), "-vv");
		rootLogger = getRootLogger();
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