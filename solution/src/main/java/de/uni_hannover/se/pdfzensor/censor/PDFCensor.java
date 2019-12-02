package de.uni_hannover.se.pdfzensor.censor;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Runs functions to place censoring rectangles (or censoring graphics) and removes elements of the PDF depending on the
 * annotations and the mode
 */
public final class PDFCensor implements PDFHandler {
	/** The maximum horizontal gap between two glyphs which would still be bridged by the censor bar. */
	private static final float MAX_BRIDGED_WIDTH = 12f;
	
	/** The maximum difference in the y-coordinate between two glyphs which would still be bridged by the censor bar. */
	private static final float MAX_BRIDGED_HEIGHT = .5f;
	
	/** The list of bounds-color pairs which will be censored. */
	private List<ImmutablePair<Rectangle2D, Color>> boundingBoxes;
	
	private Predicate<Rectangle2D> removePredicate;
	
	private static final Logger LOGGER = Logging.getLogger();
	
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
		boundingBoxes = new ArrayList<>();
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
		Objects.requireNonNull(boundingBoxes).clear();
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
		try {
			drawCensorBars(doc, page);
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "There was an error writing the page contents of page {}.", pageNum, e);
		}
	}
	
	/**
	 * A callback for when work on a document has ended.
	 *
	 * @param doc the document which is being worked on
	 */
	@Override
	public void endDocument(PDDocument doc) {
		boundingBoxes = null;
	}
	
	/**
	 * @param pos the TextPosition (represents string + its character's screen positions) to check
	 * @return true if <code>pos</code> should be censored, false otherwise
	 */
	@Override
	public boolean shouldCensorText(TextPosition pos) {
		getTextPositionInfo(pos).ifPresent(this::addOrExtendBoundingBoxes);
		return true;
	}
	
	/**
	 * Either adds the given <code>pair</code> to the <code>boundingBoxes</code> list or extends the last element of the
	 * list to also cover the the bounds of the given pair (if the bounds are within the margin and the color is the
	 * same).
	 * <br>
	 * Whether or not the previous bounds will be extended depends on the coordinates of the glyphs and {@link
	 * #MAX_BRIDGED_WIDTH} and {@link #MAX_BRIDGED_HEIGHT}.
	 *
	 * @param pair The pair to include in the <code>boundingBoxes</code> list.
	 */
	private void addOrExtendBoundingBoxes(@NotNull final ImmutablePair<Rectangle2D, Color> pair) {
		if (!boundingBoxes.isEmpty()) {
			var bb = pair.getKey();
			var last = boundingBoxes.get(boundingBoxes.size() - 1);
			var l = last.getKey();
			if (last.getValue().equals(pair.getValue()) &&
				Math.abs(bb.getY() - l.getY()) <= MAX_BRIDGED_HEIGHT &&
				Math.abs(bb.getX() - (l.getX() + l.getWidth())) <= MAX_BRIDGED_WIDTH) {
				boundingBoxes.remove(last);
				var width = Math.abs(bb.getX() + bb.getWidth() - l.getX());
				bb.setRect(l.getX(), l.getY(), width, (l.getHeight() + bb.getHeight()) * .5f);
			}
			pair.getKey().setRect(bb);
		}
		boundingBoxes.add(pair);
	}
	
	/**
	 * Transforms the <code>pos</code> into either a pair of its bounds and the color to censor the glyph with or an
	 * empty optional.
	 *
	 * @param pos The TextPosition to transform into a bounds-color pair.
	 * @return An optional containing either the bounds-color pair or nothing, if an error occurred.
	 */
	private Optional<ImmutablePair<Rectangle2D, Color>> getTextPositionInfo(@NotNull TextPosition pos) {
		try {
			var font = pos.getFont();
			var bb = font.getBoundingBox();
			float height = bb.getHeight() + bb.getLowerLeftY();
			float width = 0;
			for (var i : pos.getCharacterCodes())
				width += font.getWidth(i);
			
			var at = pos.getTextMatrix().createAffineTransform();
			if (font instanceof PDType3Font)
				at.concatenate(font.getFontMatrix().createAffineTransform());
			else
				at.scale(.001f, .001);
			
			var r = new Rectangle2D.Float(0, 0, width, height);
			return Optional.of(new ImmutablePair<>(at.createTransformedShape(r).getBounds2D(), Color.DARK_GRAY));
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "There was an error handling the font.", e);
		}
		return Optional.empty();
	}
	
	/**
	 * Draws the censor bars stored in {@link #boundingBoxes} with their respective color in the given
	 * <code>document</code> on the given <code>page</code>.
	 *
	 * @param doc  the document which is being worked on
	 * @param page the PDPage (current pdf page) that is being worked on
	 * @throws IOException If there was an I/O error writing the contents of the page.
	 */
	private void drawCensorBars(PDDocument doc, PDPage page) throws IOException {
		try (var pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.PREPEND, true)) {
			pageContentStream.saveGraphicsState();
		}
		try (var pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
			pageContentStream.restoreGraphicsState();
			for (var pair : boundingBoxes) {
				pageContentStream.setNonStrokingColor(pair.getValue());
				var r = pair.getKey();
				pageContentStream.addRect((float) r.getX(), (float) r.getY(), (float) r.getWidth(),
										  (float) r.getHeight());
				pageContentStream.fill();
			}
		}
	}
}
