package de.uni_hannover.se.pdfzensor.censor;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.censor.utils.Annotations;
import de.uni_hannover.se.pdfzensor.censor.utils.MetadataRemover;
import de.uni_hannover.se.pdfzensor.censor.utils.PDFUtils;
import de.uni_hannover.se.pdfzensor.config.Expression;
import de.uni_hannover.se.pdfzensor.config.Mode;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.images.ImageReplacer;
import de.uni_hannover.se.pdfzensor.processor.PDFHandler;
import de.uni_hannover.se.pdfzensor.text.Tokenizer;
import de.uni_hannover.se.pdfzensor.utils.RectUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;

/**
 * Runs functions to place censoring rectangles (or censoring graphics) and removes elements of the PDF depending on the
 * annotations and the mode
 */
public final class PDFCensor implements PDFHandler {
	/**
	 * The permitted deviation of the width/height of two rectangles from their union's width/height to still consider
	 * that union an extension of those rectangles.
	 * <br>
	 * Should be a number between 0 and 1 (values greater than or equal to 1 will always allow the rectangles to be
	 * unified, values smaller than or equal to 0 will never allow them to be unified). The higher the number the
	 * greater the tolerance, meaning two rectangles are sooner considered an horizontal/vertical extension of each
	 * other if this value is greater.
	 *
	 * @see #getExtended(Rectangle2D, Rectangle2D)
	 */
	private static final float DEVIATION_TOLERANCE = .2f;
	
	/**
	 * A factor for determining whether or not a gap should be bridged.
	 * <br>
	 * Increasing this value will increase the gap tolerance when combining bounds. Meaning greater values will allow
	 * greater gaps between two bounds and still combine them.
	 *
	 * @see #getExtended(Rectangle2D, Rectangle2D)
	 */
	private static final float MAX_GAP = 1.5f;
	
	/** Stores the width of a thin space, which usually is 1/5 em or 1/6 em. */
	private static final double THIN_SPACE_WIDTH = 1 / 6d;
	
	/** A {@link Logger}-instance that should be used by this class' member methods to log their state and errors. */
	private static final Logger LOGGER = Logging.getLogger();
	
	/**
	 * The tokenizer calls {@link #onTokenEncountered(String, List, Expression)} when a token was found and thus enables
	 * the censor to draw the bounds of characters belonging to this token in the color providing to the token ({@link
	 * Expression}. The payload of each character is the {@link Rectangle2D} representing its bounds on the page.
	 */
	private final Tokenizer<Expression, Rectangle2D> tokenizer;
	private final ImageReplacer imageReplacer = new ImageReplacer();
	/** Stores the settings provided in the constructor. */
	private final Settings settings;
	/** The list of bounds-color pairs which will be censored. */
	private List<ImmutablePair<Rectangle2D, Color>> boundingBoxes;
	/** The list of picture bounding boxes that should be censored.. */
	private List<Rectangle2D> pictureBoundingBoxes;
	/** The predicate to use when checking bounds of {@link TextPosition}s. */
	private Predicate<Rectangle2D> removePredicate;
	/** A new annotations instance in this {@link PDFCensor}-instance. */
	private Annotations annotations = new Annotations();
	/**
	 * Stores the bounds of the last glyph to allow for detection of space-characters. May be null if there was no
	 * previous glyph after which a space could have followed.
	 */
	private Rectangle2D lastGlyph = null;
	
	/**
	 * @param settings Settings that contain information about the mode and expressions
	 */
	public PDFCensor(@NotNull Settings settings) {
		Objects.requireNonNull(settings);
		this.settings = settings;
		tokenizer = new Tokenizer<>(settings.getExpressions());
		tokenizer.setHandler(this::onTokenEncountered);
		this.removePredicate = rect -> true;
		// to censor only segments marked beforehand with a different software
		if (Mode.MARKED.equals(settings.getMode()))
			removePredicate = removePredicate.and(annotations::isMarked);
			// to censor everything but segments marked beforehand with a different software
		else if (Mode.UNMARKED.equals(settings.getMode()))
			removePredicate = removePredicate.and(Predicate.not(annotations::isMarked));
	}
	
	/**
	 * Tests if a union created out of the two given rectangles is a horizontal or vertical extension out of those
	 * rectangles. Should that be the case the union is returned, however, should the union not be a horizontal or
	 * vertical extension, null will be returned.
	 * <br>
	 * Two rectangles are considered a horizontal extension of each other if both of their heights are deviating no more
	 * than {@link #DEVIATION_TOLERANCE} % from the height of their union. Likewise, two rectangles are considered a
	 * vertical extension of each other if both of their widths are deviating no more than {@link #DEVIATION_TOLERANCE}
	 * % from the width of their union.
	 * <br>
	 * Note that deviating over the width/height of the union is impossible because a union is always at least the size
	 * of each of the rectangles out of which it was constructed.
	 *
	 * @param r1 the first {@link Rectangle2D}.
	 * @param r2 the second {@link Rectangle2D}.
	 * @return A {@link Rectangle2D} which is the union of both given rectangles or null.
	 * @see Rectangle2D#createUnion(Rectangle2D)
	 */
	@Nullable
	private static Rectangle2D getExtended(@NotNull Rectangle2D r1, @NotNull Rectangle2D r2) {
		Validate.isTrue(ObjectUtils.allNotNull(r1, r2), "The rectangles may not be null.");
		final var comb = r1.createUnion(r2);
		final var percentage = 1 - DEVIATION_TOLERANCE;
		final var widthTolerance = comb.getWidth() * percentage;
		final var heightTolerance = comb.getHeight() * percentage;
		final var sameColumn = r1.getWidth() > widthTolerance && r2.getWidth() > widthTolerance;
		final var sameLine = r1.getHeight() > heightTolerance && r2.getHeight() > heightTolerance;
		
		final var gap = RectUtils.getRectBetween(r1, r2);
		final var tolerance = new Point2D.Double(comb.getHeight() * MAX_GAP, comb.getWidth() * MAX_GAP);
		
		return ((sameLine && gap.getWidth() < tolerance.x) ||
				(sameColumn && gap.getHeight() < tolerance.y)) ? comb : null;
	}
	
	/**
	 * Calculates the rectangle between two given rectangles and returns an {@link Optional} containing that rectangle
	 * if it is considered a space-character or an empty optional if it is not.
	 * <br>
	 * For a rectangle to be considered a space-character its respective height (for horizontal fonts) or width (for
	 * vertical fonts) must be in the range of {@link #THIN_SPACE_WIDTH} to {@link #MAX_GAP}. Furthermore, the height of
	 * a horizontal or width of a vertical space-character may be no larger than the height or width of their enclosing
	 * rectangles.
	 *
	 * @param r1   The first of the two enclosing rectangles. May be <code>null</code>.
	 * @param r2   The second of the two enclosing rectangles. May be <code>null</code>.
	 * @param font The font of which the space would be a part of.
	 * @return The {@link Optional} containing the rectangle between r1 and r2 if it is a space-character. If it is not
	 * or either r1 or r2 were <code>null</code> then an empty {@link Optional} is returned instead.
	 */
	private static Optional<Rectangle2D> getBlankBetween(Rectangle2D r1, Rectangle2D r2, PDFont font) {
		if (!ObjectUtils.allNotNull(r1, r2))
			return Optional.empty();
		var gap = RectUtils.getRectBetween(r1, r2);
		var tolerance = 1 + DEVIATION_TOLERANCE;
		// size of the gap in Em (relative to the fonts height/width depending on the font's alignment)
		double size = gap.getWidth() / ((r1.getHeight() + r2.getHeight()) / 2);
		boolean ratioFits = gap.getHeight() < tolerance * Math.max(r1.getHeight(), r2.getHeight());
		if (font.isVertical()) {
			size = gap.getHeight() / ((r1.getWidth() + r2.getWidth()) / 2);
			ratioFits = gap.getWidth() < tolerance * Math.max(r1.getWidth(), r2.getWidth());
		}
		final var isSpace = ratioFits && Range.between(THIN_SPACE_WIDTH, (double) MAX_GAP).contains(size);
		return Optional.of(gap).filter(b -> isSpace);
	}
	
	/**
	 * A callback for when work on a new document starts.
	 *
	 * @param doc the document which is being worked on
	 */
	@Override
	public void beginDocument(PDDocument doc) {
		boundingBoxes = new ArrayList<>();
		pictureBoundingBoxes = new ArrayList<>();
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
		Objects.requireNonNull(pictureBoundingBoxes).clear();
		annotations.cachePage(page);
		try {
			this.pictureBoundingBoxes = imageReplacer.replaceImages(doc, page);
		} catch (IOException e) {
			LOGGER.error(e);
		}
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
			tokenizer.flush();
			
			drawCensorBars(doc, page);
			drawCensorImages(doc, page);
			page.setAnnotations(null);
		} catch (IOException e) {
			LOGGER.error("There was an error writing the page contents of page {}.", pageNum, e);
		}
	}
	
	/**
	 * A callback for when work on a document has ended.
	 *
	 * @param doc the document which is being worked on
	 */
	@Override
	public void endDocument(PDDocument doc) {
		try {
			tokenizer.close();
		} catch (IOException e) {
			LOGGER.warn(e);
		}
		boundingBoxes = null;
		pictureBoundingBoxes = null;
		MetadataRemover.censorMetadata(doc);
		ImageReplacer.removeImageData(doc);
		doc.getDocumentCatalog().setDocumentOutline(null);
		doc.getDocumentCatalog().setPageLabels(null);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean shouldCensorText(PDPage page, TextPosition pos) {
		var bounds = getTextPositionInfo(page, pos);
		if (bounds.isEmpty()) {
			return true;
		}
		bounds = bounds.filter(p -> removePredicate.test(p));
		bounds.ifPresentOrElse(b -> {
			if (settings.distinguishLinks() && annotations.isLinked(b)) {
				tokenizer.tryFlush();
				addOrExtendBoundingBoxes(b, settings.getLinkColor());
			} else {
				var space = getBlankBetween(lastGlyph, b, pos.getFont());
				lastGlyph = b;
				try {
					if (space.isPresent())
						tokenizer.input(" ", List.of(space.get()));
					tokenizer.input(pos.getUnicode(), Collections.nCopies(pos.getUnicode().length(), b));
				} catch (IOException e) {
					LOGGER.warn(e);
				}
			}
		}, tokenizer::tryFlush);
		return bounds.isPresent();
	}
	
	/**
	 * <b><i>Do not call this method manually! It is meant to be a callback only.</i></b><br>
	 * This method is a callback for the tokenizer. It is called when a token was matched (or no match could be found
	 * for a character). In our case we want to handle this here by filling the bounding-box of the character with the
	 * respective color (as defined in {@link Expression#getColor()}).
	 *
	 * @param value   The entire value of the token. Not <code>null</code>. (e.g. {@code 123} for {@code [0-9]+})
	 * @param payload A list containing the payload for each character in the order they occur in the value.
	 * @param token   The token that got matched. May be <code>null</code> if no match was found.
	 * @see #tokenizer
	 * @see Tokenizer#setHandler(TriConsumer)
	 */
	private void onTokenEncountered(String value, @NotNull List<Rectangle2D> payload, @Nullable Expression token) {
		Objects.requireNonNull(token);
		//We are only interested in logging non-default token-matches
		if (!".".equals(token.getRegex()))
			LOGGER.debug("Found token [{}]: {}", token, value);
		payload.forEach(rect -> addOrExtendBoundingBoxes(rect, token.getColor()));
	}
	
	/**
	 * The given bounding-box and color will be added to the {@link #boundingBoxes} list or extend the last element of
	 * the list to also cover the bounds of the given pair (if the bounds are an extension of the previous bounds and
	 * the color is the same).
	 * <br>
	 * If {@link Settings#getIntersectImages()} is {@code false} then no bounding-boxes intersecting the bounding-boxes
	 * of censored images will be added to the list or extend the last element of the list. Otherwise the overlapping of
	 * text censor bars and censored images is not considered and they may overlap.
	 * <br>
	 * Whether or not the previous bounds will be extended depends on the result of {@link #getExtended(Rectangle2D,
	 * Rectangle2D)} when called with the two rectangles (and the previously mentioned factors).
	 *
	 * @param bb    The bounding-box that should be added to the list of censored bounding-boxes.
	 * @param color The color in which the provided bounding-box should be censored.
	 * @see #getExtended(Rectangle2D, Rectangle2D)
	 * @see Settings#getIntersectImages()
	 */
	private void addOrExtendBoundingBoxes(@NotNull final Rectangle2D bb, final Color color) {
		if (!boundingBoxes.isEmpty()) {
			final var last = boundingBoxes.get(boundingBoxes.size() - 1);
			final var union = getExtended(last.getLeft(), bb);
			if (last.getRight().equals(color) && union != null) {
				boundingBoxes.remove(last);
				bb.setRect(union);
			}
		}
		boundingBoxes.add(new ImmutablePair<>(bb, color));
	}
	
	/**
	 * Transforms the <code>pos</code> into either a pair of its bounds and the color to censor the glyph with or an
	 * empty optional.
	 *
	 * @param pos The TextPosition to transform into a bounds-color pair.
	 * @return An optional containing either the bounds-color pair or nothing, if an error occurred.
	 */
	private Optional<Rectangle2D> getTextPositionInfo(PDPage page, @NotNull TextPosition pos) {
		var result = Optional.<Rectangle2D>empty();
		try {
			var font = pos.getFont();
			var s = new StringBuilder();
			for (var i : pos.getCharacterCodes())
				s.append(font.toUnicode(i));
			
			if (StringUtils.isNotBlank(s)) {
				var transformed = PDFUtils.transformTextPosition(pos);
				transformed = PDFUtils.mediaBoxCoordToCropBoxCoord(transformed, page);
				result = Optional.of(transformed);
			}
		} catch (IOException e) {
			LOGGER.error("There was an error handling the font.", e);
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
		//Calculate the stencil-area that is the area of the page minus the area of each picture.
		//This stencil will be used later if settings.getIntersectImages() is present to not draw censor-boxes over images
		Area stencil = new Area(PDFUtils.pdRectToRect2D(page.getMediaBox()));
		pictureBoundingBoxes.stream().map(Area::new).forEach(stencil::subtract);
		
		try (var pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true,
															 true)) {
			for (var pair : boundingBoxes) {
				pageContentStream.setNonStrokingColor(pair.getRight());
				var r = new Area(pair.getLeft());
				if (!settings.getIntersectImages())
					r.intersect(stencil);
				PDFUtils.drawArea(pageContentStream, r);
				pageContentStream.fill();
			}
		}
	}
	
	/**
	 * Draws the default image at the bounds stored in {@link #pictureBoundingBoxes}.
	 *
	 * @param doc  the document which is being worked on.
	 * @param page the PDPage (current pdf page) that is being worked on.
	 * @throws IOException If there was an I/O error writing the contents of the page.
	 */
	private void drawCensorImages(PDDocument doc, PDPage page) throws IOException {
		try (var pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true,
															 true)) {
			pageContentStream.setStrokingColor(Color.DARK_GRAY);
			pageContentStream.setLineWidth(2);
			
			for (var rect : this.pictureBoundingBoxes) {
				pageContentStream.addRect((float) rect.getMinX(), (float) rect.getMinY(), (float) rect.getWidth(),
										  (float) rect.getHeight());
				pageContentStream.moveTo((float) rect.getMaxX(), (float) rect.getMaxY());
				pageContentStream.lineTo((float) rect.getMinX(), (float) rect.getMinY());
				pageContentStream.moveTo((float) rect.getMaxX(), (float) rect.getMinY());
				pageContentStream.lineTo((float) rect.getMinX(), (float) rect.getMaxY());
				pageContentStream.stroke();
			}
		}
	}
	
	
}
