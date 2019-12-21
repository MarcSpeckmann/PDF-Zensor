package de.uni_hannover.se.pdfzensor.testing.iostreams;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/** An extension of the ByteArrayInputStream to check if the stream was properly closed. */
public class CheckedByteInputStream extends ByteArrayInputStream {
	/** Stores if this stream was closed by a call to {@link #close()}. */
	private boolean wasClosed = false;
	
	/**
	 * Creates a new {@link ByteArrayInputStream} containing the provided data.
	 *
	 * @param buf the data used to populate the stream.
	 * @see ByteArrayInputStream#ByteArrayInputStream(byte[])
	 */
	public CheckedByteInputStream(@NotNull final byte[] buf) {
		super(buf);
	}
	
	/**
	 * Returns true iff this stream was closed previously.
	 *
	 * @return true iff this stream was closed previously.
	 */
	public boolean isClosed() {
		return wasClosed;
	}
	
	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		wasClosed = true;
		super.close();
	}
}