package de.uni_hannover.se.pdfzensor;

import org.junit.jupiter.api.Test;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.*;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.*;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
	
	@Test
	public void sampleTest() {
		//This test always succeeds
		assertTrue(true);
	}
	
	/**
	 * This method is testing valid and invalid pdf input files.
	 */
	@Test
	void testInputFiles() {
		assertThrows(NullPointerException.class, () -> App.main(null));
		
		assertExitCode(-1, () -> App.main(new String[]{"NotExisting.pdf"}));
		assertExitCode(-1, () -> App.main(new String[]{"pom.xml"}));
		assertExitCode(-1, () -> App
				.main(new String[]{getResourcePath(CORRUPTED_PDF_RESOURCE_PATH + "sample(pdfVersionDeleted).pdf")}));
		assertExitCode(-1,
					   () -> App.main(new String[]{getResourcePath(CORRUPTED_PDF_RESOURCE_PATH + "EncryptedPDF.pdf")}));
		
		assertDoesNotThrow(() -> App.main(new String[]{getResourcePath(PDF_RESOURCE_PATH + "sample.pdf")}));
		assertDoesNotThrow(() -> App.main(new String[]{getResourcePath(PDF_RESOURCE_PATH + "sample.bla.pdf")}));
		assertDoesNotThrow(() -> App.main(new String[]{getResourcePath(PDF_RESOURCE_PATH + "StillAPDF")}));
	}
}