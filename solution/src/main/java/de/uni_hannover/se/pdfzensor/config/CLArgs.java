package de.uni_hannover.se.pdfzensor.config;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

/** CLArgs represents the parsed command line arguments and provides simple aspect to specified options. */
@Command(name = "pdf-zensor", versionProvider = VersionProvider.class, separator = " ", mixinStandardHelpOptions = true,
		header = "PDF-Zensor",
		synopsisHeading = "%n@|bold,underline SYNOPSIS|@%n%n",
		descriptionHeading = "%n@|bold,underline DESCRIPTION|@%n",
		description = {"PDF-Zensor can be used to censor PDF-documents. As such it strips annotations and metadata as" +
					   "well as textual and graphical content from the pdf file."},
		parameterListHeading = "%n@|bold,underline PARAMETERS|@%n%n",
		optionListHeading = "%n@|bold,underline OPTIONS|@%n%n"
)
final class CLArgs {
	/** The input-file as it was specified. It's value <b>should not</b> be null. */
	@Nullable
	@CommandLine.Parameters(paramLabel = "\"in.pdf\"", arity = "1",
			description = {"Set the input pdf-file that should be censored. Required."})
	private File input = null;
	
	/** The output path. This may be a folder, a file or null. Null should be assigned if nothing else was specified. */
	@Option(names = {"-o", "--out"}, paramLabel = "\"out\"", arity = "1",
			description = {"The output file or path the censored file should be written to."})
	@Nullable
	private File output = null;
	
	/**
	 * The verbosity is given by how often -v was specified. If length is 0, verbosity is OFF. If null nothing was
	 * specified in the command line arguments.
	 */
	@Option(names = {"-v", "--verbose"}, arity = "0",
			description = {"Sets the logger's verbosity. Specify multiple -v options to increase verbosity."})
	@Nullable
	private boolean[] verbose = null;
	
	/**
	 * CLArgs' default constructor should be hidden to the public as {@link #fromStringArray(String...)} should be used
	 * to initialize a new instance.
	 *
	 * @see #fromStringArray(String...)
	 */
	@Contract(pure = true)
	private CLArgs() {}
	
	/**
	 * This method should be called to construct a new CLArgs instance from command line arguments.
	 *
	 * @param args the command-line arguments which will be parsed.
	 * @return an CLArgs object which contains all information about the parsed arguments.
	 * @throws NullPointerException     if the provided argument-array is null.
	 * @throws IllegalArgumentException if the provided argument-array is empty or contains null elements.
	 */
	@NotNull
	static CLArgs fromStringArray(@NotNull final String... args) {
		Validate.notEmpty(args);
		final var clArgs = new CLArgs();
		final var cmd = new CommandLine(clArgs);
		cmd.parseArgs(Validate.noNullElements(args));
		clArgs.validate();
		return clArgs;
	}
	
	/**
	 * Validates the current CLArgs instance. If it is not valid it should not be returned to the outside.
	 *
	 * @throws NullPointerException if input is null.
	 * @see #fromStringArray(String...)
	 */
	private void validate() {
		Objects.requireNonNull(input);
	}
	
	/**
	 * Returns input file given by the user.
	 *
	 * @return The input file as it was specified by the user.
	 */
	@Contract(pure = true)
	@NotNull
	final File getInput() {
		return input;
	}
	
	/**
	 * Returns output file given by the user
	 *
	 * @return The output file as it was specified by the user or null if none was specified.
	 */
	@Contract(pure = true)
	@Nullable
	final File getOutput() {
		return output;
	}
	
	/**
	 * Returns verbosity level given by the user.
	 *
	 * @return null or the level of logging verbosity if verbose was given in the arguments.
	 */
	@Contract(pure = true)
	@Nullable
	Level getVerbosity() {
		return verbose == null ? null : VERBOSITY_LEVELS[fitToArray(VERBOSITY_LEVELS, verbose.length)];
	}
}
