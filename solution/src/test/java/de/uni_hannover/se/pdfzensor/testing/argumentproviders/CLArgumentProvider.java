package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

/**
 * This class generates arguments for CLArgsTest for testing CLArgs and implements {@link ArgumentsProvider}.
 */
public class CLArgumentProvider implements ArgumentsProvider {
	/**
	 * List of template input files.
	 */
	private static final String[] inputFiles = {"/pdf-files/sample.pdf", "/pdf-files/sample.bla.pdf"};
	/**
	 * List of template output files.
	 */
	private static final String[] outputFiles = {null, "file.pdf", "src/test/resources/sample.pdf", "weirdSuffix.bla.pdf"};
	/**
	 * List of all possible verbosity levels.
	 */
	private static final int[] verbosityLevels = IntStream.range(0, VERBOSITY_LEVELS.length + 1).toArray();
	
	/**
	 * This method creates an Argument which contains a input file, output file and verbosity level depending on the
	 * given method inputs.
	 *
	 * @param in  input file
	 * @param out output file
	 * @param lvl verbosity level
	 * @return a Argument of created commando line arguments and the method inputs
	 */
	@NotNull
	private static Arguments createArgument(@NotNull String in, @Nullable String out, final int lvl) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		if (out != null) {
			arguments.add("-o");
			arguments.add(out);
		}
		Level verbosity = null;
		if (lvl > 0) {
			arguments.add("-" + "v".repeat(lvl));
			verbosity = VERBOSITY_LEVELS[fitToArray(VERBOSITY_LEVELS, lvl)];
		}
		// No tests without input file, because this case would be caught by the main.
		var inFile = new File(in);
		var outFile = Optional.ofNullable(out).map(File::new).orElse(null);
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile, verbosity);
	}
	
	/**
	 * This method provides an argument stream for parametrized test. {@link #createArgument(String, String, int)} will
	 * be called with each possible combination of {@link #inputFiles}, {@link #outputFiles} and {@link
	 * #verbosityLevels}
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
					list.add(createArgument(in, out, lvl));
		return list.stream();
	}
	
}
