package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.censor.utils.DoubleBufferedStream;
import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.PDFProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PDFStreamProcessorTest {
	
	/**
	 * @throws IOException If there is an error loading the properties.
	 */
	@Test
	void testIllegalArguments() throws IOException {
		final var processor = new PDFStreamProcessor();
		assertThrows(NullPointerException.class, () -> processor.getText(null));
	}
	
	@Test
	void pushStreamWhenUninitialized() throws IOException {
		final var processor = new PDFStreamProcessor();
		final var pushStreamMethod = TestUtility.getPrivateMethod(processor.getClass(), "pushStream", DoubleBufferedStream.class);
		assertDoesNotThrow(() -> pushStreamMethod.invoke(processor, (DoubleBufferedStream)null));
	}
	
	/**
	 * @throws IOException If there is an error loading the file or properties.
	 */
	@ParameterizedTest
	@ArgumentsSource(PDFProvider.class)
	void testStreamStack(File file) throws IOException {
		try (var doc = PDDocument.load(file)) {
			final var processor = new StackCheckingStreamProcessor();
			assertNotNull(doc);
			assertDoesNotThrow(() -> processor.getText(doc));
		}
	}
}