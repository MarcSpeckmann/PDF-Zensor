package de.uni_hannover.se.pdfzensor.utils;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Utils is a simple utility-class that provides only the {@link #fitToArray(Object[], int)} method to the outside. This
 * class contains utility methods which can be useful for other classes.
 */
public final class Utils {
	/**
	 * This constructor should not be called as no instance of {@link Utils} shall be created.
	 *
	 * @throws UnsupportedOperationException when being called
	 */
	@Contract(value = " -> fail", pure = true)
	private Utils() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Clamps an index to the array bounds if possible (length of the array is greater than zero). Should the given
	 * index not fit then either an index to the first or the last element of the array will be returned.
	 *
	 * @param array The array to which the index should be fitted.
	 * @param index The index which may not fit the bounds of the array.
	 * @param <T>   The type of the array.
	 * @return An index which is in the given array's bounds.
	 */
	public static <T> int fitToArray(@NotNull T[] array, int index) {
		Validate.notEmpty(array);
		return clamp(index, 0, array.length - 1);
	}
	
	/**
	 * Clamps the value between min and max
	 *
	 * @param value The value to be clammed
	 * @param min   The lower bound of the result (inclusive).
	 * @param max   The upper bound of the result (inclusive)
	 * @param <T>   The type of the value.
	 * @return The value fitted to the given bounds.
	 */
	@NotNull
	static <T extends Comparable<T>> T clamp(@NotNull T value, @NotNull T min, @NotNull T max) {
		Objects.requireNonNull(value);
		Objects.requireNonNull(min);
		Objects.requireNonNull(max);
		Validate.isTrue(min.compareTo(max) <= 0);
		if (value.compareTo(min) < 0) return min;
		if (value.compareTo(max) > 0) return max;
		return value;
	}
}
