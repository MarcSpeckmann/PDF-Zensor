package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import de.uni_hannover.se.pdfzensor.config.Mode;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;

/** This class generates arguments for ConfigTest for testing configs and implements {@link ArgumentsProvider}. */
public class ConfigProvider implements ArgumentsProvider {
	
	/** A map of the path to the config file and the colors which are expected to be parsed from that file. */
	static final Map<String, Color[]> expectedColorsForConfig = new HashMap<>();
	
	static {
		expectedColorsForConfig.put("valid/defaultColorsNone.json", null);
		expectedColorsForConfig.put("valid/defaultColorsAllValid.json",
									new Color[]{Color.RED, Color.BLACK, Color.GREEN, Color.BLUE});
	}
	
	/**
	 * Constructs an argument with the specified values.
	 *
	 * @param configName The path to the config file.
	 * @param out        The expected output file.
	 * @param lvl        The expected logger verbosity.
	 * @param mode       The expected censor mode.
	 * @param colors     The expected default colors.
	 * @return An Argument containing the config file and the expected values.
	 */
	private static Arguments createArgument(@Nullable String configName, @Nullable String out, @Nullable Level lvl,
											@Nullable Mode mode, @Nullable Color[] colors) {
		var config = Optional.ofNullable(configName).map(c -> getResource(CONFIG_PATH + c)).orElse(null);
		var outFile = Optional.ofNullable(out).map(File::new).orElse(null);
		return Arguments.of(config, outFile, lvl, mode, colors);
	}
	
	/**
	 * This method provides an argument stream for <code>ConfigTest#testValidConfigurations(File, File, Level, Mode,
	 * Color[])</code>. It contains arguments in the form of the config file followed by the values which are expected
	 * after parsing.
	 *
	 * @param extensionContext encapsulates the context in which the current test or container is being executed.
	 * @return stream of all created arguments
	 */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		list.add(createArgument(null, null, null, null, null));
		list.add(createArgument("testVerbosityAsIntegerValidConfig.json", "censoredFile.pdf",
								Level.DEBUG, null, null));
		list.add(createArgument("testVerbosityAsStringValidConfig.json", "censoredFile.pdf",
								Level.DEBUG, null, null));
		list.add(createArgument("valid/high_verbosity.json", "censoredFile.pdf",
								Level.ALL, Mode.ALL, null));
		list.add(createArgument("valid/mode_casesDiffer.json", "nested" + File.separatorChar + "output.pdf",
								null, Mode.UNMARKED, null));
		list.add(createArgument("valid/negative_verbosity.json", "censoredFile.pdf",
								Level.OFF, Mode.MARKED, null));
		list.add(createArgument("valid/still_a_json.txt", "censoredFile.pdf",
								Level.OFF, null, null));
		list.add(createArgument("valid/unknown_mode.json", null,
								null, null, null));
		list.add(createArgument("valid/unknown_verbosity.json", "censoredFile.pdf",
								null, Mode.UNMARKED, null));
		for (var e : expectedColorsForConfig.entrySet())
			list.add(createArgument(e.getKey(), null, null, null, e.getValue()));
		
		return list.stream();
	}
}