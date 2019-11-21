package de.uni_hannover.se.pdfzensor.config;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

/**
 * The class is responsible for parsing the given command-line arguments
 *
 */
@Command(name = "pdf-zensor", version = DummyVersionProvider.VERSION, description = {"--Here could be your description--"})
final class CLArgs {
	
	@CommandLine.Parameters(paramLabel = "\"in.pdf\"", description = {"Set the input file to censor. Required."}, arity = "1")
	@Nullable
	private File input = null;
	
	@Option(names = {"-o", "--out"}, paramLabel = "\"out\"", description = {"Set a specific output file to use."}, arity = "1")
	@Nullable
	private File output = null;
	
	@Option(names = {"-v", "--verbose"}, description = {"Specify multiple -v options to increase verbosity."}, arity = "0")
	@Nullable
	private boolean[] verbose = null;
	
	/**
	 * This method is parsing the command-line arguments.
	 *
	 * @param args the command-line arguments which will be parsed
	 * @return an CLArgs object which contains all information about the parsed arguments
	 */
	@NotNull
	static CLArgs fromStringArray(@NotNull final String... args) {
		Validate.notEmpty(args);
		final CLArgs clArgs = new CLArgs();
		final CommandLine cmd = new CommandLine(clArgs);
		cmd.parseArgs(Validate.noNullElements(args));
		return clArgs;
	}
	
	/**
	 * @return The absolute input file which was specified.
	 */
	@NotNull
	final File getInput() {
		Objects.requireNonNull(input, "The input must be an existing PDF-file.");
		return Optional.of(input)
					   .filter(File::isFile)
					   .filter(f -> "pdf".equals(FileUtils.getFileExtension(f)))
					   .map(File::getAbsoluteFile)
					   .orElseThrow();
	}
	
	/**
	 * @return null or the absolute output file if one was specified.
	 */
	@Nullable
	final File getOutput() {
		//TODO: prevent to save into other file types then .pdf or directories
		return Optional.ofNullable(output)
					   .map(File::getAbsoluteFile)
					   .orElse(null);
	}
	
	/**
	 * Returns verbosity level given bei the user
	 *
	 * @return null or the level of logging verbosity if verbose was given
	 */
	@Contract(pure = true)
	@Nullable
	Level getVerbosity() {
		return verbose == null ? null : VERBOSITY_LEVELS[fitToArray(VERBOSITY_LEVELS, verbose.length)];
	}
	
}
