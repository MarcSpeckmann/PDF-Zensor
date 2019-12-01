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

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT;

/**
 * This Annotations class caches links and highlighted annotations. It also allows to test
 * annotations for being links or highlights.
 */
public final class Annotations {
	private static final Logger LOGGER = Logging.getLogger();

	@NotNull
	private List<Rectangle2D> highlights;
	@NotNull
	private List<Rectangle2D> links;
	
	public Annotations() {
		LOGGER.log(Level.DEBUG, "Initialized a new Annotations-instance");
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
		return PDFUtils.pdrectToRect2D(annotation.getRectangle());
	}

	/**
	 * Caches links and highlights of the current PDF page by calling {@link #cacheLinks}
	 * and {@link #cacheHighlights}.
	 *
	 * @param page the current PDF page being worked on
	 */
	public void cachePage(@NotNull PDPage page){
		LOGGER.log(Level.DEBUG, "Starting to cache page: {}", page);
		cacheLinks(page);
		cacheHighlights(page);
	}

	/**
	 * Caches links of the current PDF page. If there is an error while creating the annotation list
	 * {@link #links} the list will just be cleared.
	 *
	 * @param page the current PDF page being worked on
	 */
	private void cacheLinks(@NotNull PDPage page){
		Objects.requireNonNull(page);
		try {
			LOGGER.log(Level.DEBUG, "Starting to cache the Links of page: {}", page);
			links = page.getAnnotations(PDAnnotationLink.class::isInstance).stream()
					.map(Annotations::getAnnotationRect)
					.collect(Collectors.toUnmodifiableList());
		} catch (IOException e) {
			links = List.of();
			LOGGER.log(Level.ERROR,
					"Failed to cache the Links of page: {}", page ,e);
		}
	}

	/**
	 * Caches highlighted annotations of the current PDF page. If there is an error while creating the annotation list
	 * in the stream {@link #highlights} list will just be cleared.
	 *
	 * @param page the current PDF page being worked on
	 */
	private void cacheHighlights(@NotNull PDPage page){
		Objects.requireNonNull(page);
		try {
			LOGGER.log(Level.DEBUG, "Starting to cache the Links of page: {}", page);
			highlights = page.getAnnotations(Annotations::isHighlightAnnotation).stream()
					.map(Annotations::getAnnotationRect)
					.collect(Collectors.toUnmodifiableList());
		} catch (IOException e) {
			highlights = List.of();
			LOGGER.log(Level.ERROR,
					"Failed to cache the highlighted annotations of page: {}", page ,e);
		}
	}

	/**
	 * Checks if given annotation bounding box entirely fits into one of the elements(bounding box rectangles)
	 * of {@link #highlights} list.
	 *
	 * @param rect the rectangle to be checked
	 * @return true if the given rect entirely fits into at least one rect of {@link #highlights} otherwise false
	 */
	public boolean isMarked(@NotNull Rectangle2D rect){
		return isMarked(rect, MarkCriterion.CONTAIN);
	}

	/**
	 * Checks if given annotation bounding box either intersects or entirely fits into one of the elements(bounding box
	 * rectangles) of {@link #highlights} list depending on the given {@link MarkCriterion}.
	 *
	 * @param rect the rectangle to be checked
	 * @param criteria either {@link MarkCriterion#CONTAIN} or {@link MarkCriterion#INTERSECT}
	 * @return true if at least one rect of {@link #highlights} matches the criteria with the given
	 * 			rect otherwise false
	 */
	boolean isMarked(@NotNull Rectangle2D rect, @NotNull MarkCriterion criteria){
		Objects.requireNonNull(rect);
		Objects.requireNonNull(criteria);
		Predicate<Rectangle2D> predicate = criteria.getPredicate(rect);
		return highlights.stream().anyMatch(predicate);
	}

	/**
	 * Checks if given annotation bounding box entirely fits into one of the elements(bounding box rectangles)
	 * of {@link #links} list.
	 *
	 * @param rect the rectangle to be checked
	 * @return true if the given rect entirely fits into at least one rect of {@link #links} otherwise false
	 */
	boolean isLinked(@NotNull Rectangle2D rect){
		return isLinked(rect, MarkCriterion.CONTAIN);
	}

	/**
	 * Checks if given annotation bounding box either intersects or entirely fits into one of the elements
	 * (bounding box rectangles) of {@link #links} list depending on the given {@link MarkCriterion}.
	 *
	 * @param rect the rectangle to be checked
	 * @param criteria either {@link MarkCriterion#CONTAIN} or {@link MarkCriterion#INTERSECT}
	 * @return true if at least one rect of {@link #links} matches the criteria with the given
	 * 			rect otherwise false
	 */
	boolean isLinked(@NotNull Rectangle2D rect, @NotNull MarkCriterion criteria){
		Objects.requireNonNull(rect);
		Objects.requireNonNull(criteria);
		Predicate<Rectangle2D> predicate = criteria.getPredicate(rect);
		return links.stream().anyMatch(predicate);
	}

	/**
	 * Checks if given annotation is highlighted.
	 *
	 * @param annotation the annotation to be checked
	 * @return true if the given annotation is highlighted otherwise false
	 */
	@Contract("null -> false")
	private static boolean isHighlightAnnotation(PDAnnotation annotation) {
		if (!(annotation instanceof PDAnnotationTextMarkup))
			return false;
		PDAnnotationTextMarkup tm = (PDAnnotationTextMarkup) annotation;
		String subtype = tm.getSubtype();
		return SUB_TYPE_HIGHLIGHT.equals(subtype);
	}
}
