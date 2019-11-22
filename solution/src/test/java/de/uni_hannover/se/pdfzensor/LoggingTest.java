package de.uni_hannover.se.pdfzensor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** LoggingTest should contain all unit-tests related to {@link Logging}. */
class LoggingTest {
	
	/** Returns a Stream of all log-levels as arguments. */
	static Stream<Arguments> levelLoggingParameters() throws Throwable {
		return Stream.of(Level.values())
					 .map(Arguments::of);
	}
	
	/** Multiple tests related to the correct (and incorrect) initialization of the logging. */
	@Test
	void testInit() {
		TestUtility.assertIsUtilityClass(Logging.class);
		
		//Logging is not initialized
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		Logging.deinit();
		//Logging is not initialized after deinitializing it when it was not initialized before
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		//And that does not change if we call getRootLogger
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		
		//Initialize logging on each level and check if a RootLogger was initialized on the right level
		for (Level level : Level.values()) {
			assertTrue(Logging.getRootLogger()
							  .isEmpty());
			Logging.init(level);
			assertTrue(Logging.getRootLogger()
							  .isPresent());
			assertEquals(level, Logging.getRootLogger()
									   .orElseThrow()
									   .getLevel());
			Logging.deinit();
			assertTrue(Logging.getRootLogger()
							  .isEmpty());
		}
		
		//Initializing with log-level null should throw a NullPointerException
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		assertThrows(NullPointerException.class, () -> Logging.init(null));
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		
		//Test for automatic initialization when getLogger() is called
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		assertNotNull(Logging.getLogger());
		assertTrue(Logging.getRootLogger()
						  .isPresent());
		assertEquals(Level.OFF, Logging.getRootLogger()
									   .orElseThrow()
									   .getLevel());
		Logging.deinit();
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
	}
	
	/**
	 * An automated parameterized test to check if the logging logs messages correctly for each log-level.
	 *
	 * @param loggerLevel the level of the logger. Messages lower than this level (e. g. DEBUG when loggerlevel is
	 *                    FATAL) will be filtered out
	 */
	@ParameterizedTest(name = "Run {index}: level: {0}")
	@MethodSource("levelLoggingParameters")
	void testLoggingForEachLevel(Level loggerLevel) {
		// A Stream with each Message for each (Valid) log-level (OFF and ALL are no log-levels)
		final List<LogEvent> events = Stream.of("MSG1", "MSG2", "MSG3", "MSG4")
											.map(str -> new FormattedMessageFactory().newMessage(str))
											.flatMap(msg -> Stream.of(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN,
																	  Level.ERROR, Level.FATAL)
																  .map(lvl -> new Log4jLogEvent.Builder().setLevel(lvl)
																										 .setMessage(
																												 msg)
																										 .build()))
											.collect(Collectors.toUnmodifiableList());
		
		Logger logger;
		org.apache.logging.log4j.core.Logger rootLogger;
		Logging.init(loggerLevel);
		assertNotNull(logger = Logging.getLogger());
		rootLogger = Logging.getRootLogger()
							.orElseThrow();
		rootLogger.getAppenders()
				  .values()
				  .forEach(rootLogger::removeAppender);
		var appender = new TestAppender(events, loggerLevel);
		rootLogger.addAppender(appender);
		appender.start();
		
		for (var e : events) {
			logger.log(e.getLevel(), e.getMessage());
		}
		Logging.deinit();
	}
	
	/** Used to test if logging happens in the right order and gets filtered by log-level correctly. */
	private static class TestAppender extends AbstractAppender {
		Queue<LogEvent> events;
		
		TestAppender(@NotNull List<LogEvent> events, Level lvl) {
			super("tmp", null, null, false, null);
			this.events = new ArrayDeque<>();
			events.stream()
				  .filter(e -> e.getLevel()
								.isMoreSpecificThan(lvl))
				  .forEach(this.events::offer);
		}
		
		@Override
		public void append(@NotNull final LogEvent event) {
			assertFalse(events.isEmpty());
			var cur = events.poll();
			assertEquals(cur.getLevel(), event.getLevel());
			assertEquals(cur.getMessage()
							.getFormattedMessage(), event.getMessage()
														 .getFormattedMessage());
		}
	}
}