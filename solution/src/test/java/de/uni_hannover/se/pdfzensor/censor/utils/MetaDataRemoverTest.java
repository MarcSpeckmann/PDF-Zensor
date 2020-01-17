package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.testing.PDFChecker;
import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.PDFProvider;
import org.apache.logging.log4j.Level;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MetaDataRemoverTest {
	
	@BeforeAll
	static void initLogging() {
		Logging.init(Level.ERROR);
	}
	
	/**
	 * Tests if all of the metadata has been censored correctly in a valid PDF.
	 *
	 * @param file The pdf file to test.
	 * @throws IOException if an I/O error occurs.
	 */
	@ParameterizedTest
	@ArgumentsSource(PDFProvider.class)
	void testCensorMetadataValidFile(File file) throws IOException {
		try (final var document = PDDocument.load(file)) {
			MetadataRemover.censorMetadata(document);
			PDFChecker.assertNoMetaInformation(document);
		}
	}
	
	/** Assert that {@link MetadataRemover} is a utility class. */
	@Test
	void testMetadataRemover() {
		TestUtility.assertIsUtilityClass(MetadataRemover.class);
	}
	
	/**
	 * Tests if the {@link MetadataRemover} does not throw an exception if it is unable to retrieve the document
	 * information of the given file
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testCensorMetadataInvalidFile() {
		assertThrows(NullPointerException.class, () -> MetadataRemover.censorMetadata(null));
	}
}
