package de.uni_hannover.se.pdfzensor.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * PDFUtils is a simple utility-class that provides only the {@link #fitToArray(Object[], int)} method to the outside.
 * This class contains utility methods which can be useful for other classes.
 */
public final class PDFUtils {
	/**
	 * This constructor should not be called as no instance of {@link PDFUtils} shall be created.
	 *
	 * @throws UnsupportedOperationException when being called
	 */
	@Contract(value = " -> fail", pure = true)
	private PDFUtils() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Make an index which may lead to an ArrayIndexOutOfBoundsException fit the bounds of the given array. Should the
	 * given index not fit then either an index to the first or the last element of the array will be returned.
	 *
	 * @param array The array to which the index should be fitted.
	 * @param index The index which may not fit the bounds of the array.
	 * @param <T>   The type of the array.
	 * @return An index which is in the given array's bounds.
	 */
	public static <T> int fitToArray(@NotNull T[] array, int index) {
		//TODO: Handle a empty array
		Objects.requireNonNull(array);
		Validate.notEmpty(array);
		return clamp(index, 0, array.length);
	}
	
	/**
	 * @param value The desired value.
	 * @param min   The smallest allowed value.
	 * @param max   The largest allowed value.
	 * @param <T>   The type of the value.
	 * @return The value fitted to the given bounds.
	 */
	@NotNull
	private static <T extends Comparable<T>> T clamp(@NotNull T value, @NotNull T min, @NotNull T max) {
		Objects.requireNonNull(value);
		Objects.requireNonNull(min);
		Objects.requireNonNull(max);
		if (value.compareTo(min) < 0) return min;
		if (value.compareTo(max) > 0) return max;
		return value;
	}
}
