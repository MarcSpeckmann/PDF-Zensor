package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DoubleBufferedStreamTest {
	
	static Stream<Arguments> testDataProvider() throws Throwable {
		final byte[] emptyDataset = {};
		final byte[] simpleDataset = {0x00};
		final byte[] everyByte = new byte[256];
		for (int i = 0; i < everyByte.length; i++)
			everyByte[i] = (byte) (Byte.MIN_VALUE + i);
		final byte[] everyByteTwice = ArrayUtils.addAll(everyByte, everyByte);
		return Stream.of(emptyDataset, simpleDataset, everyByte, everyByteTwice)
					 .map(Arguments::of);
	}
	
	@Test
	void testNullValues() {
		assertThrows(NullPointerException.class, () -> new DoubleBufferedStream(null, null));
		try (var tmp = new ByteArrayInputStream(new byte[0])) {
			assertThrows(NullPointerException.class, () -> new DoubleBufferedStream(null, tmp));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertThrows(NullPointerException.class, () -> new DoubleBufferedStream(new DummyStream(), null));
	}
	
	/** Run to check if the provided data is written to the underlying PDStream correctly. */
	@ParameterizedTest(name = "Run {index}")
	@MethodSource("testDataProvider")
	void testDataBuffering(byte[] data) {
		var pdStream = new DummyStream(data);
		var is = new CheckedByteInputStream(data);
		try (var stream = new DoubleBufferedStream(pdStream, is)) {
			assertEquals(is, stream.getInputStream());
			assertEquals(pdStream, stream.getStream());
			assertNotNull(stream.getOutputStream());
			stream.getOutputStream()
				  .write(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(is.isClosed());
		pdStream.check();
	}
	
	/** An extension of the ByteArrayInputStream to check if the stream was properly closed. */
	private static class CheckedByteInputStream extends ByteArrayInputStream {
		private boolean wasClosed = false;
		
		CheckedByteInputStream(final byte[] buf) {
			super(buf);
		}
		
		boolean isClosed() {
			return wasClosed;
		}
		
		@Override
		public void close() throws IOException {
			wasClosed = true;
			super.close();
		}
	}
	
	/** A Dummy PDStream to check if data was written to it correctly. */
	private static class DummyStream extends PDStream {
		private final byte[] expected;
		private final ByteArrayOutputStream os;
		
		DummyStream(byte... expected) {
			super((COSStream) null);
			this.expected = expected;
			os = new ByteArrayOutputStream();
		}
		
		@Override
		public OutputStream createOutputStream() throws IOException {
			//For testing we only need one stream
			return os;
		}
		
		void check() {
			assertArrayEquals(expected, os.toByteArray());
		}
	}
}