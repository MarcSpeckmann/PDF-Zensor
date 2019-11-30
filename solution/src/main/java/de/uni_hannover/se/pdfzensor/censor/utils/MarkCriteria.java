package de.uni_hannover.se.pdfzensor.censor.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Criteria which can be chosen to check if an object is marked
 * <ul>
 * <li>{@link #INTERSECT}</li>
 * <li>{@link #CONTAIN}</li>
 * </ul>
 */
enum MarkCriteria {

	/**
	 * INTERSECT is used when checking if rectangles intersect
	 */
	INTERSECT(Rectangle2D::intersects),
	/**
	 * CONTAIN INTERSECT is used when checking if rectangle entirely contains one another
	 */
	CONTAIN(Rectangle2D::contains);

	/**
	 * the wanted Predicate
	 */
	private final BiPredicate<Rectangle2D, Rectangle2D> predicate;

	/**
	 *  Constructs the wanted Criteria depending on the input function
	 * @param predicate a predicate that can be one of {@link Rectangle2D#intersects} or {@link Rectangle2D#contains}
	 */
	@Contract(pure = true)
	MarkCriteria(BiPredicate<Rectangle2D, Rectangle2D> predicate) {
		this.predicate = predicate;
	}
	
	@NotNull
	@Contract(pure = true)
	Predicate<Rectangle2D> getPredicate(Rectangle2D other) {
		return rect -> predicate.test(rect, other);
	}
}