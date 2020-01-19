package de.uni_hannover.se.pdfzensor;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.Objects;

/**
 * Logging is a simple utility-class that provides only the {@link #getLogger()}, {@link #init(Level)} and {@link
 * #deinit()} method to the outside. Internally it is responsible for initializing and setting up the underlying logging
 * system. If the Log-Level of the Root-Logger should be set, {@link #init(Level)} has to be called with the appropriate
 * level <b>before</b> any call to {@link #getLogger()} is made.
 * <b>Not thread-safe</b>
 */
public final class Logging {
	
	/** The available logging levels ordered by their detail (ascending). */
	@NotNull
	@SuppressWarnings("squid:S2386") // we can disable it here as this is an enum array and should not be alterable
	public static final Level[] VERBOSITY_LEVELS =
			{Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL};
	/** The path to the log file and its name. */
	private static final String LOG_FILE = App.ROOT_DIR + "log.log";
	/** Log files with a file size (in KBs) meeting or exceeding this value will be overwritten. */
	private static final int LOG_FILE_SIZE_KB = 1024;
	/** Stores the context that is currently initialized. */
	@Nullable
	private static LoggerContext context = null;
	/** The level of the console logger. */
	@SuppressWarnings("FieldCanBeLocal")// the field is retrieved via introspection in the tests
	@Nullable
	@TestOnly
	private static Level consoleLevel = null;
	
	/**
	 * This constructor should not be called as no instance of {@link Logging} shall be created.
	 *
	 * @throws UnsupportedOperationException when being called
	 */
	@Contract(value = " -> fail", pure = true)
	private Logging() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Creates a new Logger using the fully qualified name of the calling class as the logger's name. Implemented like
	 * {@link LogManager#getLogger()} but also checks if Logging was initialized (and if not initializes it) before
	 * creating the logger. Initializes with the default value of {@link Level#OFF}.
	 *
	 * @return the created logger
	 * @throws UnsupportedOperationException if the calling class could not be determined
	 */
	@NotNull
	public static Logger getLogger() {
		init(Level.ERROR); //Won't do anything if the logging is initialized already
		//Taken from LogManager#getLogger
		return LogManager.getLogger(StackLocatorUtil.getCallerClass(2));
	}
	
	/**
	 * Responsible for initializing the logging and configuring it. Does nothing if a context is initialized already.
	 *
	 * @param consoleLogLevel Levels less specific than {@code consoleLogLevel} will be filtered from console logging.
	 */
	public static void init(@NotNull final Level consoleLogLevel) {
		if (context != null) return;
		var builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel(Level.ERROR)
			   .setConfigurationName("DefaultConfig");
		
		var consoleAppender = createConsoleAppender(builder, "ConsoleAppender", consoleLogLevel);
		var logFileAppender = createLogFileAppender(builder, "LogFileAppender", Level.ALL);
		setRootLogger(builder, consoleAppender, logFileAppender);
		context = Configurator.initialize(builder.build());
	}
	
	/** Deinitializes the current logging-context. Does nothing if none is initialized. */
	public static void deinit() {
		Configurator.shutdown(context);
		context = null;
	}
	
	/**
	 * Creates a new console appender of the specified name using the provided configuration builder. The console
	 * appender is created with the given log level and a PatternLayout set to the pattern {@code %d [%t] %-5level:
	 * %msg%n%throwable}.
	 * <br>
	 * The appender is automatically added to the build-configuration.
	 *
	 * @param builder  the builder that should be used to create the appender
	 * @param name     the name of the created console appender
	 * @param logLevel the log level that should be assigned to the console appender
	 * @return the created console appender
	 */
	@SuppressWarnings("SameParameterValue")
	// if we decided to create more appenders with this code they should not be called the same
	@NotNull
	private static AppenderComponentBuilder createConsoleAppender(
			@NotNull final ConfigurationBuilder<BuiltConfiguration> builder, @NotNull final String name,
			@NotNull final Level logLevel) {
		Objects.requireNonNull(builder);
		Objects.requireNonNull(name);
		Objects.requireNonNull(logLevel);
		consoleLevel = logLevel;
		var appender = builder.newAppender(name, "CONSOLE")
							  .add(builder.newFilter("ThresholdFilter", Result.ACCEPT, Result.DENY)
										  .addAttribute("level", consoleLevel))
							  .add(builder.newLayout("PatternLayout")
										  .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
		builder.add(appender);
		return appender;
	}
	
	/**
	 * Creates a new rolling file appender of the specified name using the provided configuration builder. The rolling
	 * file appender will log events more specific or equal to the given log level and append them to the log file.
	 * Events will be logged with a PatternLayout set to the pattern {@code [%4sn] %d - [%t] [%.32c{3}] %-5level:
	 * %msg%n%throwable}.
	 * <br>
	 * Should a previously existing log file's size (in KBs) meet or exceed {@link #LOG_FILE_SIZE_KB} then a backup of
	 * that file will be created and the log file will be overwritten. Other already existing backups in that directory
	 * will be overwritten by the new backup.
	 * <br>
	 * The appender is automatically added to the build-configuration.
	 *
	 * @param builder  the builder that should be used to create the appender
	 * @param name     the name of the created rolling file appender
	 * @param logLevel the log level that should be assigned to the rolling file appender
	 * @return the created rolling file appender
	 */
	@SuppressWarnings("SameParameterValue")
	@NotNull
	private static AppenderComponentBuilder createLogFileAppender(
			@NotNull final ConfigurationBuilder<BuiltConfiguration> builder, @NotNull final String name,
			@NotNull final Level logLevel) {
		Objects.requireNonNull(builder);
		Objects.requireNonNull(name);
		Objects.requireNonNull(logLevel);
		var appender = builder.newAppender(name, "ROLLINGFILE")
							  .add(builder.newFilter("ThresholdFilter", Result.ACCEPT, Result.DENY)
										  .addAttribute("level", logLevel))
							  .addAttribute("fileName", LOG_FILE)
							  .addAttribute("filePattern", FilenameUtils.removeExtension(LOG_FILE) + ".bak")
							  .addComponent(builder.newComponent("Policies")
												   .addComponent(builder.newComponent("OnStartupTriggeringPolicy")
																		.addAttribute("minSize",
																					  LOG_FILE_SIZE_KB * 1024)))
							  .add(builder.newLayout("PatternLayout")
										  .addAttribute("header", "Log-file for PDF-Zensor. Created: %d%n%n")
										  .addAttribute("footer", "Logging for PDF-Zensor finished (%d)%n%n%n")
										  .addAttribute("pattern",
														"[%4sn] %d - [%t] [%.32c{3}] %-5level: %msg%n%throwable"));
		builder.add(appender);
		return appender;
	}
	
	/**
	 * Creates a new root logger using the specified built-configuration. The created root logger is initialized to
	 * report to the log level {@link Level#ALL} and the passed appenders are added to it. The root-logger is
	 * automatically added to the build-configuration.
	 * <br>
	 * The root logger is assigned the level {@link Level#ALL} so it reports all events to its appenders, which can then
	 * filter them separately.
	 *
	 * @param builder                   the build-configuration that should be modified
	 * @param appenderComponentBuilders the appenders that should be assigned to the root logger
	 * @return the created root logger
	 */
	@SuppressWarnings("UnusedReturnValue")
	// return value is unused, for possible future API reasons it still exists though
	@NotNull
	private static RootLoggerComponentBuilder setRootLogger(
			@NotNull final ConfigurationBuilder<BuiltConfiguration> builder,
			@NotNull final AppenderComponentBuilder... appenderComponentBuilders) {
		Objects.requireNonNull(builder);
		Objects.requireNonNull(appenderComponentBuilders);
		var root = builder.newRootLogger(Level.ALL);
		for (var appender : appenderComponentBuilders)
			root.add(builder.newAppenderRef(appender.getName()));
		builder.add(root);
		return root;
	}
}