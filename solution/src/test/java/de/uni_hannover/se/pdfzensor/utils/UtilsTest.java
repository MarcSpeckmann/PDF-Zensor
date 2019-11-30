package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ColorProvider;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.IntStream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.testing.argumentproviders.ColorProvider.COLOR_PREFIXES;
import static de.uni_hannover.se.pdfzensor.utils.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

/** UtilsTest should contain all unit-tests related to {@link Utils}. */
public class UtilsTest {
	
	@Test
	void testGeneral() {
		TestUtility.assertIsUtilityClass(Utils.class);
	}
	
	/** Multiple tests related of using fitToArray. */
	@Test
	void fitToArray() {
		assertEquals(0, Utils.fitToArray(VERBOSITY_LEVELS, -1));
		for (int i = 0; i < VERBOSITY_LEVELS.length; i++)
			assertEquals(i, Utils.fitToArray(VERBOSITY_LEVELS, i));
		
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length + 1));
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {-1000, -100, -10, -3, -2, -1, 0, 1, 2, 3, 10, 100, 1000})
	void fitToArrayEmptyArray(int index) {
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(new Object[0], index));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {-1000, -100, -10, -3, -2, -1, 0, 1, 2, 3, 10, 100, 1000})
	@SuppressWarnings("ConstantConditions")
		//may be suppressed here as want to ensure that null values throw an error
	void fitToArrayNullArray(int index) {
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, index));
	}
	
	/** Multiple tests related to method call clamp with null. */
	@SuppressWarnings("ConstantConditions") //may be suppressed here as want to ensure that null values throw an error
	@Test
	void clampInvalidArguments() {
		Integer[] values = {0, null};
		for (Integer val : values)
			for (Integer min : values)
				for (Integer max : values)
					if (!ObjectUtils.allNotNull(val, min, max))
						assertThrows(NullPointerException.class, () -> Utils.clamp(val, min, max));
	}
	
	@ParameterizedTest
	@CsvSource({"1, 0", "10, 0", "100, -100", "2000, 1000", Integer.MAX_VALUE+", "+Integer.MIN_VALUE})
	void testClampMaxSmallerMin(int min, int max) {
		assertThrows(IllegalArgumentException.class, () -> Utils.clamp(0, min, max));
	}
	
	@ParameterizedTest
	@CsvSource({"0, 0", "1, 1", "0, 1", "0, 10", "-100, 100", "1000, 2000"})
	void clamp(int min, int max) {
		IntStream.range(min, max).forEach(i -> assertEquals(i, Utils.clamp(i, min, max)));
	}
	
	/**
	 * Checks if the colorCodes all produce the expected color and if the expected color will be converted into one of
	 * the color-codes. The color-codes will be prepended with each of the {@link #COLOR_PREFIXES}.
	 */
	@ParameterizedTest(name = "Run {index}: ColorCodes: {0}")
	@ArgumentsSource(ColorProvider.class)
	void testColorCode(@NotNull String[] colorCodes, Color expected) {
		//Check if getColorOrNull works correctly
		for (String code : colorCodes)
			for (String pre : COLOR_PREFIXES)
				assertEquals(expected, getColorOrNull(pre + code));
		//Check if colorToString works correctly
		var actual = colorToString(expected).replaceFirst("(?i)0x|#", "");
		assertTrue(Arrays.stream(colorCodes).anyMatch(actual::equalsIgnoreCase),
				   "Expected one of: #" + Arrays.toString(colorCodes) + " but was: #" + actual);
	}
	
	@Test
	void testGetNullColor() {
		assertNull(getColorOrNull(null));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"BLACK", "#f", "#ff", "#ffff", "#fffff", "#ffffgg", "0xffffgg", "#f3875323", "foo",
			"0Xkkkkk", "1Xffffff", "#varargs", "#a color"})
	void testIllegalColor(String code) {
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull(code));
	}
}