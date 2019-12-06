package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import de.uni_hannover.se.pdfzensor.config.Mode;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;

/** This class generates arguments for ConfigTest for testing configs and implements {@link ArgumentsProvider}. */
public class ConfigProvider implements ArgumentsProvider {
	/**
	 * Constructs an argument with the specified values.
	 *
	 * @param configName The path to the config file.
	 * @param out        The expected output file.
	 * @param lvl        The expected logger verbosity.
	 * @param mode       The expected censor mode.
	 * @return An Argument containing the config file and the expected values.
	 */
	@NotNull
	private static Arguments createArgument(@Nullable String configName, @Nullable String out, @Nullable Level lvl,
											@Nullable Mode mode) {
		var config = Optional.ofNullable(configName).map(c -> getResource(CONFIG_PATH + c)).orElse(null);
		var outFile = Optional.ofNullable(out).map(File::new).orElse(null);
		return Arguments.of(config, outFile, lvl, mode);
	}
	
	/**
	 * This method provides an argument stream for <code>ConfigTest#testValidConfigurations(File, String, Level,
	 * Mode)</code>. It contains arguments in the form of the config file followed by the values which are expected
	 * after parsing.
	 *
	 * @param extensionContext encapsulates the context in which the current test or container is being executed.
	 * @return stream of all created arguments
	 */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		list.add(createArgument(null, null, null, null));
		list.add(createArgument("testVerbosityAsIntegerValidConfig.json", "censoredFile.pdf", Level.DEBUG, null));
		list.add(createArgument("testVerbosityAsStringValidConfig.json", "censoredFile.pdf", Level.DEBUG, null));
		list.add(createArgument("valid/high_verbosity.json", "censoredFile.pdf", Level.ALL, Mode.ALL));
		list.add(createArgument("valid/mode_casesDiffer.json", "nested" + File.separatorChar + "output.pdf", null,
								Mode.UNMARKED));
		list.add(createArgument("valid/negative_verbosity.json", "censoredFile.pdf", Level.OFF, Mode.MARKED));
		list.add(createArgument("valid/still_a_json.txt", "censoredFile.pdf", Level.OFF, null));
		list.add(createArgument("valid/unknown_mode.json", null, null, null));
		list.add(createArgument("valid/unknown_verbosity.json", "censoredFile.pdf", null, Mode.UNMARKED));
		return list.stream();
	}
}