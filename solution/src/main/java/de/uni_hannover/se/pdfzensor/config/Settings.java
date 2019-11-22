package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
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
import java.util.regex.Pattern;

/**
 * The class which holds all the settings from the parsed configuration file and commandline arguments, validated and
 * prioritized correctly.
 */
public final class Settings {
	
	private static final String SIX_DIGIT_HEX_PATTERN = "(?i)^(0x|#)[0-9a-f]{6}$";
	private static final String THREE_DIGIT_HEX_PATTERN = "(?i)^(0x|#)[0-9a-f]{3}$";
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
	 * @param args The commandline arguments.
	 * @throws IOException If the configuration file could not be parsed.
	 */
	public Settings(@NotNull final String[] args) throws IOException {
		final var cla = CLArgs.fromStringArray(args);
		final var config = getDefaultConfig();
		final var cp = ConfigParser.fromFile(config);
		final var verbose = ObjectUtils.firstNonNull(cla.getVerbosity(), cp.getVerbosity(), Level.OFF);
		Logging.init(verbose);
		
		input = cla.getInput();
		output = ObjectUtils.firstNonNull(checkOutput(cla.getOutput()), checkOutput(cp.getOutput()), getDefaultOutput(
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
	
	/**
	 * @param hexCode A string containing a hexadecimal color code with 3 digits
	 * @return A string containing a hexadecimal color code with 6 digits
	 */
	@NotNull
	private static String transformToSixDigit(@NotNull final String hexCode) {
		Validate.matchesPattern(Objects.requireNonNull(hexCode), THREE_DIGIT_HEX_PATTERN,"Must be a valid hex color code.");
		return hexCode.replaceFirst("(?i)0x", "#")
				.replaceAll("(?i)[0-9A-F]", "$0$0");
	}
	
	/**
	 * @param hexCode A string containing a hexadecimal color code.
	 * @return The awt.Color of the hexadecimal color code or null, if the given string was null.
	 */
	@Contract("null -> null")
	@Nullable
	static Color getColorOrNull(@Nullable final String hexCode) {
		if (hexCode == null) return null;
		var copy = hexCode;
		if (hexCode.matches(THREE_DIGIT_HEX_PATTERN)) copy = transformToSixDigit(hexCode);
		Validate.matchesPattern(copy, SIX_DIGIT_HEX_PATTERN, "Must be a valid hex color code.");
		return Color.decode(copy);
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
		if (out.isDirectory()) return getDefaultOutput(out.getPath());
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
		final var in = input.getName();
		final var inName = FilenameUtils.removeExtension(in);
		return new File(Objects.requireNonNull(path) + File.separatorChar + inName + "_cens.pdf").getAbsoluteFile();
	}
	
	/**
	 * @return The absolute default configuration file or null if it did not exist.
	 */
	@Nullable
	private File getDefaultConfig() {
		// TODO: set proper default config
		final var c = new File("config.json");
		return Optional.of(c)
				.filter(File::isFile)
				.filter(f -> "json".equals(FileUtils.getFileExtension(f)))
				.map(File::getAbsoluteFile)
				.orElse(null);
	}
	
	/**
	 * @param color The color to convert into a hexadecimal color code.
	 * @return The hexadecimal color code representing the given color.
	 */
	@NotNull
	@Contract("_ -> !null")
	public static String colorToString(@Nullable Color color) {
		return Optional.ofNullable(color)
				.map(c -> String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()))
				.orElse("null");
	}
}