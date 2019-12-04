package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static de.uni_hannover.se.pdfzensor.utils.Utils.colorToString;

/**
 * The Settings class constitutes an abstraction and unification of the configuration file ({@link Config}) and the
 * command line arguments ({@link CLArgs}). Instead of accessing each configuration entity separately, they should be
 * unified via settings and passed to the outside from there. Upon construction settings passes the command line
 * arguments to {@link CLArgs}, loads the corresponding configuration file via {@link Config} and takes their parameters
 * according to the following rules:<br>
 * <ol>
 *     <li><b>CLArgs</b> overrides <b>Config</b> overrides <b>Default values</b><br></li>
 *     <li>No value will be set to null (for this purpose the default values exist)<br></li>
 *     <li><b>input</b> may only be specified in the CLArgs</li>
 * </ol>
 */
public final class Settings {
	/** The color that text should be censored in if it does not match any other specified expression. */
	static final Color DEFAULT_CENSOR_COLOR = Color.BLACK;
	/** The color links should be censored in if nothing else was specified. */
	private static final Color DEFAULT_LINK_COLOR = Color.BLUE;
	
	/** The path at which the pdf-file that should be censored is located. */
	@NotNull
	private final File input;
	/** The path into which the censored pdf-file should be written. */
	@NotNull
	private final File output;
	/** The color with which to censor links. */
	@NotNull
	private final Color linkColor;
	/** The mode to use for censoring. See {@link Mode} for more information. */
	@NotNull
	private final Mode mode;
	/**
	 * A set of regex-color-tuples to identify with what color to censor which text. Should at least contain the tuple
	 * (".", {@link #DEFAULT_CENSOR_COLOR}).
	 */
	@NotNull
	private final Expression[] expressions;
	
	/**
	 * Constructs the settings object from the configuration file and the commandline arguments.
	 *
	 * @param configPath the path to the config file (SHOULD BE REMOVED LATER)
	 * @param args       The commandline arguments.
	 * @throws IOException If the configuration file could not be parsed.
	 */
	public Settings(@Nullable String configPath, @NotNull final String... args) throws IOException {
		final var clArgs = CLArgs.fromStringArray(args);
		final var config = getConfig(configPath);
		final var verbose = ObjectUtils.firstNonNull(clArgs.getVerbosity(), config.getVerbosity(), Level.OFF);
		Logging.init(verbose);
		
		input = clArgs.getInput();
		output = checkOutput(
				ObjectUtils
						.firstNonNull(clArgs.getOutput(), config.getOutput(), input.getAbsoluteFile().getParentFile()));
		linkColor = DEFAULT_LINK_COLOR;
		mode = ObjectUtils.firstNonNull(clArgs.getMode(), Mode.ALL);
		expressions = new Expression[]{new Expression(".", DEFAULT_CENSOR_COLOR)};
		
		//Dump to log
		final var logger = Logging.getLogger();
		logger.log(Level.DEBUG, "Finished parsing the settings:");
		logger.log(Level.DEBUG, "\tInput-file: {}", input);
		logger.log(Level.DEBUG, "\tConfig-file: {}", configPath);
		logger.log(Level.DEBUG, "\tOutput-file: {}", output);
		logger.log(Level.DEBUG, "\tLogger verbosity: {}", verbose);
		logger.log(Level.DEBUG, "\tLink-Color: {}", () -> colorToString(linkColor));
		logger.log(Level.DEBUG, "\tExpressions");
		for (var exp : expressions)
			logger.log(Level.DEBUG, "\t\t{}", exp);
	}
	
	/**
	 * Tries to load the configuration file from the provided path. If the path is <code>null</code> the empty
	 * configuration (everything <code>null</code>) will be used.
	 *
	 * @return The configuration file that was loaded from the specified path.
	 * @throws IOException if the configuration file could not be found or read.
	 */
	@NotNull
	private static Config getConfig(@Nullable String configPath) throws IOException {
		return Config.fromFile(Optional.ofNullable(configPath).map(File::new).orElse(null));
	}
	
	/** Returns the input file as it was specified in the command-line arguments. */
	@NotNull
	@Contract(pure = true)
	public File getInput() {
		return input;
	}
	
	/** Returns the output file as it was specified in the command-line arguments and config. */
	@NotNull
	@Contract(pure = true)
	public File getOutput() {
		return output;
	}
	
	/** Returns the color links should be censored in as it was specified in the command-line arguments and config. */
	@NotNull
	@Contract(pure = true)
	public Color getLinkColor() {
		return linkColor;
	}
	
	/** Returns the censor mode which should be used when censoring PDF-files. */
	@NotNull
	@Contract(pure = true)
	public Mode getMode() {
		return mode;
	}
	
	/** Returns the expressions as they were specified in the command-line arguments and config. */
	@NotNull
	@Contract(pure = true)
	public Expression[] getExpressions() {
		return ObjectUtils.cloneIfPossible(expressions);
	}
	
	/**
	 * Validates the provided output file. If it is a file it itself will be returned. If it is a folder (or does not
	 * exist and has no suffix) a path to <code>{out}/{input name}_cens.pdf</code> is returned.
	 *
	 * @param out The output file that should be validated. May not be null.
	 * @return the validated output file the censored PDF should be written into.
	 * @throws NullPointerException if out is null
	 * @see #getDefaultOutput(String)
	 */
	@NotNull
	private File checkOutput(@NotNull final File out) {
		var result = Objects.requireNonNull(out);
		if (!out.isFile() && StringUtils.isEmpty(FileUtils.getFileExtension(out)))
			result = getDefaultOutput(out.getPath());
		return result;
	}
	
	/**
	 * Will return the absolute default filename in directory {@param path}. The default filename is {@code
	 * in_cens.pdf}, where {@code in} is the name of the input file.
	 *
	 * @param path The path in which the output file with default naming should be located.
	 * @return The absolute default output file.
	 */
	@NotNull
	private File getDefaultOutput(@NotNull final String path) {
		final var inName = FilenameUtils.removeExtension(input.getName());
		return new File(Objects.requireNonNull(path) + File.separatorChar + inName + "_cens.pdf").getAbsoluteFile();
	}
}