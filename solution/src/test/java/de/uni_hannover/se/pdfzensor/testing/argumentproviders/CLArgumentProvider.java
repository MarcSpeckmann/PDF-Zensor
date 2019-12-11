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
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

/**
 * This class generates arguments for CLArgsTest for testing CLArgs and implements {@link ArgumentsProvider}.
 */
public class CLArgumentProvider implements ArgumentsProvider {
	/** List of template input files. */
	private static final String[] inputFiles = {"/pdf-files/sample.pdf", "/pdf-files/sample.bla.pdf"};
	/** List of template output files. */
	private static final String[] outputFiles = {null, "file.pdf", "src/test/resources/sample.pdf", "weirdSuffix.bla.pdf"};
	/** List of all possible verbosity levels. */
	private static final int[] verbosityLevels = IntStream.range(0, VERBOSITY_LEVELS.length - 2).toArray();
	/** List of all possible mode options. */
	private static final Mode[] modeOptions = {Mode.MARKED, Mode.UNMARKED, null};
	/** List containing lists of pairs, the left element of the pair is a regex and the right element is a hex color */
	static final ArrayList<ArrayList<ImmutablePair<String, String>>> expExpressions = new ArrayList<>();
	
	static {
		final String[] expressionRegex = {"reg0", "reg1", "reg2", "reg3", "reg4"};
		final List<String> colorsWithInjectedNull = new ArrayList<>();
		
		for (var cPrefix : ColorProvider.COLOR_PREFIXES) {
			var i = 0;
			for (var colors : ColorProvider.COLORS.values()) {
				for (var color : colors)
					colorsWithInjectedNull.add(cPrefix + color);
				if (i % 2 == 0)
					colorsWithInjectedNull.add(null);
				i++;
			}
		}
		
		for (var i = 0; i < colorsWithInjectedNull.size(); i++) {
			var expressions = new ArrayList<ImmutablePair<String, String>>();
			for (String regex : expressionRegex) {
				var color = i + 1 < colorsWithInjectedNull.size() ? colorsWithInjectedNull.get(i++) : null;
				expressions.add(new ImmutablePair<>(regex, color));
				// inside the loop to test for varying number of expressions
				expExpressions.add(expressions);
			}
		}
	}
	
	/**
	 * This method creates an Argument which contains a input file, output file, verbosity level, mode and expressions
	 * depending on the given method inputs.
	 *
	 * @param in    input file
	 * @param out   output file
	 * @param lvl   verbosity level (zero equals {@link Level#WARN})
	 * @param mode  mode indicating which argument to set
	 * @param exp   expressions as a string-string pair
	 * @param quiet quiet logging
	 * @return a Argument of created commando line arguments and the method inputs
	 */
	@NotNull
	private static Arguments createArgument(@NotNull String in, @Nullable String out, final int lvl,
											@Nullable Mode mode,
											@Nullable ArrayList<ImmutablePair<@NotNull String, @Nullable String>> exp,
											boolean quiet) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		if (out != null) {
			arguments.add("-o");
			arguments.add(out);
		}
		Level verbosity = null;
		if (lvl > 0) {
			arguments.add("-" + "v".repeat(lvl));
			verbosity = VERBOSITY_LEVELS[fitToArray(VERBOSITY_LEVELS, lvl + 3)];
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
		if (exp != null) {
			for (var pair : exp) {
				arguments.add("-e");
				arguments.add(pair.getLeft());
				if (pair.getRight() != null)
					arguments.add(pair.getRight());
			}
		}
		if (quiet)
			arguments.add("-q");
		// No tests without input file, because this case would be caught by the main.
		var inFile = new File(in);
		var outFile = Optional.ofNullable(out).map(File::new).orElse(null);
		var expressions = Optional.ofNullable(exp).orElse(new ArrayList<>());
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile, verbosity, mode, expressions, quiet);
	}
	
	/**
	 * This method provides an argument stream for a parametrized test. {@link #createArgument(String, String, int,
	 * Mode, ArrayList, boolean)} will be called with each possible combination of {@link #inputFiles}, {@link
	 * #outputFiles}, {@link #verbosityLevels}, achievable {@link Mode} settings via the command line and quiet settings
	 * but without expressions.
	 * <br>
	 * {@link Mode} via the command line: {@link Mode#MARKED} for <code>-m</code>, {@link Mode#UNMARKED} for
	 * <code>-u</code> or * <code>null</code> for neither (<code>-m</code> or <code>-u</code>).
	 * <br>
	 * Note: The tests for expressions are only disconnected from the input-output-verbosity-mode tests to reduce
	 * testing time. The argument consumer and correctness of parsing optional positional parameters is still tested
	 * thoroughly.
	 *
	 * @param extensionContext encapsulates the context in which the current test or container is being executed.
	 * @return stream of all created arguments
	 */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		for (String in : inputFiles)
			for (String out : outputFiles)
				for (int lvl : verbosityLevels)
					for (Mode mode : modeOptions)
						for (boolean quiet : List.of(true, false))
							list.add(createArgument(in, out, lvl, mode, null, quiet));
		for (String in : inputFiles)
			for (var expList : expExpressions)
				list.add(createArgument(in, null, -1, null, new ArrayList<>(expList), false));
		return list.stream();
	}
}