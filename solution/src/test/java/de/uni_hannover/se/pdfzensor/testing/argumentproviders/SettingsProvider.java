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
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.argumentproviders.CLArgumentProvider.expExpressions;
import static de.uni_hannover.se.pdfzensor.testing.argumentproviders.ConfigProvider.expectedColorsForConfig;

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
	 * @param out  The output file which should be converted into an argument.
	 * @param lvl  The verbosity level which should be converted into an argument.
	 * @param mode The mode which should be converted into an argument.
	 * @param exp  The expressions as a string-string pair
	 * @return The given values converted into valid command-line arguments including an input.
	 */
	@NotNull
	private static String[] createCLArguments(@Nullable String out, final int lvl, @Nullable Mode mode,
											  @Nullable ArrayList<ImmutablePair<@NotNull String, @Nullable String>> exp) {
		var arguments = new ArrayList<String>();
		
		arguments.add("sample.pdf");
		if (out != null) {
			arguments.add("-o");
			arguments.add(out);
		}
		if (mode != null) {
			switch (mode) {
				case MARKED:
					arguments.add("-m");
					break;
				case UNMARKED:
					arguments.add("-u");
					break;
			}
		}
		if (lvl > 0)
			arguments.add("-" + "v".repeat(lvl));
		if (exp != null) {
			for (var pair : exp) {
				arguments.add("-e");
				arguments.add(pair.getLeft());
				if (pair.getRight() != null)
					arguments.add(pair.getRight());
			}
		}
		
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
			list.add(Arguments.of(null,
								  createCLArguments(null, -1, null, expList),
								  "sample.pdf", null, null, null, new ArrayList<>(expList), null));
			
			// Mode set by CLArgs
			list.add(Arguments.of("testVerbosityAsIntegerValidConfig.json",
								  createCLArguments(null, -1, Mode.MARKED, expList),
								  "sample.pdf", "censoredFile.pdf", Level.DEBUG, Mode.MARKED, new ArrayList<>(expList),
								  null));
			// output overwritten by CLArgs
			list.add(Arguments.of("testVerbosityAsIntegerValidConfig.json",
								  createCLArguments("clArgsOutput.pdf", -1, Mode.UNMARKED, expList),
								  "sample.pdf", "clArgsOutput.pdf", Level.DEBUG, Mode.UNMARKED,
								  new ArrayList<>(expList), null));
			// verbosity overwritten by CLArgs
			list.add(Arguments.of("testVerbosityAsIntegerValidConfig.json",
								  createCLArguments(null, 6, null, expList),
								  "sample.pdf", "censoredFile.pdf", Level.TRACE, null, new ArrayList<>(expList), null));
			// verbosity downscaled
			list.add(Arguments.of("valid/high_verbosity.json",
								  createCLArguments("out.pdf", 5, null, expList),
								  "sample.pdf", "out.pdf", Level.DEBUG, Mode.ALL, new ArrayList<>(expList), null));
			// nested output
			list.add(Arguments.of("valid/mode_casesDiffer.json",
								  createCLArguments(null, -1, null, expList),
								  "sample.pdf", "nested" + File.separatorChar + "output.pdf", null, Mode.UNMARKED,
								  new ArrayList<>(expList), null));
			// with default colors
			for (var e : expectedColorsForConfig.entrySet()) {
				list.add(Arguments.of(e.getKey(),
									  createCLArguments(null, -1, null, expList),
									  "sample.pdf", null, null, null, new ArrayList<>(expList), e.getValue()));
			}
		}
		
		return list.stream();
	}
}