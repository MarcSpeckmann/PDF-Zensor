package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.uni_hannover.se.pdfzensor.censor.utils.PDFUtils.pdRectToRect2D;
import static org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT;

/**
 * This Annotations class caches links and highlighted annotations. It also allows to test annotations for being links
 * or highlights.
 */
@SuppressWarnings("WeakerAccess") // this class is member of the public API
public final class Annotations {
	/** A {@link Logger}-instance that should be used by this class' member methods to log their state and errors. */
	private static final Logger LOGGER = Logging.getLogger();
	
	/**
	 * Contains cached highlights after caching a PDF page.
	 *
	 * @see #cachePage(PDPage)
	 */
	@NotNull
	public List<Rectangle2D> highlights;
	
	/**
	 * Contains cached links after caching a PDF page.
	 *
	 * @see #cachePage(PDPage)
	 */
	@NotNull
	private List<Rectangle2D> links;
	
	/** Initializes a new Annotations-instance and creates new, empty {@link #highlights} and {@link #links} lists. */
	public Annotations() {
		highlights = List.of();
		links = List.of();
	}
	
	/**
	 * Translates the given PDF annotation into a bounding box {@link Rectangle2D}.
	 *
	 * @param annotation the annotation to be bounded by a rectangle
	 * @return bounding box rectangle
	 */
	@NotNull
	private static Rectangle2D getAnnotationRect(@NotNull PDAnnotation annotation) {
		var rectangle = pdRectToRect2D(annotation.getRectangle());
		if (annotation instanceof PDAnnotationTextMarkup) {
			final var quads = ((PDAnnotationTextMarkup) annotation).getQuadPoints();
			var path = new Path2D.Float();
			int i = 0;
			while (i < quads.length) {
				path.moveTo(quads[i++], quads[i++]);
				path.lineTo(quads[i++], quads[i++]);
			}
			rectangle = path.getBounds2D();
		}
		return rectangle;
	}
	
	/**
	 * Checks if given annotation is highlighted.
	 *
	 * @param annotation the annotation to be checked
	 * @return true if the given annotation is highlighted, false otherwise
	 */
	@Contract("null -> false")
	private static boolean isHighlightAnnotation(@Nullable PDAnnotation annotation) {
		if (!(annotation instanceof PDAnnotationTextMarkup)) return false;
		PDAnnotationTextMarkup tm = (PDAnnotationTextMarkup) annotation;
		String subtype = tm.getSubtype();
		return SUB_TYPE_HIGHLIGHT.equals(subtype);
	}
	
	/**
	 * Caches links and highlights of the current PDF page by calling {@link #cacheLinks(PDPage)} and {@link
	 * #cacheHighlights(PDPage)}.
	 *
	 * @param page the current PDF page being worked on
	 * @see #cacheLinks(PDPage)
	 * @see #cacheHighlights(PDPage)
	 */
	public void cachePage(@NotNull PDPage page) {
		LOGGER.log(Level.DEBUG, "Caching annotations...");
		cacheLinks(page);
		cacheHighlights(page);
	}
	
	/**
	 * Caches links of the current PDF page. If there is an error while creating the annotation list {@link #links} the
	 * list will just be cleared.
	 *
	 * @param page the current PDF page being worked on
	 */
	private void cacheLinks(@NotNull PDPage page) {
		Objects.requireNonNull(page);
		try {
			LOGGER.log(Level.DEBUG, "Caching links...");
			links = page.getAnnotations(PDAnnotationLink.class::isInstance).stream().map(Annotations::getAnnotationRect)
						.collect(Collectors.toUnmodifiableList());
			LOGGER.log(Level.DEBUG, "Cached {} links", links.size());
		} catch (IOException e) {
			links = List.of();
			LOGGER.log(Level.ERROR, "Failed to cache links", e);
		}
	}
	
	/**
	 * Caches highlighted annotations of the current PDF page. If there is an error while creating the annotation list
	 * in the stream {@link #highlights} list will just be cleared.
	 *
	 * @param page the current PDF page being worked on
	 */
	private void cacheHighlights(@NotNull PDPage page) {
		Objects.requireNonNull(page);
		try {
			LOGGER.log(Level.DEBUG, "Caching highlight-annotations...");
			highlights = page.getAnnotations(Annotations::isHighlightAnnotation).stream()
							 .map(Annotations::getAnnotationRect).collect(Collectors.toUnmodifiableList());
			LOGGER.log(Level.DEBUG, "Cached {} highlight-annotations", highlights.size());
		} catch (IOException e) {
			highlights = List.of();
			LOGGER.log(Level.ERROR, "Failed to cache highlight-annotations", e);
		}
	}
	
	/**
	 * Checks if the given rectangle bounds intersect a highlight from the {@link #highlights} list.
	 *
	 * @param rect the rectangle to be checked
	 * @return true if the given rect intersects at least one rect of {@link #highlights}, false otherwise
	 */
	public boolean isMarked(@NotNull Rectangle2D rect) {
		return isMarked(rect, MarkCriterion.CONTAIN_90);
	}
	
	/**
	 * Checks if the given rectangle bounds either intersect or are contained in a highlight from the {@link
	 * #highlights} list, depending on the given <code>criteria</code>.
	 *
	 * @param rect     the rectangle to be checked
	 * @param criteria either {@link MarkCriterion#CONTAIN} or {@link MarkCriterion#INTERSECT}
	 * @return true if at least one rect of {@link #highlights} matches the criteria with the given rect, false
	 * otherwise
	 */
	public boolean isMarked(@NotNull Rectangle2D rect, @NotNull MarkCriterion criteria) {
		Objects.requireNonNull(rect);
		Objects.requireNonNull(criteria);
		Predicate<Rectangle2D> predicate = criteria.getPredicate(rect);
		return highlights.stream().anyMatch(predicate);
	}
	
	/**
	 * Checks if the given rectangle bounds intersect a link from the {@link #links} list.
	 *
	 * @param rect the rectangle to be checked
	 * @return true if the given rect intersects at least one rect of {@link #links}, false otherwise
	 */
	public boolean isLinked(@NotNull Rectangle2D rect) {
		return isLinked(rect, MarkCriterion.INTERSECT);
	}
	
	/**
	 * Checks if the given rectangle bounds either intersect or are contained in a link from the {@link #links} list,
	 * depending on the given <code>criteria</code>.
	 *
	 * @param rect     the rectangle to be checked
	 * @param criteria either {@link MarkCriterion#CONTAIN} or {@link MarkCriterion#INTERSECT}
	 * @return true if at least one rect of {@link #links} matches the criteria with the given rect, false otherwise
	 */
	public boolean isLinked(@NotNull Rectangle2D rect, @NotNull MarkCriterion criteria) {
		Objects.requireNonNull(rect);
		Objects.requireNonNull(criteria);
		Predicate<Rectangle2D> predicate = criteria.getPredicate(rect);
		return links.stream().anyMatch(predicate);
	}
}