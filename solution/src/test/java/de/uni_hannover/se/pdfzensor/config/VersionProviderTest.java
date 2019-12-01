package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLHelpTest should contain all unit-tests related to {@link VersionProvider}.
 */
final class VersionProviderTest {
	
	/**
	 * Automated tests to check if version and timestamp exist in properties.
	 */
	@Test
	void testPropertyExistence() {
		final Properties properties = new Properties();
		assertDoesNotThrow(() -> properties.load(VersionProviderTest.class.getResourceAsStream("/project.properties")));
		assertNotNull(properties);
		assertNotNull(properties.getProperty("version"));
		assertNotNull(properties.getProperty("timestamp"));
	}
	
	/**
	 * Automated tests that checks if the getVersion function returns parameters that are longer than their
	 * description.
	 */
	@Test
	void testGetVersion() {
		var versionProvider = new VersionProvider();
		assertDoesNotThrow(versionProvider::getVersion);
		try {
			String[] version = versionProvider.getVersion();
			assertNotNull(version);
			assertTrue(version.length >= 2);
			assertTrue(version[1].startsWith("Version: "));
			assertTrue(version[2].startsWith("Build: "));
		} catch (IOException ex) {
			fail("failed to load VersionProvider");
		}
	}
	
	
}