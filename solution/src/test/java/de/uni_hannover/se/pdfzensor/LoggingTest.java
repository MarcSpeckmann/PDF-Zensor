package de.uni_hannover.se.pdfzensor;

import de.uni_hannover.se.pdfzensor.testing.LoggingUtility;
import de.uni_hannover.se.pdfzensor.testing.appenders.TestAppender;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.LogLevelProvider;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.LoggingUtility.*;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.*;
import static org.junit.jupiter.api.Assertions.*;

/** LoggingTest should contain all unit-tests related only to {@link Logging}. */
@SuppressWarnings("ConstantConditions")
class LoggingTest {
	
	@Test
	void testGeneral() {
		assertIsUtilityClass(Logging.class);
	}
	
	@Test
	void testDeinitializingUninitializedLogging() {
		assertFalse(isLoggingInitialized());
		assertDoesNotThrow(Logging::deinit);
		assertFalse(isLoggingInitialized());
	}
	
	@ParameterizedTest
	@ArgumentsSource(LogLevelProvider.class)
	void testInitializeLevel(@NotNull Level level) {
		assertFalse(isLoggingInitialized());
		Logging.init(level);
		assertTrue(isLoggingInitialized());
		assertEquals(level, getRootLogger().orElseThrow().getLevel());
		Logging.deinit();
		assertFalse(isLoggingInitialized());
	}
	
	@Test
	void testInitializeWithNull() {
		assertFalse(isLoggingInitialized());
		assertThrows(NullPointerException.class, () -> Logging.init(null));
		assertFalse(isLoggingInitialized());
	}
	
	@Test
	void testAutomaticInit() {
		assertFalse(isLoggingInitialized());
		assertNotNull(Logging.getLogger());
		assertTrue(isLoggingInitialized());
		assertEquals(Level.ERROR, getRootLogger().orElseThrow().getLevel());
		Logging.deinit();
		assertFalse(isLoggingInitialized());
	}
	
	/**
	 * An automated parameterized test to check if the logging logs messages correctly for each log-level.
	 *
	 * @param loggerLevel the level of the logger. Messages lower than this level (e. g. DEBUG when logger-level is
	 *                    FATAL) will be filtered out
	 */
	@ParameterizedTest(name = "[{index}] level: {0}")
	@ArgumentsSource(LogLevelProvider.class)
	void testLoggingForEachLevel(@NotNull Level loggerLevel) {
		// A Stream with each Message for each (valid) log-level (OFF and ALL are no log-levels)
		final String[] messages = {null, "MSG1", "MSG2", "MSG3", "MSG4"};
		final var events = crossJoin(Stream.of(messages), LOG_LEVELS, LoggingUtility::createLogEvent)
				.collect(Collectors.toUnmodifiableList());
		
		Logging.init(loggerLevel);
		var logger = Logging.getLogger();
		assertNotNull(logger);
		// Set our TestAppender to be the only appender in the root logger
		var rootLogger = getRootLogger().orElseThrow();
		rootLogger.getAppenders().values().forEach(rootLogger::removeAppender);
		var appender = new TestAppender(events, loggerLevel);
		rootLogger.addAppender(appender);
		appender.start();
		//
		
		for (var e : events)
			logger.log(e.getLevel(), e.getMessage());
		Logging.deinit();
	}
}