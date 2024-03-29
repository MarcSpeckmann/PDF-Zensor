package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ColorProvider;
import org.apache.commons.lang3.Functions;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.*;
import java.util.stream.IntStream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

/** UtilsTest should contain all unit-tests related to {@link Utils}. */
class UtilsTest {
	
	/**
	 * Checks general properties of the {@link Utils}-class.
	 */
	@Test
	void testGeneral() {
		TestUtility.assertIsUtilityClass(Utils.class);
	}
	
	/**
	 * Checks if {@link Utils#fitToArray(Object[], int)} works on the {@link de.uni_hannover.se.pdfzensor.Logging#VERBOSITY_LEVELS}
	 * by passing each valid index, -1, length and length+1 into the function and checking if the output is as
	 * expected.
	 */
	@Test
	void fitToArray() {
		assertEquals(0, Utils.fitToArray(VERBOSITY_LEVELS, -1));
		for (int i = 0; i < VERBOSITY_LEVELS.length; i++)
			assertEquals(i, Utils.fitToArray(VERBOSITY_LEVELS, i));
		
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length + 1));
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length));
	}
	
	/**
	 * Checks that the sensing of erroneous inputs into {@link Utils#fitToArray(Object[], int)} is not dependent of
	 * other arguments. By asserting that for each passed value a {@link IllegalArgumentException} is thrown when the
	 * array-argument is set to be empty.
	 *
	 * @param index the index to check for that its passing does not change the output from an {@link
	 *              IllegalArgumentException}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1000, -100, -10, -3, -2, -1, 0, 1, 2, 3, 10, 100, 1000})
	void fitToArrayEmptyArray(int index) {
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(new Object[0], index));
	}
	
	/**
	 * Checks that the sensing of erroneous inputs into {@link Utils#fitToArray(Object[], int)} is not dependent of
	 * other arguments. By asserting that for each passed value a {@link NullPointerException} is thrown when the
	 * array-argument is set to <code>null</code>.
	 *
	 * @param index the index to check for that its passing does not change the output from an {@link
	 *              NullPointerException}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {-1000, -100, -10, -3, -2, -1, 0, 1, 2, 3, 10, 100, 1000})
	@SuppressWarnings("ConstantConditions")
	//may be suppressed here as want to ensure that null values throw an error
	void fitToArrayNullArray(int index) {
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, index));
	}
	
	/**
	 * Checks any combinations of <code>null</code>-values in an argument of {@link Utils#clamp(Comparable, Comparable,
	 * Comparable)} (except non <code>null</code>) if they all get identified as erroneous inputs and if thus a {@link
	 * NullPointerException} is thrown.
	 */
	@SuppressWarnings("ConstantConditions") // we explicitly want to check if this issue is noted at runtime
	@Test
	void clampInvalidArguments() {
		final Integer[] values = {0, null};
		for (Integer val : values)
			for (Integer min : values)
				for (Integer max : values)
					if (!ObjectUtils.allNotNull(val, min, max))
						assertThrows(NullPointerException.class, () -> Utils.clamp(val, min, max));
	}
	
	/**
	 * Tests for each passed range if {@link Utils#clamp(Comparable, Comparable, Comparable)} correctly identifies that
	 * the range is faulty (<code>max &gt; min</code>).
	 *
	 * @param min the <b>upper</b> bound of the tested range.
	 * @param max the <b>lower</b> bound of the tested range.
	 */
	@ParameterizedTest
	@CsvSource({"1, 0", "10, 0", "100, -100", "2000, 1000", Integer.MAX_VALUE + ", " + Integer.MIN_VALUE})
	void testClampMaxSmallerMin(int min, int max) {
		assertThrows(IllegalArgumentException.class, () -> Utils.clamp(0, min, max));
	}
	
	/**
	 * Tests for each passed range if any value within the range gets mapped to itself correctly by {@link
	 * Utils#clamp(Comparable, Comparable, Comparable)}.
	 *
	 * @param min the lower bound of the tested range.
	 * @param max the upper bound of the tested range.
	 */
	@ParameterizedTest
	@CsvSource({"0, 0", "1, 1", "0, 1", "0, 10", "-100, 100", "1000, 2000"})
	void clamp(int min, int max) {
		IntStream.range(min, max).forEach(i -> assertEquals(i, Utils.clamp(i, min, max)));
	}
	
	/**
	 * Asserts for each ColorCodes-Color-pair that all of the provided color-codes are representing the expected color
	 * when adding anyone of the {@link ColorProvider#COLOR_PREFIXES}. To test validity for each prefix-code-combination
	 * it is asserted that {@link Utils#getColorOrNull(String)} returns the correct color.
	 * <br>
	 * Also tests that {@link Utils#isHexColorCode(String)} will return true for each prefix-code-combination.
	 * <br>
	 * The reverse direction is also tested by calling {@link Utils#colorToString(Color)} and checking if it returns any
	 * of the color-codes when stripped of its prefix.
	 *
	 * @param colorCode A color-code that should correspond to the given color.
	 * @param expected  The color that should be represented in hexadecimal notation by the color-code.
	 */
	@ParameterizedTest(name = "Run {index}: colorCode: {0}")
	@ArgumentsSource(ColorProvider.class)
	void testColorCode(@NotNull String colorCode, @NotNull Color expected) {
		//Check if getColorOrNull works correctly
		assertEquals(expected, getColorOrNull(colorCode));
		assertTrue(isHexColorCode(colorCode));
		//Check if colorToString works correctly
		var exp = colorCode.replaceFirst("(?i)0x|#", "");
		if (exp.length() > 3) {
			var actual = colorToString(expected).replaceFirst("(?i)0x|#", "");
			assertTrue(exp.equalsIgnoreCase(actual),
					   String.format("Expected %s to match the color %s, but was %s.", exp, expected, actual));
		}
	}
	
	/**
	 * Checks if {@link Utils#getColorOrNull(String)} behaves correctly for a passed <code>null</code> value. That is
	 * that <code>null</code> should be returned.
	 */
	@Test
	void testGetNullColor() {
		assertNull(getColorOrNull(null));
	}
	
	/**
	 * Asserts for each of the passed color-codes that it is invalid by calling {@link Utils#getColorOrNull(String)} and
	 * expecting an {@link IllegalArgumentException}.
	 * <br>
	 * Also tests that {@link Utils#isHexColorCode(String)} will return false for invalid color codes.
	 *
	 * @param code the color-code to assert invalidity for.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"BLACK", "#f", "#ff", "#ffff", "#fffff", "#ffffgg", "0xffffgg", "#f3875323", "foo",
			"0Xkkkkk", "1Xffffff", "#varargs", "#a color"})
	void testIllegalColor(String code) {
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull(code));
		assertFalse(isHexColorCode(code));
	}
	
	/**
	 * Tests tryCall on two cases: Checks if non-throwing functions are executed, throwing-functions are aborted.
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testTryCall() {
		final boolean[] called = {false};
		final Functions.FailableRunnable<Exception> nonThrowing = () -> called[0] = true;
		final Functions.FailableRunnable<UnsupportedOperationException> throwing = () -> {
			throw new UnsupportedOperationException();
		};
		final var logger = Logging.getLogger();
		
		assertThrows(NullPointerException.class, () -> tryCall(null, null, null));
		
		called[0] = false;
		assertThrows(NullPointerException.class, () -> tryCall(nonThrowing, null, null));
		assertFalse(called[0]);
		
		called[0] = false;
		assertTrue(tryCall(nonThrowing, logger, null));
		assertTrue(called[0]);
		
		called[0] = false;
		assertTrue(tryCall(nonThrowing, logger, "message"));
		assertTrue(called[0]);
		
		called[0] = false;
		assertFalse(tryCall(throwing, logger, null));
		assertFalse(called[0]);
		
		called[0] = false;
		assertFalse(tryCall(throwing, logger, "message"));
		assertFalse(called[0]);
		Logging.deinit();
	}
	
	//TODO: implement correctly (the interrupted flag gets cleared by the join)
	/*
	@Test
	void testTryCallInterrupted() {
		final var logger = Logging.getLogger();
		final Functions.FailableRunnable<InterruptedException> longMethod = () -> {Thread.sleep(100000);};
		Thread t = new Thread(() -> tryCall(longMethod, logger, null));
		t.start();
		t.interrupt();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(t.isInterrupted());
		
		Logging.deinit();
	}*/
	
}