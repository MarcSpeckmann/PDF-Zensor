package de.uni_hannover.swt.pdfzensor;

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
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.uni_hannover.swt.pdfzensor.TestUtility.assertIsUtilityClass;
import static org.junit.jupiter.api.Assertions.*;

class LoggingTest {
	
	static Stream<Arguments> levelLoggingParameters() throws Throwable {
		return Stream.of(Level.values())
					 .map(Arguments::of);
	}
	
	@Test
	void testInit() {
		assertIsUtilityClass(Logging.class);
		
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		Logging.deinit();
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		Logging.deinit();
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		
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
		
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		assertThrows(NullPointerException.class, () -> Logging.init(null));
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
		assertNotNull(Logging.getLogger());
		assertTrue(Logging.getRootLogger()
						  .isPresent());
		Logging.deinit();
		assertTrue(Logging.getRootLogger()
						  .isEmpty());
	}
	
	@ParameterizedTest(name = "Run {index}: level: {0}")
	@MethodSource("levelLoggingParameters")
	void testLoggingForEachLevel(Level loggerLevel) {
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