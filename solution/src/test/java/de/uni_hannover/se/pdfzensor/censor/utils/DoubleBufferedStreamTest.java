package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ByteArrayProvider;
import de.uni_hannover.se.pdfzensor.testing.iostreams.CheckedByteInputStream;
import de.uni_hannover.se.pdfzensor.testing.iostreams.DummyPDStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/** This class should contain all the tests related only to the {@link DoubleBufferedStream}. */
class DoubleBufferedStreamTest {
	
	/** Checks if an illegal null-argument is correctly identified and signaled. */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testNullValues() {
		assertThrows(NullPointerException.class, () -> new DoubleBufferedStream(null, null));
		try (var tmp = new ByteArrayInputStream(new byte[0])) {
			assertThrows(NullPointerException.class, () -> new DoubleBufferedStream(null, tmp));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertThrows(NullPointerException.class, () -> new DoubleBufferedStream(new DummyPDStream(), null));
	}
	
	/**
	 * Run to check if the provided data is written to the underlying PDStream correctly.
	 *
	 * @param data The expected stream data to use when testing the buffering.
	 */
	@ParameterizedTest(name = "Run {index}")
	@ArgumentsSource(ByteArrayProvider.class)
	void testDataBuffering(@NotNull byte[] data) {
		var pdStream = new DummyPDStream(data);
		var is = new CheckedByteInputStream(data);
		try (var stream = new DoubleBufferedStream(pdStream, is)) {
			assertEquals(is, stream.getInputStream());
			assertEquals(pdStream, stream.getStream());
			assertNotNull(stream.getOutputStream());
			stream.getOutputStream().write(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(is.isClosed());
		pdStream.assertAsExpected();
	}
}