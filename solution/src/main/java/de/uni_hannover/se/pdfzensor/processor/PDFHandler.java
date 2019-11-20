package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;

/**
 * An extension of the {@code PDFTextStripper} to add another level of abstraction.
 */
public interface PDFHandler {
	/**
	 * A callback for when work on a new document starts.
	 *
	 * @param doc the document which is being worked on
	 */
	void beginDocument(PDDocument doc);
	
	/**
	 * A callback for when work on a new page starts.
	 *
	 * @param doc     the document which is being worked on
	 * @param page    the PDPage that is being worked on
	 * @param pageNum the number of the page that is being worked on
	 */
	void beginPage(PDDocument doc, PDPage page, int pageNum);
	
	/**
	 * A callback for when work on a page has ended.
	 *
	 * @param doc     the document which is being worked on
	 * @param page    the PDPage that is being worked on
	 * @param pageNum the number of the page that is being worked on
	 */
	void endPage(PDDocument doc, PDPage page, int pageNum);
	
	/**
	 * A callback for when work on a document has ended.
	 *
	 * @param doc the document which is being worked on
	 */
	void endDocument(PDDocument doc);
	
	/**
	 * @param pos the TextPosition to check
	 * @return true if {@code pos} should be censored, false otherwise
	 */
	boolean shouldCensorText(TextPosition pos);
}