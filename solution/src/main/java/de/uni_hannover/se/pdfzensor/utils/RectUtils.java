package de.uni_hannover.se.pdfzensor.utils;

import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.Objects;

import static java.lang.Math.*;

/**
 * RectUtils is a utility-class for all rectangle-related helper-methods. These methods were split from the
 * general-purpose utility class as there are quite a few rectangle-related ones such that it seems useful to do so.
 */
@SuppressWarnings("WeakerAccess")
public final class RectUtils {
	
	/**
	 * This constructor should not be called as no instance of {@link RectUtils} shall be created.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	@Contract(value = " -> fail", pure = true)
	private RectUtils() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Retrieves the rectangle spanned between the first and second rectangle. If the rectangles overlap their span is
	 * <code>(x:&nbsp;0,&nbsp;y:&nbsp;0,&nbsp;w:&nbsp;0,&nbsp;h:&nbsp;0)</code>. Otherwise the span is the gap between
	 * these two rectangles.
	 *
	 * @param r1 the first rectangle. Not <code>null</code>.
	 * @param r2 the second rectangle. Not <code>null</code>.
	 * @return the rectangle spanned by the first and second rectangle.
	 * @throws NullPointerException if r1 or r2 are <code>null</code>.
	 */
	@NotNull
	public static Rectangle2D getRectBetween(@NotNull final Rectangle2D r1, @NotNull final Rectangle2D r2) {
		var ret = new Rectangle2D.Double();
		if (!r1.intersects(r2)) {
			if (areHorizontallyAligned(r1, r2)) {
				var minX = min(r1.getMaxX(), r2.getMaxX());
				var minY = min(r1.getMinY(), r2.getMinY());
				var maxX = max(r1.getMinX(), r2.getMinX());
				var maxY = max(r1.getMaxY(), r2.getMaxY());
				ret.setFrameFromDiagonal(minX, minY, maxX, maxY);
			} else {
				var minX = min(r1.getMinX(), r2.getMinX());
				var minY = min(r1.getMaxY(), r2.getMaxY());
				var maxX = max(r1.getMaxX(), r2.getMaxX());
				var maxY = max(r1.getMinY(), r2.getMinY());
				ret.setFrameFromDiagonal(minX, minY, maxX, maxY);
			}
		}
		return ret;
	}
	
	/**
	 * Checks if the two provided rectangles are horizontally aligned. By horizontally aligned it is meant that their
	 * minimum and maximum extends along the y-axis overlap.
	 *
	 * @param r1 the first rectangle. Not <code>null</code>.
	 * @param r2 the second rectangle. Not <code>null</code>.
	 * @return true if the rectangles are horizontally aligned.
	 * @throws NullPointerException if r1 or r2 are <code>null</code>.
	 */
	public static boolean areHorizontallyAligned(@NotNull final Rectangle2D r1, @NotNull final Rectangle2D r2) {
		Objects.requireNonNull(r1);
		Objects.requireNonNull(r2);
		var range1 = Range.between(r1.getMinY(), r1.getMaxY());
		var range2 = Range.between(r2.getMinY(), r2.getMaxY());
		return range1.isOverlappedBy(range2);
	}
	
}
