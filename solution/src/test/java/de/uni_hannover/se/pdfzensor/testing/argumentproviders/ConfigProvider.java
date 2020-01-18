package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import de.uni_hannover.se.pdfzensor.config.Mode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;

/** This class generates arguments for ConfigTest for testing configs and implements {@link ArgumentsProvider}. */
public class ConfigProvider implements ArgumentsProvider {
	
	/** A map of the path to the config file and the colors which are expected to be parsed from that file. */
	static final Map<String, Color[]> expectedColorsForConfig = new HashMap<>();
	
	/** A map of the path to the config file and the expression expected to be parsed from that file. */
	static final Map<String, ArrayList<ImmutablePair<String, String>>> expectedExpressionForConfig = new HashMap<>();
	
	static {
		expectedColorsForConfig.put("valid/defaultColorsNone.json", null);
		expectedColorsForConfig.put("valid/defaultColorsAllValid.json",
									new Color[]{Color.RED, Color.BLACK, Color.GREEN, Color.BLUE});
		expectedColorsForConfig.put("valid/expressionsNoColorsDefaultColors.json",
									new Color[]{Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE});
		
		var expressions = new ArrayList<ImmutablePair<String, String>>(Arrays.asList(
				new ImmutablePair<>("Author: [a-zA-Z]*", "0xF0F0F0"),
				new ImmutablePair<>("hello world", null),
				new ImmutablePair<>("[A-Z]", "#00F")));
		expectedExpressionForConfig.put("valid/expressionsAllValid.json", expressions);
		expressions = new ArrayList<>(Arrays.asList(
				new ImmutablePair<>("black", null),
				new ImmutablePair<>("red", null),
				new ImmutablePair<>("green", null),
				new ImmutablePair<>("blue", null),
				new ImmutablePair<>("white", null)));
		expectedExpressionForConfig.put("valid/expressionsNoColorsDefaultColors.json", expressions);
	}
	
	/**
	 * Constructs an argument with the specified values.
	 *
	 * @param configName      The path to the config file.
	 * @param out             The expected output file.
	 * @param lvl             The expected logger verbosity.
	 * @param mode            The expected censor mode.
	 * @param intersectImages The expected intersecting image behavior.
	 * @param links           The expected given setting for distinguishing links.
	 * @param expressions     The expected expressions as a string-string pair list.
	 * @param colors          The expected default colors.
	 * @return An Argument containing the config file and the expected values.
	 */
	private static Arguments createArgument(@Nullable String configName, @Nullable String out, @Nullable Level lvl,
											@Nullable Mode mode,
											@Nullable Boolean intersectImages,
											@Nullable Boolean links,
											@Nullable ArrayList<ImmutablePair<String, String>> expressions,
											@Nullable Color[] colors) {
		var config = Optional.ofNullable(configName).map(c -> getResource(CONFIG_PATH + c)).orElse(null);
		var outFile = Optional.ofNullable(out).map(File::new).orElse(null);
		var intersectImg = Optional.ofNullable(intersectImages).orElse(false);
		var distLinks = Optional.ofNullable(links).orElse(false);
		return Arguments.of(config, outFile, lvl, mode, intersectImg, distLinks, expressions, colors);
	}
	
	/**
	 * This method provides an argument stream for <code>ConfigTest#testValidConfigurations(File, File, Level, Mode,
	 * boolean, ArrayList, Color[])</code>. It contains arguments in the form of the config file followed by the values
	 * which are expected after parsing.
	 *
	 * @param extensionContext encapsulates the context in which the current test or container is being executed.
	 * @return stream of all created arguments
	 */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		list.add(createArgument(null, null, null, null, null, null, null, null));
		list.add(createArgument("testVerbosityAsIntegerValidConfig.json", "censoredFile.pdf",
								Level.DEBUG, null, null, null, null, null));
		list.add(createArgument("testVerbosityAsStringValidConfig.json", "censoredFile.pdf",
								Level.DEBUG, null, null, null, null, null));
		list.add(createArgument("valid/high_verbosity.json", "censoredFile.pdf",
								Level.ALL, Mode.ALL, null, false, null, null));
		list.add(createArgument("valid/mode_casesDiffer.json", "nested" + File.separatorChar + "output.pdf",
								null, Mode.UNMARKED, true, null, null, null));
		list.add(createArgument("valid/negative_verbosity.json", "censoredFile.pdf",
								Level.OFF, Mode.MARKED, false, true, null, null));
		list.add(createArgument("valid/still_a_json.txt", "censoredFile.pdf",
								Level.OFF, null, true, null, null, null));
		list.add(createArgument("valid/unknown_mode.json", null,
								null, null, null, null, null, null));
		list.add(createArgument("valid/unknown_verbosity.json", "censoredFile.pdf",
								null, Mode.UNMARKED, null, null, null, null));
		for (var col : expectedColorsForConfig.entrySet())
			list.add(createArgument(col.getKey(), null, null, null, null, null,
									expectedExpressionForConfig.get(col.getKey()),
									col.getValue()));
		for (var exp : expectedExpressionForConfig.entrySet())
			list.add(createArgument(exp.getKey(), null, null, null, null, null, exp.getValue(),
									expectedColorsForConfig.get(exp.getKey())));
		
		return list.stream();
	}
}