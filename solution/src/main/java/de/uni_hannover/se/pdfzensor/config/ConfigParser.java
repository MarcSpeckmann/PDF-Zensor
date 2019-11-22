package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

/** Stores all the necessary properties for censoring a pdf-file. */
final class ConfigParser {
	@Nullable
	private final File output;
	@Nullable
	private final Level verbose;
	
	/** An empty parser Object. Is used if the configuration file could not found. */
	private ConfigParser() {
		this(null, null);
	}
	
	/**
	 * Takes properties from a Json-String and sets the variables accordingly
	 *
	 * @param output  Output-File where the censored File will be stored in
	 * @param verbose Level of verbosity
	 */
	@JsonCreator()
	private ConfigParser(@Nullable @JsonProperty("output") final File output,
						 @Nullable @JsonProperty("verbose") final Object verbose) {
		this.output = output;
		this.verbose = verboseToLevel(verbose);
	}
	
	/**
	 * @param config The configuration file that wants to be parsed.
	 * @return An object which contains information about the parsed configuration file.
	 * @throws IOException If the configuration file couldn't be parsed.
	 */
	@Contract("null -> new")
	@NotNull
	static ConfigParser fromFile(@Nullable final File config) throws IOException {
		if (config == null) {
			// return empty config parser
			return new ConfigParser();
		}
		Validate.isTrue(config.isFile() && "json".equals(FileUtils.getFileExtension(config)),
						"config-file in ConfigParser.fromFile() is not a valid config-file!");
		return new ObjectMapper().readValue(config, ConfigParser.class);
	}
	
	@Nullable
	private Level verboseToLevel(@Nullable final Object verbose) {
		if (verbose instanceof String) // verbose as String
			return Level.getLevel(((String) verbose).toUpperCase()); // LEVEL in Uppercase
		else if (verbose instanceof Integer) { // verbose as Integer
			final Level[] logLevels = Logging.VERBOSITY_LEVELS;
			return logLevels[fitToArray(logLevels, (int) verbose)];
		}
		return null;
	}
	
	@Contract(pure = true)
	@Nullable
	File getOutput() {
		return this.output;
	}
	
	@Nullable
	Level getVerbosity() {
		return ObjectUtils.cloneIfPossible(this.verbose);
	}
	
}
