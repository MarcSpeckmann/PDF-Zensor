package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.TestUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** PDFProcessorTest should contain all unit-tests related to {@link PDFProcessor}. */
class PDFProcessorTest {
	/** Path to the pdf-tests Resources */
	private static final String PDF_PATH = "/pdf-files/";
	
	/**to checks a pdf-document is being processed In the right order */
	private int checkOrderCounter = 0;
	/**to checks a pdf-document is being closed and opened In the right order */
	private boolean isStarted = false;

	/**
	 * a dummy initialization of the that we need to test if a pdf-document is being
	 * processed In the right order, also to check if the functions were called at all,
	 * its remove all the pages of the processed document so can we be sure that its been processed by checking
	 * if the document is empty
	 * */
	PDFHandler dummyHandler = new PDFHandler() {
		@Override
		public void beginDocument(final PDDocument doc) {
			// it should not be opened by now
			assertFalse(isStarted);
			// mark it as open
			isStarted = true;
		}
		@Override
		public void beginPage(final PDDocument doc, final PDPage page, final int pageNum) {
			//should be true as the document is opened
			assertTrue(isStarted);
			//it should not call beginPage before it end the last page
			assertEquals(0, checkOrderCounter);
			// increments it by opening a new page
			checkOrderCounter += 1;
			// to checks if the function is called at all
			doc.removePage(page);
		}
		@Override
		public void endPage(final PDDocument doc, final PDPage page, final int pageNum) {
			//should be true as the document is opened
			assertTrue(isStarted);
			//it should not call endPage before it begin the page
			assertEquals(1, checkOrderCounter);
			// decrements it by ending the page
			checkOrderCounter -= 1;
		}
		@Override
		public void endDocument(final PDDocument doc) {
			// there are no pages that es being not closed
			assertEquals(0, checkOrderCounter);
			//should be true as the document is opened
			assertTrue(isStarted);
			isStarted = false;
		}
		@Override
		public boolean shouldCensorText(final TextPosition pos) {
			return false;
		}
	};

	/**
	 * tests the {@link PDFProcessor#PDFProcessor(PDFHandler)} by valid and invalid input
	 */
	@Test
	void testConstructor()  {
		assertThrows(NullPointerException.class, () -> new PDFProcessor(null));
		assertDoesNotThrow(() -> new PDFProcessor(dummyHandler));
	}

	/**
	 * @return A stream of all files in the directory {@link #PDF_PATH}.
	 * @throws IOException If there is an error loading the files.
	 */
	private static Stream<Arguments> testForFile() throws IOException {
		return Files.walk(Paths.get(TestUtility.getResource(PDF_PATH).getAbsolutePath())).map(Path::toFile)
				.filter(File::isFile).map(Arguments::of);
	}

	/**
	 *  checks if a pdf-document is being processed In the right order, also it checks
	 *  if the functions were called at all
	 * @param file The PDF File that is being processed.
	 * @throws IOException if an IO error occurs.
	 */
	@ParameterizedTest(name = "Run {index}: file: {0}")
	@MethodSource("testForFile")
	void testProcessing(File file) throws IOException {

		var doc = PDDocument.load(file);
		PDFProcessor processor = new PDFProcessor(dummyHandler);
		processor.process(doc);

		// it should be always 0 because the dummy handler removes all pages
		assertEquals(0,doc.getPages().getCount());
		// cause the document must be closed after all
		assertFalse(isStarted);
	}
}