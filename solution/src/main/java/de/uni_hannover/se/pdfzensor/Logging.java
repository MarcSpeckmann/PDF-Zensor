package de.uni_hannover.se.pdfzensor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
	public static final Level[] VERBOSITY_LEVELS = {Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL};
	
	/** Stores the context that is currently initialized. */
	private static LoggerContext context = null;
	
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
		init(Level.OFF); //Won't do anything if the logging is initialized already
		//Taken from LogManager#getLogger
		return LogManager.getLogger(StackLocatorUtil.getCallerClass(2));
	}
	
	/**
	 * Responsible for initializing the logging and configuring it. Does nothing if a context is initialized already.
	 *
	 * @param rootLevel Levels less specific than {@code rootLevel} will be filtered.
	 */
	public static void init(@NotNull final Level rootLevel) {
		if (context != null) return;
		Objects.requireNonNull(rootLevel);
		var builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel(Level.ERROR)
			   .setConfigurationName("DefaultConfig")
			   .add(builder.newFilter("ThresholdFilter", Result.ACCEPT, Result.NEUTRAL)
						   .addAttribute("level", rootLevel));
		
		var consoleAppender = createConsoleAppender(builder, "ConsoleAppender");
		setRootLogger(builder, rootLevel, consoleAppender);
		context = Configurator.initialize(builder.build());
	}
	
	/** Deinitializes the current logging-context. Does nothing if none is initialized. */
	public static void deinit() {
		Configurator.shutdown(context);
		context = null;
	}
	
	/**
	 * Creates a new console appender of the specified name using the provided configuration builder. The console
	 * appender is created with a PatternLayout set to the pattern {@code %d [%t] %-5level: %msg%n%throwable}.<br> The
	 * appender is automatically added to the build-configuration.
	 *
	 * @param builder the builder that should be used to create the appender
	 * @param name    the name of the created console appender
	 * @return the created console appender
	 */
	@SuppressWarnings("SameParameterValue")
	// if we decided to create more appenders with this code they should not be called the same
	@NotNull
	private static AppenderComponentBuilder createConsoleAppender(
			@NotNull final ConfigurationBuilder<BuiltConfiguration> builder, @NotNull final String name) {
		Objects.requireNonNull(builder);
		Objects.requireNonNull(name);
		var appender = builder.newAppender(name, "CONSOLE")
							  .addAttribute("target", Target.SYSTEM_OUT)
							  .add(builder.newLayout("PatternLayout")
										  .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
		builder.add(appender);
		return appender;
	}
	
	/**
	 * Creates a new root logger using the specified built-configuration. The created root logger is initialized to
	 * report the given log-level and the passed appenders are added to it. The root-logger is automatically added to
	 * the build-configuration.
	 *
	 * @param builder                   the build-configuration that should be modified
	 * @param logLevel                  the logging level that should be assigned to the root logger
	 * @param appenderComponentBuilders the appenders that should be assigned to the root logger
	 * @return the created root logger
	 */
	@SuppressWarnings("UnusedReturnValue")
	// return value is unused, for possible future API reasons it still exists though
	@NotNull
	private static RootLoggerComponentBuilder setRootLogger(
			@NotNull final ConfigurationBuilder<BuiltConfiguration> builder,
			@NotNull final Level logLevel,
			@NotNull final AppenderComponentBuilder... appenderComponentBuilders) {
		Objects.requireNonNull(builder);
		Objects.requireNonNull(logLevel);
		Objects.requireNonNull(appenderComponentBuilders);
		var root = builder.newRootLogger(logLevel);
		for (var appender : appenderComponentBuilders)
			root.add(builder.newAppenderRef(appender.getName()));
		builder.add(root);
		return root;
	}
}