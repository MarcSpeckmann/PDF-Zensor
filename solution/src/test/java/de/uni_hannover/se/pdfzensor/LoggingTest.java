package de.uni_hannover.se.pdfzensor;

import de.uni_hannover.se.pdfzensor.testing.LoggingUtility;
import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.appenders.TestAppender;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.LogLevelProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.LoggingUtility.LOG_LEVELS;
import static org.junit.jupiter.api.Assertions.*;

/** LoggingTest should contain all unit-tests related only to {@link Logging}. */
@SuppressWarnings("ConstantConditions")
class LoggingTest {
	
	@Test
	void testGeneral() {
		TestUtility.assertIsUtilityClass(Logging.class);
	}
	
	/** Multiple tests related to the correct (and incorrect) initialization of the logging. */
	@Test
	void testInit() {
		//Logging is not initialized
		assertTrue(TestUtility.getRootLogger().isEmpty());
		Logging.deinit();
		//Logging is not initialized after deinitializing it when it was not initialized before
		assertTrue(TestUtility.getRootLogger().isEmpty());
		//And that does not change if we call getRootLogger
		assertTrue(TestUtility.getRootLogger().isEmpty());
		
		//Initialize logging on each level and check if a RootLogger was initialized on the right level
		for (Level level : Level.values()) {
			assertTrue(TestUtility.getRootLogger().isEmpty());
			Logging.init(level);
			assertTrue(TestUtility.getRootLogger().isPresent());
			assertEquals(level, TestUtility.getRootLogger().orElseThrow().getLevel());
			Logging.deinit();
			assertTrue(TestUtility.getRootLogger().isEmpty());
		}
		
		//Initializing with log-level null should throw a NullPointerException
		assertTrue(TestUtility.getRootLogger().isEmpty());
		assertThrows(NullPointerException.class, () -> Logging.init(null));
		assertTrue(TestUtility.getRootLogger().isEmpty());
		
		//Test for automatic initialization when getLogger() is called
		assertTrue(TestUtility.getRootLogger().isEmpty());
		assertNotNull(Logging.getLogger());
		assertTrue(TestUtility.getRootLogger().isPresent());
		assertEquals(Level.OFF, TestUtility.getRootLogger().orElseThrow().getLevel());
		Logging.deinit();
		assertTrue(TestUtility.getRootLogger().isEmpty());
	}
	
	/**
	 * An automated parameterized test to check if the logging logs messages correctly for each log-level.
	 *
	 * @param loggerLevel the level of the logger. Messages lower than this level (e. g. DEBUG when loggerlevel is
	 *                    FATAL) will be filtered out
	 */
	@ParameterizedTest(name = "Run {index}: level: {0}")
	@ArgumentsSource(LogLevelProvider.class)
	void testLoggingForEachLevel(Level loggerLevel) {
		// A Stream with each Message for each (Valid) log-level (OFF and ALL are no log-levels)
		final String[] messages = {"MSG1", "MSG2", "MSG3", "MSG4"};
		final var events = TestUtility.join(Stream.of(messages), LOG_LEVELS, LoggingUtility::createLogEvent)
									  .collect(Collectors.toUnmodifiableList());
		
		Logger logger;
		org.apache.logging.log4j.core.Logger rootLogger;
		Logging.init(loggerLevel);
		assertNotNull(logger = Logging.getLogger());
		rootLogger = TestUtility.getRootLogger().orElseThrow();
		rootLogger.getAppenders().values().forEach(rootLogger::removeAppender);
		var appender = new TestAppender(events, loggerLevel);
		rootLogger.addAppender(appender);
		appender.start();
		
		for (var e : events)
			logger.log(e.getLevel(), e.getMessage());
		Logging.deinit();
	}
}