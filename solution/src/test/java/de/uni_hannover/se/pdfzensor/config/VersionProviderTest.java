package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;
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
	void getProperties() {
		final Properties properties = new Properties();
		try {
			properties.load(Objects.requireNonNull(this.getClass()
													   .getResourceAsStream("/project.properties")));
		} catch (IOException ex) {
			fail("failed to load properties");
		}
		assertNotNull(properties);
		assertNotNull(properties.getProperty("version"));
		assertNotNull(properties.getProperty("timestamp"));
	}
	
	/**
	 * Automated tests that checks if the getVersion function returns parameters that are longer than their
	 * description.
	 */
	@Test
	void getVersion() {
		var versionProvider = new VersionProvider();
		assertDoesNotThrow(versionProvider::getVersion);
		try {
			String[] version = versionProvider.getVersion();
			assertNotNull(version);
			assertTrue(version[1].length() > "Version: ".length());
			assertTrue(version[2].length() > "Build: ".length());
		} catch (IOException ex) {
			fail("failed to load VersionProvider");
		}
	}
	
	
}