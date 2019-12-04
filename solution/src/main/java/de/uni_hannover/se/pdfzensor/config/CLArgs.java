package de.uni_hannover.se.pdfzensor.config;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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
	@SuppressWarnings("CanBeFinal") // it cannot be final as it will be set by picoCLI
	@Nullable
	@Parameters(paramLabel = "\"in.pdf\"", arity = "1",
			description = {"Set the input pdf-file that should be censored. Required."})
	private File input = null;
	
	/** The output path. This may be a folder, a file or null. Null should be assigned if nothing else was specified. */
	@SuppressWarnings("CanBeFinal") // it cannot be final as it will be set by picoCLI
	@Option(names = {"-o", "--out"}, paramLabel = "\"out\"", arity = "1",
			description = {"The output file or path the censored file should be written to."})
	@Nullable
	private File output = null;
	
	/**
	 * The verbosity is given by how often -v was specified. If length is 0, verbosity is OFF. If null nothing was
	 * specified in the command line arguments.
	 */
	@SuppressWarnings("CanBeFinal") // it cannot be final as it will be set by picoCLI
	@Option(names = {"-v", "--verbose"}, arity = "0",
			description = {"Sets the logger's verbosity. Specify multiple -v options to increase verbosity."})
	@Nullable
	private boolean[] verbose = null;
	
	/** Container for the mode. */
	@ArgGroup()
	@NotNull
	private final MarkedOptions modes = new MarkedOptions();
	
	/** Helper class to allow for exclusivity between the marked and unmarked mode. */
	private static final class MarkedOptions {
		/** A boolean indicating that the desired censor mode is {@link Mode#MARKED}. */
		@Option(names = {"-m", "--censor-marked"}, arity = "0", required = true, description = {"Include only marked segments when censoring."})
		private boolean marked = false;
		
		/** A boolean indicating that the desired censor mode is {@link Mode#UNMARKED}. */
		@Option(names = {"-u", "--censor-unmarked"}, arity = "0", required = true, description = {"Exclude all marked segments when censoring."})
		private boolean unmarked = false;
	}
	
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
		return Objects.requireNonNull(input);
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
	
	/**
	 * Converts the boolean into the respective {@link Mode}. <b>Note:</b> both booleans being false does not result in
	 * {@link Mode#ALL} but in <code>null</code>. This is the case to still allow for the configuration to set the
	 * {@link Mode} ({@link Mode#ALL} is the default value of the setting, not the default value for the {@link CLArgs}
	 * argument).
	 *
	 * @return null of the Mode representing the booleans specified by the arguments.
	 */
	@Contract(pure = true)
	@Nullable
	Mode getMode() {
		Mode desiredMode = null;
		if (modes.marked) desiredMode = Mode.MARKED;
		else if (modes.unmarked) desiredMode = Mode.UNMARKED;
		return desiredMode;
	}
}
