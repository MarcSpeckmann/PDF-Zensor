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
public enum MarkCriterion {
	/** INTERSECT is used when checking if a rectangle intersects with another rectangle. */
	INTERSECT(Rectangle2D::intersects),
	/** CONTAIN is used when checking if rectangle entirely contains one another. */
	CONTAIN(Rectangle2D::contains),
	/** CONTAIN_90 is used to checking if the rectangle contains 90% of another. */
	CONTAIN_90(MarkCriterion::contains90Percent);
	
	/** The wanted predicate. */
	private final BiPredicate<Rectangle2D, @NotNull Rectangle2D> predicate;
	
	/**
	 * Constructs the wanted Criteria depending on the input function
	 *
	 * @param predicate a predicate that can be one of {@link Rectangle2D#intersects} or {@link Rectangle2D#contains}
	 */
	@Contract(pure = true)
	MarkCriterion(BiPredicate<Rectangle2D, @NotNull Rectangle2D> predicate) {
		this.predicate = predicate;
	}
	
	/**
	 * A predicate that returns true if a given rectangle fulfills this {@link MarkCriterion}'s condition and false
	 * otherwise.
	 *
	 * @param other The rectangle to evaluate the predicate on.
	 * @return A predicate with the given rectangle and this {@link MarkCriterion}'s condition.
	 */
	@NotNull
	@Contract(pure = true)
	Predicate<@NotNull Rectangle2D> getPredicate(@NotNull Rectangle2D other) {
		return rect -> predicate.test(rect, other);
	}
	
	private static boolean contains90Percent(Rectangle2D r1, Rectangle2D r2) {
		return r1.intersects(r2) && areaOfRect(r1.createIntersection(r2)) >= areaOfRect(r2)*0.9;
	}
	
	private static double areaOfRect(Rectangle2D rect) {
		return rect.getWidth()*rect.getHeight();
	}
}