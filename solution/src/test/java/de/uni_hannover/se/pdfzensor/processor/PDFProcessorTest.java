package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** PDFProcessorTest should contain all unit-tests related to {@link PDFProcessor}. */
class PDFProcessorTest {
	/** to checks a pdf-document is being processed In the right order */
	private int checkOrderCounter = 0;
	/** to checks a pdf-document is being closed and opened In the right order */
	private boolean isStarted = false;
	
	/**
	 * a dummy initialization of the that we need to test if a pdf-document is being processed In the right order, also
	 * to check if the functions were called at all, its remove all the pages of the processed document so can we be
	 * sure that its been processed by checking if the document is empty
	 */
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
	void testConstructor() {
		assertThrows(NullPointerException.class, () -> new PDFProcessor(null));
		assertDoesNotThrow(() -> new PDFProcessor(dummyHandler));
	}
}