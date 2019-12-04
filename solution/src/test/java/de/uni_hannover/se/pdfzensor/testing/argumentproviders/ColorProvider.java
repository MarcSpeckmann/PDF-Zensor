package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The ColorProvider may be used as a JUnit {@link ArgumentsProvider} to feed corresponding String[]-Color-pairs as
 * arguments. Each of the string-values is a valid hexadecimal representation for the given color if any {@link
 * #COLOR_PREFIXES} is added.
 */
public class ColorProvider implements ArgumentsProvider {
	public static final String[] COLOR_PREFIXES = {"0X", "0x", "#"};
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
	
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		return COLORS.entrySet().stream().map(e -> Arguments.of(e.getValue(), e.getKey()));
	}
}
