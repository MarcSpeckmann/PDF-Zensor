package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import de.uni_hannover.se.pdfzensor.config.Mode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.config.Mode.*;
import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResourcePath;
import static de.uni_hannover.se.pdfzensor.testing.argumentproviders.CLArgumentProvider.expExpressions;
import static de.uni_hannover.se.pdfzensor.testing.argumentproviders.ConfigProvider.*;

/** This class generates arguments for SettingsTest and implements {@link ArgumentsProvider}. */
public class SettingsProvider implements ArgumentsProvider {
	/**
	 * Creates the command-line arguments according to the given values similar to
	 * <code>CLArgumentProvider#createArgument(String, String, int, Mode, ArrayList)</code>.
	 * <br>
	 * Note that the input is not variable, this is because it cannot be set via the config and therefore does not
	 * require further testing when combining configuration file and command-line arguments (tests in
	 * <code>CLArgsTest</code> should suffice).
	 *
	 * @param out   The output file which should be converted into an argument.
	 * @param lvl   The verbosity level which should be converted into an argument (zero equals {@link Level#WARN}).
	 * @param mode  The mode which should be converted into an argument.
	 * @param exp   The expressions as a string-string pair.
	 * @param quiet The boolean specifying silencing the logging output.
	 * @return The given values converted into valid command-line arguments including an input.
	 */
	@NotNull
	private static String[] createCLArguments(@Nullable String config, @Nullable String out, final int lvl, @Nullable Mode mode,
											  @Nullable ArrayList<ImmutablePair<@NotNull String, @Nullable String>> exp,
											  boolean quiet) {
		Objects.requireNonNull(exp);
		var arguments = new ArrayList<String>();
		
		arguments.add("sample.pdf");
		
		if (config != null) {
			arguments.add("-c");
			arguments.add(getResourcePath(CONFIG_PATH + config));
		}
		
		if (out != null) {
			arguments.add("-o");
			arguments.add(out);
		}
		
		if (MARKED.equals(mode))
			arguments.add("-m");
		else if (UNMARKED.equals(mode))
			arguments.add("-u");
		
		if (lvl > 0)
			arguments.add("-" + "v".repeat(lvl));
		for (var pair : exp) {
			arguments.add("-e");
			arguments.add(pair.getLeft());
			Optional.ofNullable(pair.getRight()).ifPresent(arguments::add);
		}
		if (quiet)
			arguments.add("-q");
		
		return arguments.toArray(new String[0]);
	}
	
	/**
	 * This method provides an argument stream for <code>SettingsTest#testSettingsWithBoth()</code>. It contains
	 * arguments in the form of the config file followed by the given command-line arguments.
	 *
	 * @param extensionContext encapsulates the context in which the current test or container is being executed.
	 * @return stream of all created arguments
	 */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		for (var expList : expExpressions) {
			for (boolean quiet : List.of(true, false)) {
				list.add(Arguments.of(createCLArguments(null, null, -1, null, expList, quiet), "sample.pdf", null, null,
									  null, new ArrayList<>(expList), null, quiet));
				
				// Mode set by CLArgs
				list.add(Arguments.of(createCLArguments("testVerbosityAsIntegerValidConfig.json",
														null, -1, MARKED, expList, quiet), "sample.pdf",
									  "censoredFile.pdf", Level.DEBUG, MARKED, new ArrayList<>(expList), null,
									  quiet));
				// output overwritten by CLArgs
				list.add(Arguments.of(createCLArguments("testVerbosityAsIntegerValidConfig.json",
														"clArgsOutput.pdf", -1, Mode.UNMARKED, expList, quiet),
									  "sample.pdf", "clArgsOutput.pdf", Level.DEBUG, Mode.UNMARKED,
									  new ArrayList<>(expList), null, quiet));
				// verbosity overwritten by CLArgs
				list.add(Arguments.of(createCLArguments("testVerbosityAsIntegerValidConfig.json",
														null, 3, null, expList, quiet), "sample.pdf",
									  "censoredFile.pdf", Level.TRACE, null, new ArrayList<>(expList), null, quiet));
				// verbosity downscaled
				list.add(Arguments
								 .of(createCLArguments("valid/high_verbosity.json", "out.pdf", 2, null, expList, quiet),
									 "sample.pdf", "out.pdf", Level.DEBUG, Mode.ALL, new ArrayList<>(expList), null,
									 quiet));
				// nested output
				list.add(Arguments.of(createCLArguments("valid/mode_casesDiffer.json", null, -1, null, expList, quiet),
									  "sample.pdf", "nested" + File.separatorChar + "output.pdf", null, Mode.UNMARKED,
									  new ArrayList<>(expList), null, quiet));
			}
			// default colors in config
			for (var e : expectedColorsForConfig.entrySet()) {
				var expExpressionsList = new ArrayList<>(expList);
				var configList = expectedExpressionForConfig.get(e.getKey());
				if (configList != null)
					expExpressionsList.addAll(configList);
				list.add(Arguments.of(createCLArguments(e.getKey(),
														null, -1, null, expList, false),
									  "sample.pdf", null, null, null, expExpressionsList, e.getValue(), false));
			}
			// expressions in config
			for (var e : expectedExpressionForConfig.entrySet()) {
				var expExpressionsList = new ArrayList<>(expList);
				expExpressionsList.addAll(e.getValue());
				list.add(Arguments.of(createCLArguments(e.getKey(),
														null, -1, null, expList, false),
									  "sample.pdf", null, null, null, expExpressionsList,
									  expectedColorsForConfig.get(e.getKey()), false));
			}
		}
		
		return list.stream();
	}
}