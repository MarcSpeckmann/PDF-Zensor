package de.uni_hannover.se.pdfzensor.censor.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.nio.file.Path;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.lang.Math.max;

/**
 * Criteria which can be chosen to check if an object is marked.
 */
public enum MarkCriterion {
	/** INTERSECT is used when checking if a rectangle intersects with another rectangle. */
	INTERSECT(Area::intersects),
	/** CONTAIN is used when checking if rectangle entirely contains one another. */
	CONTAIN(Area::contains),
	/** CONTAIN_70 is used to checking if the rectangle contains 70% of another. */
	CONTAIN_70(MarkCriterion::contains70Percent);
	
	/** The wanted predicate. */
	private final BiPredicate<Area, @NotNull Rectangle2D> predicate;
	
	/**
	 * Constructs the wanted Criteria depending on the input function
	 *
	 * @param predicate a predicate that can be one of {@link Rectangle2D#intersects} or {@link Rectangle2D#contains}
	 */
	@Contract(pure = true)
	MarkCriterion(BiPredicate<Area, @NotNull Rectangle2D> predicate) {
		this.predicate = predicate;
	}
	
	/**
	 * Returns true iff r1 intersects with r2 and if that intersection is at least 70% of r2's area.
	 *
	 * @param r1 the first rectangle.
	 * @param r2 the second rectangle.
	 * @return true iff r1 intersects with r2 and if that intersection is at least 70% of r2's area.
	 */
	private static boolean contains70Percent(@NotNull Area r1, Rectangle2D r2) {
		//r1.intersects(r2) && areaOfRect(r1.(r2)) >= areaOfRect(r2) * 0.7;
		var intersect = new Area(r2);
		intersect.intersect(r1);
		return areaOfRect(intersect.getBounds2D()) >= 0.7*areaOfRect(r2);
	}
	
	/**
	 * Calculates the area of the rectangle.
	 *
	 * @param rect the rectangle of which to calculate the area.
	 * @return the rectangle's area.
	 */
	private static double areaOfRect(@NotNull Rectangle2D rect) {
		return max(0, rect.getWidth()) * max(0, rect.getHeight());
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
	Predicate<@NotNull Area> getPredicate(@NotNull Rectangle2D other) {
		return rect -> predicate.test(rect, other);
	}
}