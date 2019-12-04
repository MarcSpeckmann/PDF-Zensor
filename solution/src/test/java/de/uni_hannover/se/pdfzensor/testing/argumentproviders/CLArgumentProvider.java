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
	 * List of all possible mode options.
	 */
	private static final Mode[] modeOptions = new Mode[]{Mode.MARKED, Mode.UNMARKED, null};
	
	/**
	 * This method creates an Argument which contains a input file, output file and verbosity level depending on the
	 * given method inputs.
	 *
	 * @param in   input file
	 * @param out  output file
	 * @param lvl  verbosity level
	 * @param mode mode indicating which argument to set
	 * @return a Argument of created commando line arguments and the method inputs
	 */
	@NotNull
	private static Arguments createArgument(@NotNull String in, @Nullable String out, final int lvl,
											@Nullable Mode mode) {
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
		// No tests without input file, because this case would be caught by the main.
		var inFile = new File(in);
		var outFile = Optional.ofNullable(out).map(File::new).orElse(null);
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile, verbosity, mode);
	}
	
	/**
	 * This method provides an argument stream for parametrized test. {@link #createArgument(String, String, int, Mode)}
	 * will be called with each possible combination of {@link #inputFiles}, {@link #outputFiles}, {@link
	 * #verbosityLevels} and the achievable {@link Mode} settings via the command line.
	 * <br>
	 * {@link Mode} via the command line: {@link Mode#MARKED} for <code>-m</code>, {@link Mode#UNMARKED} for
	 * <code>-u</code> or * <code>null</code> for neither (<code>-m</code> or <code>-u</code>).
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
						list.add(createArgument(in, out, lvl, mode));
		return list.stream();
	}
	
}
