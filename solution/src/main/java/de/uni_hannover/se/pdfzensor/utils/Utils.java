package de.uni_hannover.se.pdfzensor.utils;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 * Utils should be a general utility-class for methods that occur that are general purpose and may generally be used by
 * any class.
 */
public final class Utils {
	/** The regular expression 3 digit hexadecimal color-codes should match */
	private static final String SIX_DIGIT_HEX_PATTERN = "(?i)^(0x|#)[0-9a-f]{6}$";
	
	/** The regular expression 6 digit hexadecimal color-codes should match */
	private static final String THREE_DIGIT_HEX_PATTERN = "(?i)^(0x|#)[0-9a-f]{3}$";
	
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
	 * Clamps the value between min and max.
	 *
	 * @param value The value to be clamped.
	 * @param min   The lower bound of the result (inclusive).
	 * @param max   The upper bound of the result (inclusive).
	 * @param <T>   The type of the value.
	 * @return The value fitted to the given bounds.
	 */
	@NotNull
	static <T extends Comparable<T>> T clamp(@NotNull T value, @NotNull T min, @NotNull T max) {
		var result = Objects.requireNonNull(value);
		Objects.requireNonNull(min);
		Objects.requireNonNull(max);
		Validate.isTrue(min.compareTo(max) <= 0);
		if (value.compareTo(min) < 0) result = min;
		else if (value.compareTo(max) > 0) result = max;
		return result;
	}
	
	/**
	 * Translates the provided hexadecimal color-code into the corresponding color. If the color-code is null, null will
	 * be returned. The color code should either be 3 or 6 hexadecimal digits (0-f) prepended with # or 0x. Cases are
	 * ignored (0Xabcdef is identical to 0xABCDEF). E.g. #0bc and #00bbcc are identical.
	 *
	 * @param hexCode A string containing a hexadecimal color code. May be null.
	 * @return The {@link Color} corresponding to the hexadecimal color code or null, if the given string was null.
	 */
	@Contract("null -> null")
	@Nullable
	public static Color getColorOrNull(@Nullable String hexCode) {
		if (hexCode == null) return null;
		if (hexCode.matches(THREE_DIGIT_HEX_PATTERN)) //replace 0X and 0x by # and than double each hex-digit
			hexCode = hexCode.replaceFirst("(?i)0x", "#").replaceAll("(?i)[0-9A-F]", "$0$0");
		Validate.matchesPattern(hexCode, SIX_DIGIT_HEX_PATTERN, hexCode + " is not a valid hex color code.");
		return Color.decode(hexCode);
	}
	
	
	/**
	 * Returns the corresponding 6 digit color code for the provided color. That is the RGB channels written in
	 * hexadecimal successively (in that order). The hex-string is than prepended with a #-symbol.<br> Example: black
	 * &rarr; #000000; white &rarr; #FFFFFF; red &rarr; #FF0000<br>
	 * <br>
	 * A null-value will be converted to "null".
	 *
	 * @param color The color to convert into a hexadecimal color code.
	 * @return The hexadecimal color code representing the given color.
	 */
	@NotNull
	@Contract("_ -> !null")
	public static String colorToString(@Nullable Color color) {
		return Optional.ofNullable(color)
					   .map(c -> String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()))
					   .orElse("null");
	}
}
