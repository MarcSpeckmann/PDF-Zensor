package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;


/**
 * Tests the {@link TextProcessor}
 * pageBeginCounter count how often start page has been visited to check if the whole document has been reviewed.
 * pageEndCounter count how often end page was called.
 * checkOrderSequenceCounter to check if the order is correct.
 *
 */
public class TextProcessorTest {
	private int pageBeginCounter = 0;
	private boolean beginDocument = false;
	private boolean endDocument = false;
	private int checkOrderSequenceCounter = 0;
	private PDFHandler handler = new PDFHandler() {
		@Override
		public void beginDocument(final PDDocument doc) {
			beginDocument = true;
		}

		@Override
		public void beginPage(final PDDocument doc, final PDPage page, final int pageNum) {
			pageBeginCounter += 1;
			assertEquals(0, checkOrderSequenceCounter);
			checkOrderSequenceCounter += 1;
		}

		@Override
		public void endPage(final PDDocument doc, final PDPage page, final int pageNum) {
			assertEquals(1, checkOrderSequenceCounter);
			checkOrderSequenceCounter -= 1;
		}

		@Override
		public void endDocument(final PDDocument doc) {
			endDocument = true;
		}

		@Override
		public boolean shouldCensorText(final TextPosition pos) {
			return false;
		}
	};

	/**
	 * Tests if the processing of the document is done in the correct order
	 * @throws IOException if there is an Error in the TextProcessor() or at file-loading
	 */
	@Test
	void testTextProcessingOrderOfFunctionCallsInTextProcessor() throws IOException {
		TextProcessor tp = new TextProcessor(handler);
		String path = "src/test/resources/pdf-files/cusatop-intro.pdf";
		File file = new File(path);
		PDDocument doc;
		doc = PDDocument.load(file);
		tp.getText(doc);

		var numberOfPages = doc.getPages().getCount();

		assertEquals(numberOfPages, pageBeginCounter);
		assertEquals(0, checkOrderSequenceCounter);
		assertTrue(beginDocument);
		assertTrue(endDocument);
	}

	/**
	 * Tests the TextProcessor-constructor
	 */
	@Test
	void testTextProcessorConstructor(){
		assertThrows(NullPointerException.class, ()-> new TextProcessor(null));
	}

	/**
	 * Tests the behaviour of the TextProcessor-constructor with a valid handler
	 */
	@Test
	void testValidInput(){
		assertDoesNotThrow(()-> new TextProcessor(handler));
	}

}
