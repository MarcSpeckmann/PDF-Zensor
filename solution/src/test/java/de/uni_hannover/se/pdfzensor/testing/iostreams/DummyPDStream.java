package de.uni_hannover.se.pdfzensor.testing.iostreams;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


/** A Dummy PDStream to check if data was written to it correctly. */
public class DummyPDStream extends PDStream {
	/** The data that is expected to be written to this stream. */
	private final byte[] expected;
	/** Any incoming data will be written into this stream. */
	@NotNull
	private final ByteArrayOutputStream os;
	
	/**
	 * Creates a new DummyStream and sets its expected data-set.
	 *
	 * @param expected the data-set that will be tested against when calling {@link #assertAsExpected()}.
	 */
	public DummyPDStream(byte... expected) {
		super((COSStream) null);
		this.expected = expected;
		os = new ByteArrayOutputStream();
	}
	
	/** {@inheritDoc} */
	@NotNull
	@SuppressWarnings("ReturnPrivateMutableField")
	@Override
	public OutputStream createOutputStream() {
		//For testing we only need one stream and thus don't need to create a new one in here
		return os;
	}
	
	/**
	 * Asserts that the actually written data corresponds to the expected data as set in the constructor.
	 */
	public void assertAsExpected() {
		assertArrayEquals(expected, os.toByteArray());
	}
}