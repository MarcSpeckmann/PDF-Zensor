package de.uni_hannover.se.pdfzensor.testing.appenders;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Used to test if logging happens in the right order and gets filtered by log-level correctly. It is not guaranteed
 * that the appender works in any other scenario. Upon construction a sorted list of events and the TestAppender's level
 * are provided. Whenever a message is appended it is checked if the message corresponds to the next event in the
 * provided list in regards to its level and message. The list passed in the constructor thus gives the content and the
 * order of the expected messages. Those events will first be filtered by their specificity such that the appender only
 * expects events that are more specific than the given level. If the actual LogEvent does not match the expected one a
 * JUnit assertion fails.
 */
public class TestAppender extends AbstractAppender {
	@NotNull
	private Queue<LogEvent> events = new ArrayDeque<>();
	
	public TestAppender(@NotNull List<? extends LogEvent> events, @NotNull Level lvl) {
		super("test appender", null, null, false, null);
		events.stream().filter(e -> e.getLevel().isMoreSpecificThan(lvl)).forEach(this.events::offer);
	}
	
	@Override
	public void append(@NotNull final LogEvent event) {
		assertFalse(events.isEmpty());
		var cur = events.poll();
		assertEquals(cur.getLevel(), event.getLevel());
		assertEquals(cur.getMessage().getFormattedMessage(), event.getMessage().getFormattedMessage());
	}
}
