package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;

/** An abstraction of the {@link org.apache.pdfbox.text.PDFTextStripper} to simplify and hide information by the API. */
public interface PDFHandler {
	/**
	 * A callback for when work on a new document starts.
	 *
	 * @param doc the document which is being worked on.
	 */
	void beginDocument(PDDocument doc);
	
	/**
	 * A callback for when work on a new page starts.
	 *
	 * @param doc     the document which is being worked on.
	 * @param page    the PDPage that is being worked on.
	 * @param pageNum the number of the page that is being worked on.
	 */
	void beginPage(PDDocument doc, PDPage page, int pageNum);
	
	/**
	 * A callback for when work on a page has ended.
	 *
	 * @param doc     the document which is being worked on.
	 * @param page    the PDPage that is being worked on.
	 * @param pageNum the number of the page that is being worked on.
	 */
	void endPage(PDDocument doc, PDPage page, int pageNum);
	
	/**
	 * A callback for when work on a document has ended.
	 *
	 * @param doc the document which is being worked on.
	 */
	void endDocument(PDDocument doc);
	
	/**
	 * This callback is called by {@link TextProcessor} for each encountered Glyph to check if it should be censored or
	 * not.
	 *
	 * @param pos the TextPosition to check.
	 * @return True if {@code pos} should be censored, false otherwise.
	 */
	boolean shouldCensorText(PDPage page, TextPosition pos);
}