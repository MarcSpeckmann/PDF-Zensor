package de.uni_hannover.se.pdfzensor.censor;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.censor.utils.Annotations;
import de.uni_hannover.se.pdfzensor.censor.utils.MetadataRemover;
import de.uni_hannover.se.pdfzensor.censor.utils.PDFUtils;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
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
	/** The maximum horizontal gap between two glyphs to still combine their bounds. */
	private static final float MAX_BRIDGED_WIDTH = 10.8f;
	
	/** The maximum vertical gap between two glyphs to still combine their bounds. */
	private static final float MAX_BRIDGED_HEIGHT = 10.8f;
	
	/**
	 * The maximum difference in the x (or y) coordinate to still consider glyphs to be on the same line (or same
	 * column). Exists to keep the censor bars (to prevent the combination of bounding boxes into one single box
	 * censoring the whole page). Slightly increasing this value will also combine sub- and superscript text into the
	 * censor bar.
	 */
	private static final float MAX_COORDINATE_DIFF = 1f;
	
	private static final Logger LOGGER = Logging.getLogger();
	
	/** The list of bounds-color pairs which will be censored. */
	private List<ImmutablePair<Rectangle2D, Color>> boundingBoxes;
	
	private Predicate<Rectangle2D> removePredicate;
	
	private Annotations annotations = new Annotations();
	
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
		annotations.cachePage(page);
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
			page.getAnnotations().clear();
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
		MetadataRemover.censorMetadata(doc);
	}
	
	/**
	 * @param pos the TextPosition (represents string + its character's screen positions) to check
	 * @return true if <code>pos</code> should be censored, false otherwise
	 */
	@Override
	public boolean shouldCensorText(TextPosition pos) {
		var censoredPair = getTextPositionInfo(pos).filter(p -> removePredicate.test(p.getLeft()));
		censoredPair.ifPresent(this::addOrExtendBoundingBoxes);
		return censoredPair.isPresent();
	}
	
	/**
	 * Either adds the given <code>pair</code> to the <code>boundingBoxes</code> list or extends the last element of the
	 * list to also cover the bounds of the given pair (if the bounds are within the margin and the color is the same).
	 * <br>
	 * Whether or not the previous bounds will be extended by the bounds of the given pair depends on the result of
	 * {@link #areAdjacent(Rectangle2D, Rectangle2D)} when called with the two rectangles.
	 *
	 * @param pair The pair to include in the <code>boundingBoxes</code> list.
	 */
	private void addOrExtendBoundingBoxes(@NotNull final ImmutablePair<Rectangle2D, Color> pair) {
		if (!boundingBoxes.isEmpty()) {
			var bb = pair.getLeft();
			var last = boundingBoxes.get(boundingBoxes.size() - 1);
			var l = last.getLeft();
			if (last.getRight().equals(pair.getRight()) && areAdjacent(l, bb)) {
				boundingBoxes.remove(last);
				bb.setRect(l.createUnion(bb));
			}
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
		var result = Optional.<ImmutablePair<Rectangle2D, Color>>empty();
		try {
			var font = pos.getFont();
			var s = new StringBuilder();
			for (var i : pos.getCharacterCodes())
				s.append(font.toUnicode(i));
			
			if (StringUtils.isNotBlank(s)) {
				var transformed = PDFUtils.transformTextPosition(pos);
				var color = Color.DARK_GRAY;
				
				if (annotations.isLinked(transformed))
					color = Color.BLUE;
				
				result = Optional.of(new ImmutablePair<>(transformed, color));
			}
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "There was an error handling the font.", e);
		}
		return result;
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
				pageContentStream.setNonStrokingColor(pair.getRight());
				var r = pair.getLeft();
				pageContentStream.addRect((float) r.getX(), (float) r.getY(), (float) r.getWidth(),
										  (float) r.getHeight());
				pageContentStream.fill();
			}
		}
	}
	
	/**
	 * Uses {@link #MAX_BRIDGED_WIDTH}, {@link #MAX_BRIDGED_HEIGHT} and {@link #MAX_COORDINATE_DIFF} to determine if two
	 * given rectangles are horizontally or vertically adjacent to each other.
	 * <br>
	 * The rectangles are considered horizontally adjacent if their center y-coordinates are within the range of {@link
	 * #MAX_COORDINATE_DIFF} and the horizontal gap between them is smaller than or equal to {@link
	 * #MAX_BRIDGED_WIDTH}.
	 * <br>
	 * Similarly, they are considered vertically adjacent if their center x-coordinates are within the range of {@link
	 * #MAX_COORDINATE_DIFF} and the vertical gap between them is smaller than or equal to {@link #MAX_BRIDGED_HEIGHT}.
	 *
	 * @param r1 the first {@link Rectangle2D}.
	 * @param r2 the second {@link Rectangle2D}.
	 * @return true if the rectangles are horizontally or vertically adjacent to each other, false otherwise.
	 */
	private static boolean areAdjacent(@NotNull Rectangle2D r1, @NotNull Rectangle2D r2) {
		Validate.isTrue(ObjectUtils.allNotNull(r1, r2), "The rectangles may not be null.");
		final var xDiff = Math.abs(r1.getCenterX() - r2.getCenterX());
		final var yDiff = Math.abs(r1.getCenterY() - r2.getCenterY());
		final var horizontalGap = xDiff - (r1.getWidth() + r2.getWidth()) / 2f;
		final var verticalGap = yDiff - (r1.getHeight() + r2.getHeight()) / 2f;
		return ((yDiff <= MAX_COORDINATE_DIFF && horizontalGap <= MAX_BRIDGED_WIDTH) ||
				(xDiff <= MAX_COORDINATE_DIFF && verticalGap <= MAX_BRIDGED_HEIGHT));
	}
}
