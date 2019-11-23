package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.config.Settings.colorToString;
import static org.junit.jupiter.api.Assertions.*;


/** SettingsTest should contain all unit-tests related to {@link Settings}. */
class SettingsTest {
	private static final String[] COLOR_PREFIXES = {"0X", "0x", "#"};
	private static final Map<Color, String[]> COLORS = new HashMap<>();
	
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
	
	/** Unit-tests for {@link Settings} constructor Settings */
	@Test
	void testSettings() {
		// if the command line argument is not given or has a faulty structure
		//TODO the following test does not Work yet
		//assertThrows(NullPointerException.class, () -> new Settings(null));
		assertThrows(IllegalArgumentException.class, () -> new Settings(new String[2]));
		// if the command line argument is given but not valid
		// split uses whitespace as delimiter and splits the single string into an array of multiple strings for using it as an argument
		//
		assertThrows(picocli.CommandLine.UnmatchedArgumentException.class,
					 () -> new Settings("pdf-zensor", "\"NichtExistenteDatei.pdf\""));
		// for this test there has to be a zensieren.pdf file in the same directory but no config.json
		assertThrows(picocli.CommandLine.UnmatchedArgumentException.class,
					 () -> new Settings("pdf-zensor", "\"zensieren.pdf\"", "-c", "\"config.json\""));
	}
	
	/**
	 * Checks if the colorCodes all produce the expected color and if the expected color will be converted into one of
	 * the color-codes. The color-codes will be prepended with each of the {@link #COLOR_PREFIXES}.
	 */
	@ParameterizedTest(name = "Run {index}: ColorCodes: {0}")
	@MethodSource("colorCodeProvider")
	void testColorCode(String[] colorCodes, Color expected) {
		//Check if getColorOrNull works correctly
		for (String code : colorCodes)
			for (String pre : COLOR_PREFIXES)
				assertEquals(expected, Settings.getColorOrNull(pre + code));
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
		assertNull(Settings.getColorOrNull(null));
		
		// check if invalid color-codes are detected
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("BLACK"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#f"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#ff"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#ffff"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#fffff"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#ffffgg"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("0xffffgg"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#f3875323"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("foo"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("0Xkkkkk"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("1Xffffff"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#varargs"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#a color"));
	}
}