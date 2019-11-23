package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.censor.utils.DoubleBufferedStream;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PDFStreamProcessorTest {
	
	@Nullable
	@SuppressWarnings("unchecked")
	private static Deque<DoubleBufferedStream> getStreamStack(PDFStreamProcessor processor) {
		try {
			var field = PDFStreamProcessor.class.getDeclaredField("currentStream");
			field.setAccessible(true);
			var value = field.get(processor);
			return (Deque<DoubleBufferedStream>) value;
		} catch (Exception e) {
			Assertions.fail("Could not retrieve the stream-stack", e);
		}
		return null;
	}
	
	@Test
	void testIllegalArguments() throws IOException {
		final var processor = new PDFStreamProcessor();
		assertThrows(NullPointerException.class, () -> processor.getText(null));
	}
	
	@Test
	void testStreamStack() throws IOException {
		final var processor = new PDFStreamProcessor();
		final var stack = getStreamStack(processor);
		
	}
	
}