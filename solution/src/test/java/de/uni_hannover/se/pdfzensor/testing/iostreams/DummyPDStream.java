package de.uni_hannover.se.pdfzensor.testing.iostreams;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


/** A Dummy PDStream to check if data was written to it correctly. */
public class DummyPDStream extends PDStream {
	private final byte[] expected;
	@NotNull
	private final ByteArrayOutputStream os;
	
	public DummyPDStream(byte... expected) {
		super((COSStream) null);
		this.expected = expected;
		os = new ByteArrayOutputStream();
	}
	
	@NotNull
	@SuppressWarnings("ReturnPrivateMutableField")
	@Override
	public OutputStream createOutputStream() {
		//For testing we only need one stream and thus don't need to create a new one in here
		return os;
	}
	
	public void assertAsExpected() {
		assertArrayEquals(expected, os.toByteArray());
	}
}