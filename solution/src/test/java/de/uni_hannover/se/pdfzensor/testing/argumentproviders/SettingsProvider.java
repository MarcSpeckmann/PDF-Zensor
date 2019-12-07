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

/** This class generates arguments for SettingsTest and implements {@link ArgumentsProvider}. */
public class SettingsProvider implements ArgumentsProvider {
	/**
	 * A list containing strings for regex and color to construct the expected Expressions from. Not as extensive as
	 * {@link CLArgumentProvider}
	 */
	private static final ArrayList<ImmutablePair<@NotNull String, @Nullable String>> expExpressions = new ArrayList<>();
	
	static {
		expExpressions.add(0, new ImmutablePair<>(".", "#FFFFFF"));
		expExpressions.add(1, new ImmutablePair<>("reg1", "#123"));
		expExpressions.add(2, new ImmutablePair<>("reg2", "0xF0A78C"));
		expExpressions.add(3, new ImmutablePair<>("reg3", "#AbC"));
		expExpressions.add(4, new ImmutablePair<>("reg4", null));
	}
	
	/**
	 * A helper function to easily set what new elements of what collection of elements to use.
	 *
	 * @param replaceFrom The ArrayList from which to take the new elements.
	 * @param indices     The indices to take from replaceFrom and put into the return ArrayList.
	 * @param <T>         The type of the ArrayList.
	 * @return A new ArrayList containing the elements which were at the specified indices in the given ArrayList.
	 */
	@NotNull
	private static <T> ArrayList<T> replaceByIndices(@NotNull ArrayList<T> replaceFrom, @NotNull int... indices) {
		var list = new ArrayList<T>();
		for (int i : indices)
			list.add(replaceFrom.get(i));
		return list;
	}
	
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
			// don't add last one since it will be added regardless of the arguments
			for (var i = 0; i < exp.size() - 1; i++) {
				var pair = exp.get(i);
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
		
		var expList = replaceByIndices(expExpressions, 0);
		list.add(Arguments.of(null,
							  createCLArguments(null, -1, null, expList),
							  "sample.pdf", null, null, null, expList));
		
		// Mode set by CLArgs
		expList = replaceByIndices(expExpressions, 3, 0);
		list.add(Arguments.of("testVerbosityAsIntegerValidConfig.json",
							  createCLArguments(null, -1, Mode.MARKED, expList),
							  "sample.pdf", "censoredFile.pdf", Level.DEBUG, Mode.MARKED, expList));
		// output overwritten by CLArgs
		expList = replaceByIndices(expExpressions, 1, 2, 0);
		list.add(Arguments.of("testVerbosityAsIntegerValidConfig.json",
							  createCLArguments("clArgsOutput.pdf", -1, Mode.UNMARKED, expList),
							  "sample.pdf", "clArgsOutput.pdf", Level.DEBUG, Mode.UNMARKED, expList));
		// verbosity overwritten by CLArgs
		expList = replaceByIndices(expExpressions, 3, 1, 0);
		list.add(Arguments.of("testVerbosityAsIntegerValidConfig.json",
							  createCLArguments(null, 6, null, expList),
							  "sample.pdf", "censoredFile.pdf", Level.TRACE, null, expList));
		// verbosity downscaled
		expList = replaceByIndices(expExpressions, 4, 2, 0);
		list.add(Arguments.of("valid/high_verbosity.json",
							  createCLArguments("out.pdf", 5, null, expList),
							  "sample.pdf", "out.pdf", Level.DEBUG, Mode.ALL, expList));
		// nested output
		expList = replaceByIndices(expExpressions, 4, 0);
		list.add(Arguments.of("valid/mode_casesDiffer.json",
							  createCLArguments(null, -1, null, expList),
							  "sample.pdf", "nested" + File.separatorChar + "output.pdf", null, Mode.UNMARKED,
							  expList));
		
		return list.stream();
	}
}