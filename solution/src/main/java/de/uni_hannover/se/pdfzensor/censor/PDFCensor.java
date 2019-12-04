package de.uni_hannover.se.pdfzensor.censor;

import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Runs functions to place censoring rectangles (or censoring graphics) and removes elements of the PDF depending on the
 * annotations and the mode
 */
public final class PDFCensor implements PDFHandler {
	private Predicate<Rectangle2D> removePredicate;
	
	/**
	 * @param settings Settings that contain information about the mode and expressions
	 */
	public PDFCensor(@NotNull Settings settings) {
		Objects.requireNonNull(settings);
		this.removePredicate = rect -> true;
	}
	
	/**
	 * A callback for when work on a new document starts.
	 *
	 * @param doc the document which is being worked on
	 */
	@Override
	public void beginDocument(PDDocument doc) {
	
	}
	
	/**
	 * A callback for when work on a new page starts.
	 *
	 * @param doc     the document which is being worked on
	 * @param page    the PDPage (current pdf page) that is being worked on
	 * @param pageNum the number of the page that is being worked on
	 */
	@Override
	public void beginPage(PDDocument doc, PDPage page, int pageNum) {
	
	}
	
	/**
	 * A callback for when work on a page has ended.
	 *
	 * @param doc     the document which is being worked on
	 * @param page    the PDPage (current pdf page) that is being worked on
	 * @param pageNum the number of the page that is being worked on
	 */
	@Override
	public void endPage(PDDocument doc, PDPage page, int pageNum) {
	
	}
	
	/**
	 * A callback for when work on a document has ended.
	 *
	 * @param doc the document which is being worked on
	 */
	@Override
	public void endDocument(PDDocument doc) {
	}
	
	/**
	 * @param pos the TextPosition (represents string + its character's screen positions) to check
	 * @return true if {@code pos} should be censored, false otherwise
	 */
	@Override
	public boolean shouldCensorText(TextPosition pos) {
		return true;
	}
}
