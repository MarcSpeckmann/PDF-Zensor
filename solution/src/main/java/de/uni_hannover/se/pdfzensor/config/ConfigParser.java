package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;

public class ConfigParser {
    @Nullable
    private final File outPut;
    @Nullable
    private final Level verbose;
    //@Nullable
    //private final Color linkColor;
    //@Nullable
    //private final Color regExColor;
    //@Nullable
    //private final Color textColor;
    //@Nullable
    //private final String defaultImagePath;
    //@Nullable
    //private final Color[] defaultColors;
    /**
     * An empty parser Object. Is used if the configuration file could not found.
     */
    private ConfigParser() {
        this.outPut = null;
        this.verbose = null;
    }

    @JsonCreator()
    private ConfigParser(@Nullable @JsonProperty("outPut") final File outPut,
                        @Nullable @JsonProperty("verbose") final Level verbose){
        this.outPut = outPut;
        this.verbose = verbose;
    }

    /**
     * @param config The configuration file that wants to be parsed.
     * @return An object which contains information about the parsed configuration file.
     * @throws IOException If the configuration file couldn't be parsed.
     */
    @Contract("null -> new")
    @NotNull
    public ConfigParser fromFile(@Nullable final File config) throws IOException {
        if(config == null) {
            // return empty config parser
            return new ConfigParser();
        }
        Validate.isTrue(config.isFile() && "json".equals(FileUtils.getFileExtension(config)), "Bullshit file");
        return new ObjectMapper().readValue(config, ConfigParser.class);
    }
    @Nullable
    private Level verboseToLevel(@Nullable final Object verbose) {
        if(verbose instanceof String) // verbose as String
            return Level.getLevel(((String) verbose).toUpperCase()); // LEVEL in Uppercase
        else if(verbose instanceof Integer) { // verbose as Integer
            final Level[] LOG_LEVELS = new Level[]{Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL}; // Create Array with all Levels
            return LOG_LEVELS[smallerOrBigger((Integer)verbose, LOG_LEVELS.length)];
        }
        return null;
    }
    @NotNull
    private Integer smallerOrBigger(int verbose, int arrayLength) {
        return Math.min(Math.max(verbose, 0), arrayLength - 1);
    }
    @Nullable
    public File getOutput() {
        return this.outPut;
    }
    @Nullable
    public Level getVerbosity() {
        return ObjectUtils.cloneIfPossible(this.verbose);
    }

}
