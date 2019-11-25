package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.TestUtility;
import de.uni_hannover.se.pdfzensor.config.Settings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

/** UtilsTest should contain all unit-tests related to {@link Utils}. */
public class UtilsTest {
	private static final String[] COLOR_PREFIXES = {"0X", "0x", "#"};
	public static final Map<Color, String[]> COLORS = new HashMap<>();
	
	static {
		COLORS.put(Color.black, new String[]{"000", "000000"});
		COLORS.put(Color.red, new String[]{"f00", "ff0000"});
		COLORS.put(Color.green, new String[]{"0f0", "00ff00"});
		COLORS.put(Color.blue, new String[]{"00f", "0000ff"});
		COLORS.put(Color.yellow, new String[]{"ff0", "ffff00"});
		COLORS.put(Color.cyan, new String[]{"0ff", "00ffff"});
		COLORS.put(Color.magenta, new String[]{"f0f", "ff00ff"});
		COLORS.put(Color.white, new String[]{"fff", "ffffff"});
		COLORS.put(Color.DARK_GRAY, new String[]{"404040"});
		COLORS.put(Color.GRAY, new String[]{"808080"});
		// some random colors
		COLORS.put(new Color(130, 150, 161), new String[]{"8296A1"});
		COLORS.put(new Color(77, 52, 67), new String[]{"4D3443"});
		COLORS.put(new Color(18, 10, 77), new String[]{"120A4D"});
		COLORS.put(new Color(18, 52, 86), new String[]{"123456"});
		COLORS.put(new Color(3, 77, 31), new String[]{"034D1F"});
		COLORS.put(new Color(77, 76, 27), new String[]{"4D4C1B"});
		COLORS.put(new Color(86, 42, 86), new String[]{"562A56"});
		COLORS.put(new Color(250, 204, 204), new String[]{"FACCCC"});
	}
	
	/** Provides a set of arguments for {@link #testColorCode(String[], Color)} generated from {@link #COLORS}. */
	private static Stream<Arguments> colorCodeProvider() {
		return COLORS.entrySet()
					 .stream()
					 .map(e -> Arguments.of(e.getValue(), e.getKey()));
	}
	
	/** Multiple tests related of using fitToArray. */
	@Test
	void fitToArray() {
		TestUtility.assertIsUtilityClass(Utils.class);
		
		assertEquals(0, Utils.fitToArray(VERBOSITY_LEVELS, -1));
		for (int i = 0; i < VERBOSITY_LEVELS.length; i++)
			assertEquals(i, Utils.fitToArray(VERBOSITY_LEVELS, i));
		
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length + 1));
		assertEquals(VERBOSITY_LEVELS.length - 1, Utils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length));
	}
	
	/** Multiple tests related to method call fitToArray with an empty array. */
	@Test
	void fitToArrayEmptyArray() {
		var emptyArray = new Object[0];
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyArray, 1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyArray, -1));
		assertThrows(IllegalArgumentException.class, () -> Utils.fitToArray(emptyArray, 0));
	}
	
	/** Multiple tests related to method call fitToArray with null instead of an array. */
	@SuppressWarnings("ConstantConditions") //may be suppressed here as want to ensure that null values throw an error
	@Test
	void fitToArrayNullArray() {
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, 1));
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, -1));
		assertThrows(NullPointerException.class, () -> Utils.fitToArray(null, 0));
	}
	
	/** Multiple tests related to method call clamp with null. */
	@SuppressWarnings("ConstantConditions") //may be suppressed here as want to ensure that null values throw an error
	@Test
	void clampInvalidArguments() {
		assertThrows(NullPointerException.class, () -> Utils.clamp(null, null, null));
		assertThrows(NullPointerException.class, () -> Utils.clamp(null, 0, 0));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, 0, null));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, null, 0));
		assertThrows(NullPointerException.class, () -> Utils.clamp(null, 0, 0));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, 0, null));
		assertThrows(NullPointerException.class, () -> Utils.clamp(0, null, 0));
		
		//Issue: max < min
		assertThrows(IllegalArgumentException.class, () -> Utils.clamp(0, 1, 0));
	}
	
	/** Multiple tests related to using fitToArray. */
	@Test
	void clamp() {
		assertEquals(1, Utils.clamp(0, 1, 6));
		assertEquals(4, Utils.clamp(6, 2, 4));
		assertEquals(2, Utils.clamp(2, 1, 3));
	}
	
	/**
	 * Checks if the colorCodes all produce the expected color and if the expected color will be converted into one of
	 * the color-codes. The color-codes will be prepended with each of the {@link #COLOR_PREFIXES}.
	 */
	@ParameterizedTest(name = "Run {index}: ColorCodes: {0}")
	@MethodSource("colorCodeProvider")
	void testColorCode(@NotNull String[] colorCodes, Color expected) {
		//Check if getColorOrNull works correctly
		for (String code : colorCodes)
			for (String pre : COLOR_PREFIXES)
				assertEquals(expected, getColorOrNull(pre + code));
		//Check if colorToString works correctly
		var actual = colorToString(expected);
		boolean valid = false;
		for (String code : colorCodes) {
			for (String pre : COLOR_PREFIXES) {
				if (actual.equalsIgnoreCase(pre + code)) {
					valid = true;
					break;
				}
			}
			if (valid) break;
		}
		assertTrue(valid, "Expected one of: #" + Arrays.toString(colorCodes) + " but was: " + actual);
		
	}
	
	/** Unit-tests for {@link Settings} function getColorOrNull */
	@Test
	void testIllegalColors() {
		// check if a null-argument results in null
		assertNull(getColorOrNull(null));
		
		// check if invalid color-codes are detected
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("BLACK"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#f"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#ff"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#ffff"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#fffff"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#ffffgg"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("0xffffgg"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#f3875323"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("foo"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("0Xkkkkk"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("1Xffffff"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#varargs"));
		assertThrows(IllegalArgumentException.class, () -> getColorOrNull("#a color"));
	}
}