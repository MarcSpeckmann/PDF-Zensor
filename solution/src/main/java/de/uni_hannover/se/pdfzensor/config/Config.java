package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import de.uni_hannover.se.pdfzensor.App;
import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.utils.Utils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static de.uni_hannover.se.pdfzensor.utils.Utils.*;

/** An in-memory representation of a configuration-file. */
final class Config {
	/** The path to the default config file and its name. */
	private static final File DEFAULT_CONFIG_FILE = new File(App.ROOT_DIR + "defaultConfig.json");
	/** A default output-path. Stores the folder censored PDF-files should be written to. */
	@Nullable
	private final File output;
	/** The logging verbosity. Any log-message with a less specific level will not be logged. */
	@Nullable
	private final Level verbose;
	/** The censor mode. See {@link Mode} for more information. */
	@Nullable
	private final Mode mode;
	/** Whether text censor bars may be drawn atop of censored images. */
	private final boolean intersectImages;
	/** Whether links should be distinguishable from normal text by their censor color or be considered normal text. */
	private final boolean distinguishLinks;
	/** An array of {@link Expression}s to use when censoring. */
	@Nullable
	private final Expression[] expressions;
	/**
	 * A list of default colors which will be used to assign a color to regular expressions which were not given a color
	 * by the user.
	 */
	@Nullable
	private final Color[] defaultColors;
	
	/**
	 * The default constructor creates an empty ConfigurationParser. That is: all values are set to null (or their
	 * respective default value in case of primitive types).
	 */
	private Config() {
		this(null, null, null, null, null, null, null);
	}
	
	/**
	 * Initializes a new in-memory configuration from the provided values.
	 *
	 * @param output           the file where the censored file should be stored. Null if not specified.
	 * @param verbose          the level of logging verbosity (encoded as a String or int). Null if not specified.
	 * @param mode             the mode to use when censoring as a string. Null if not specified.
	 * @param intersectImages  the boolean denoting if text censor bars may overlap censored images. Null if not
	 *                         specified.
	 * @param distinguishLinks the boolean denoting if links should be distinguished or treated the same as normal text.
	 *                         Null if not specified.
	 * @param expressions      the expressions specified in the configuration file.
	 * @param defaultColors    a string array containing hexadecimal color codes. Null if not specified.
	 * @see #objectToLevel(Object)
	 * @see Mode#stringToMode(String)
	 */
	@JsonCreator()
	private Config(@Nullable @JsonProperty("output") final File output,
				   @Nullable @JsonProperty("verbose") final Object verbose,
				   @Nullable @JsonProperty("censor") final String mode,
				   @Nullable @JsonProperty("intersectImages") final Boolean intersectImages,
				   @Nullable @JsonProperty("links") final Boolean distinguishLinks,
				   @Nullable @JsonProperty("expressions") final Expression[] expressions,
				   @Nullable @JsonProperty("defaultColors") final String[] defaultColors) {
		this.output = output;
		this.verbose = objectToLevel(verbose);
		this.mode = Mode.stringToMode(mode);
		this.intersectImages = Optional.ofNullable(intersectImages).orElse(false);
		this.distinguishLinks = Optional.ofNullable(distinguishLinks).orElse(false);
		this.expressions = expressions;
		this.defaultColors = hexArrayToColorArray(defaultColors);
	}
	
	/**
	 * Reads the provided JSON-formatted config-file and stores its data into a new instance of {@link Config}.
	 *
	 * @param config The configuration file that should be parsed. If null the default configuration (everything null)
	 *               will be returned.
	 * @return An object which contains information about the parsed configuration file.
	 * @throws IllegalArgumentException If the passed config is not a file or does not contain a valid JSON string (for
	 *                                  this software).
	 */
	@Contract("_ -> new")
	@NotNull
	static Config fromFile(@Nullable final File config) {
		if (config == null)
			return new Config();
		Validate.isTrue(config.isFile(), "The given configuration file (%s) does not exist.", config.getAbsolutePath());
		try {
			return new ObjectMapper().readValue(config, Config.class);
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"The configuration file could not be parsed because it is not a JSON string valid for this software.");
		}
	}
	
	/**
	 * Writes the default configuration file. The configuration will contain a representation of the following entries:
	 * <ul>
	 *     <li>output file: null</li>
	 *     <li>verbosity level: {@link Level#WARN}</li>
	 *     <li>censor mode: {@link Mode#ALL}</li>
	 *     <li>intersect images: {@code false}</li>
	 *     <li>distinguish links: {@code false}</li>
	 *     <li>expressions: [regex: "."; color: {@link Settings#DEFAULT_CENSOR_COLOR}]</li>
	 *     <li>default colors: {@link Settings#DEFAULT_COLORS}</li>
	 * </ul>
	 *
	 * @return true if the file was successfully written, false otherwise.
	 * @see Expression
	 */
	private static boolean createDefaultConfigFile() {
		Objects.requireNonNull(DEFAULT_CONFIG_FILE, "The file to write the default configuration to may not be null.");
		final var factory = JsonNodeFactory.instance;
		final var configNode = factory.objectNode();
		final var expressions = factory.arrayNode().addObject()
									   .put("regex", ".")
									   .put("color", colorToString(Settings.DEFAULT_CENSOR_COLOR));
		configNode.putNull("output")
				  .put("verbose", "WARN")
				  .put("censor", "ALL")
				  .put("intersectImages", false)
				  .put("links", false)
				  .putArray("expressions").add(expressions);
		final var defaultColors = configNode.putArray("defaultColors");
		for (var color : Settings.DEFAULT_COLORS)
			defaultColors.add(colorToString(color));
		try {
			new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(DEFAULT_CONFIG_FILE, configNode);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Returns the default configuration file. If the file does not exist then an attempt is made to create it. On
	 * successful creation or if the file already existed (and if overwrite is true, was overwritten successfully) then
	 * the file is returned, otherwise null is returned.
	 *
	 * @param overwrite sets whether the already existing default configuration should be overwritten or not. This
	 *                  argument does not have any effect if no configuration file exists.
	 * @return the default configuration file if it existed or was created or overwritten successfully, null otherwise.
	 * @see #createDefaultConfigFile()
	 */
	@Nullable
	static File getDefaultConfigFile(boolean overwrite) {
		var success = true;
		if (!DEFAULT_CONFIG_FILE.isFile() || overwrite)
			success = createDefaultConfigFile();
		return success ? DEFAULT_CONFIG_FILE : null;
	}
	
	/**
	 * Translates the given object into a {@link Level} or null if it was not possible. {@code verbosity} may be a
	 * string or an int. If it is an int it indexes into {@link Logging#VERBOSITY_LEVELS}. If it is too small OFF is
	 * returned, if it is too large ALL is returned. If the given object is neither a string or an int null is
	 * returned.
	 *
	 * @param verbose the string or integer that should be translated into a level.
	 * @return the level corresponding to the provided verbosity or null if it could not be parsed.
	 */
	@Nullable
	@Contract("null -> null")
	private static Level objectToLevel(@Nullable final Object verbose) {
		Level lvl = null;
		if (verbose instanceof String)
			lvl = Level.getLevel(((String) verbose).toUpperCase());
		else if (verbose instanceof Integer)
			lvl = Logging.VERBOSITY_LEVELS[fitToArray(Logging.VERBOSITY_LEVELS, (int) verbose)];
		return lvl;
	}
	
	/**
	 * Converts an array containing hexadecimal representation of colors into a color array or null if the array was
	 * empty or not present.
	 *
	 * @param hex The String array containing the colors in hexadecimal representation.
	 * @return A color array representing the given hexadecimal color codes or null.
	 */
	@Nullable
	@Contract("null -> null")
	private Color[] hexArrayToColorArray(@Nullable final String[] hex) {
		if (hex == null || hex.length == 0)
			return null;
		// filter should never be necessary
		var stream = Arrays.stream(hex).map(Utils::getColorOrNull).filter(Objects::nonNull);
		return stream.toArray(Color[]::new);
	}
	
	/**
	 * Returns the output-path as it was specified in the loaded config.
	 *
	 * @return The output-path as it was specified in the loaded config. Or null if none was specified.
	 */
	@Contract(pure = true)
	@Nullable
	File getOutput() {
		return this.output;
	}
	
	/**
	 * Returns the verbosity as it was specified in the loaded config.
	 *
	 * @return The verbosity as it was specified in the loaded config. Or null if none was specified.
	 */
	@SuppressWarnings("ReturnPrivateMutableField")// may be suppressed as the Level can not be overwritten.
	@Contract(pure = true)
	@Nullable
	Level getVerbosity() {
		return this.verbose;
	}
	
	/**
	 * Returns the censor mode as specified in the loaded config.
	 *
	 * @return The censor mode as specified in the loaded config. Or null if none was specified.
	 */
	@Contract(pure = true)
	@Nullable
	Mode getMode() {
		return this.mode;
	}
	
	/**
	 * Returns the behavior when censor bars overlap with images as specified in the loaded config.
	 *
	 * @return The desired behavior for overlapping censor bars and images as specified in the loaded config. True if
	 * overlapping is allowed, false otherwise.
	 */
	@Contract(pure = true)
	boolean getIntersectImages() {
		return this.intersectImages;
	}
	
	/**
	 * Returns whether links should be distinguishable from normal text by their censor color or be considered normal
	 * text instead as specified in the loaded config.
	 *
	 * @return True if a distinction of links and normal text is desired, false otherwise.
	 */
	@Contract(pure = true)
	boolean distinguishLinks() {
		return this.distinguishLinks;
	}
	
	/**
	 * Returns the array of expressions as they were specified in the loaded config.
	 *
	 * @return An array containing the expressions as specified in the loaded config.
	 */
	@Contract(pure = true)
	@Nullable
	Expression[] getExpressions() {
		return this.expressions;
	}
	
	/**
	 * Returns the default colors as they were specified (in hexadecimal representation) in the loaded config.
	 *
	 * @return A color array containing the default colors as specified in the loaded config.
	 */
	@Contract(pure = true)
	@Nullable
	Color[] getDefaultColors() {
		return this.defaultColors;
	}
}