package de.uni_hannover.se.pdfzensor;

import org.junit.jupiter.api.Test;

import static de.uni_hannover.se.pdfzensor.App.main;
import static de.uni_hannover.se.pdfzensor.testing.TestConstants.*;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.*;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
	/**
	 * This method is testing valid and invalid pdf input files.
	 */
	@Test
	void testInputFiles() {
		assertThrows(NullPointerException.class, () -> main((String[]) null));
		assertThrows(NullPointerException.class, () -> main((String) null));

		assertExitCode(-1, () -> main("NotExisting.pdf"));
		assertExitCode(-1, () -> main("pom.xml"));
		assertExitCode(-1, () -> main(getResourcePath(CORRUPTED_PDF_RESOURCE_PATH + "sample(pdfVersionDeleted).pdf")));
		
		assertDoesNotThrow(() -> main(getResourcePath(PDF_RESOURCE_PATH + "sample.pdf")));
		assertDoesNotThrow(() -> main(getResourcePath(PDF_RESOURCE_PATH + "sample.bla.pdf")));
		assertDoesNotThrow(() -> main(getResourcePath(PDF_RESOURCE_PATH + "StillAPDF")));
	}
}