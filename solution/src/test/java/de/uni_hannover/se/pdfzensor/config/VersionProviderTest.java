package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLHelpTest should contain all unit-tests related to {@link VersionProvider}.
 */
class VersionProviderTest {
	
	/**
	 * TODO: ADD JavaDoc
	 */
	@Test
	void getVersion() {
		var versionProvider = new VersionProvider();
		assertDoesNotThrow(versionProvider::getVersion);
		assertNotNull(versionProvider.getVersion());
		assertTrue(versionProvider.getVersion().length > 0, "The version output has no lines");
	}
}