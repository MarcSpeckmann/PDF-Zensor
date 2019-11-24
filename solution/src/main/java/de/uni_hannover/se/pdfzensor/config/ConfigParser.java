package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	
	/** An empty parser Object. Is used if the configuration file could not be found. */
	private ConfigParser() {
		this(null, null);
	}
	
	/**
	 * Takes properties from a Json-String and sets the variables accordingly.
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
	 * @throws IOException              If the configuration file couldn't be found.
	 * @throws IllegalArgumentException If the passed config is not a file or does not have the suffix .json or does not
	 *                                  contain a valid JSON string.
	 */
	@Contract("null -> new")
	@NotNull
	static ConfigParser fromFile(@Nullable final File config) throws IOException {
		if (config == null)
			return new ConfigParser();
		Validate.isTrue(config.isFile() && "json".equals(FileUtils.getFileExtension(config)),
						"The configuration file is not a valid JSON-file.");
		try {
			return new ObjectMapper().readValue(config, ConfigParser.class);
		} catch (JsonParseException | JsonMappingException e) {
			throw new IllegalArgumentException("The configuration file does not contain a valid JSON-string.");
		}
	}
	
	@Nullable
	private static Level verboseToLevel(@Nullable final Object verbose) {
		if (verbose instanceof String)
			return Level.getLevel(((String) verbose).toUpperCase());
		else if (verbose instanceof Integer)
			return Logging.VERBOSITY_LEVELS[fitToArray(Logging.VERBOSITY_LEVELS, (int) verbose)];
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