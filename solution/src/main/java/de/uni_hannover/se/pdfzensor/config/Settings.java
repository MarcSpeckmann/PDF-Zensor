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
 * The class which holds all the settings from the parsed configuration file and commandline arguments, validated and
 * prioritized correctly.
 */
public final class Settings {
	private static final String DEFAULT_CENSOR_COLOR = "#000000";
	
	@NotNull
	private final File input;
	@NotNull
	private final File output;
	@NotNull
	private final Color linkColor;
	@NotNull
	private final Expression[] expressions;
	
	/**
	 * Constructs the settings object from the configuration file and the commandline arguments. The settings of this
	 * object will be logged at {@link Level#DEBUG}.
	 *
	 * @param configPath the path to the config file (SHOULD BE REMOVED LATER)
	 * @param args The commandline arguments.
	 * @throws IOException If the configuration file could not be parsed.
	 */
	public Settings(@NotNull String configPath,@NotNull final String... args) throws IOException {
		final var clargs = CLArgs.fromStringArray(args);
		final var config = getDefaultConfig(configPath);
		final var configParser = Config.fromFile(config);
		final var verbose = ObjectUtils.firstNonNull(clargs.getVerbosity(), configParser.getVerbosity(), Level.OFF);
		Logging.init(verbose);
		
		input = clargs.getInput();
		output = ObjectUtils.firstNonNull(checkOutput(clargs.getOutput()), checkOutput(configParser.getOutput()), getDefaultOutput(
				input.getParent()));
		linkColor = Color.BLUE;
		expressions = new Expression[]{new Expression(".", DEFAULT_CENSOR_COLOR)};
		
		//Dump to log
		final var logger = Logging.getLogger();
		logger.log(Level.DEBUG, "Finished parsing the settings:");
		logger.log(Level.DEBUG, "\tInput-file: {}", input);
		logger.log(Level.DEBUG, "\tConfig-file: {}", config);
		logger.log(Level.DEBUG, "\tOutput-file: {}", output);
		logger.log(Level.DEBUG, "\tLogger verbosity: {}", verbose);
		logger.log(Level.DEBUG, "\tLink-Color: {}", () -> colorToString(linkColor));
		logger.log(Level.DEBUG, "\tDefined-Expressions");
		for (var exp : expressions)
			logger.log(Level.DEBUG, "\t\t{}", exp);
	}
	
	@NotNull
	@Contract(pure = true)
	public File getInput() {
		return input;
	}
	
	@NotNull
	@Contract(pure = true)
	public File getOutput() {
		return output;
	}
	
	@NotNull
	@Contract(pure = true)
	public Color getLinkColor() {
		return linkColor;
	}
	
	@NotNull
	@Contract(pure = true)
	public Expression[] getExpressions() {
		return ObjectUtils.cloneIfPossible(expressions);
	}
	
	/**
	 * @param out The output file which will be validated.
	 * @return null if the output to check was null or was invalid, a valid output file otherwise.
	 */
	@Contract("null -> null")
	@Nullable
	private File checkOutput(@Nullable final File out) {
		if (out == null) return null;
		if ("pdf".equals(FileUtils.getFileExtension(out))) return out.getAbsoluteFile();
		if (out.isDirectory() || StringUtils.isEmpty(FileUtils.getFileExtension(out))) return getDefaultOutput(out.getPath());
		return null;
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
	
	/**
	 * @return The absolute default configuration file or null if it did not exist.
	 */
	@Nullable
	private File getDefaultConfig(String configPath) {
		final var c = new File(configPath);
		return Optional.of(c)
				.filter(File::isFile)
				.filter(f -> "json".equals(FileUtils.getFileExtension(f)))
				.map(File::getAbsoluteFile)
				.orElse(null);
	}
}