package de.uni_hannover.se.pdfzensor.testing;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LoggingUtility may provide utility-functions and constants related to testing the logger.
 */
public final class LoggingUtility {
	/**
	 * Provides a list of log-levels. That is the same as the {@link Logging#VERBOSITY_LEVELS} but without {@link
	 * Level#OFF} and {@link Level#ALL} as they are no levels you should log at.
	 */
	public static final List<Level> LOG_LEVELS = Stream.of(Logging.VERBOSITY_LEVELS)
													   .filter(Level.OFF::equals)
													   .filter(Level.ALL::equals)
													   .collect(Collectors.toUnmodifiableList());
	
	/**
	 * Creates a new {@link LogEvent} containing the given message and level.
	 *
	 * @param msg   The message the log-event is storing.
	 * @param level The level at which the log-event should be logged.
	 * @return a LogEvent containing the provided data.
	 */
	@NotNull
	public static LogEvent createLogEvent(@NotNull String msg, Level level) {
		var event = new MutableLogEvent(new StringBuilder(msg), new Object[0]);
		event.setLevel(level);
		return event;
	}
	
	public static boolean isLoggingInitialized() {
		return getRootLogger().isPresent();
	}
	
	@NotNull
	public static Optional<Logger> getRootLogger() {
		try {
			var contextField = Logging.class.getDeclaredField("context");
			contextField.setAccessible(true);
			var context = (LoggerContext) contextField.get(null);
			return Optional.ofNullable(context).map(LoggerContext::getRootLogger);
		} catch (Exception e) {
			Assertions.fail("Could not retrieve the RootLogger.", e);
		}
		return Optional.empty();
	}
	
}
