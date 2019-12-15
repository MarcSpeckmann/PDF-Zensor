package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

//TODO add more tests and maybe alter the structure of the tests (one test may be to check an entire file for the metadata)
class MetaDataRemoverTest {
	/**
	 * Tests if all of the metadata have been censored correctly using a valid PDF.
	 *
	 * @throws IOException if an IO error occurs.
	 */
	@Test
	void testCensorMetadataValidFile() throws IOException {
		var file = TestUtility.getResource("/pdf-files/fullMetadata.pdf");
		try (final var document = PDDocument.load(file)) {
			MetadataRemover.censorMetadata(document);
			
			var docInfo = document.getDocumentInformation();
			assertTrue(StringUtils.isEmpty(docInfo.getAuthor()));
			assertTrue(StringUtils.isEmpty(docInfo.getCreator()));
			assertTrue(StringUtils.isEmpty(docInfo.getProducer()));
			assertTrue(StringUtils.isEmpty(docInfo.getTitle()));
			assertTrue(StringUtils.isEmpty(docInfo.getSubject()));
			assertTrue(StringUtils.isEmpty(docInfo.getKeywords()));
			assertNull(docInfo.getCreationDate());
			assertNull(docInfo.getModificationDate());
		}
	}
	
	/** Assert that {@link MetadataRemover} is a utility class. */
	@Test
	void testMetadataRemover() {
		TestUtility.assertIsUtilityClass(MetadataRemover.class);
	}
	
	/**
	 * Tests if the MetadateRemover does not throw an exception if it is unable to retrieve the document information of
	 * the given file
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testCensorMetadataInvalidFile() {
		assertThrows(NullPointerException.class, () -> MetadataRemover.censorMetadata(null));
	}
}
