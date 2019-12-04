package de.uni_hannover.se.pdfzensor.testing.iostreams;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/** An extension of the ByteArrayInputStream to check if the stream was properly closed. */
public class CheckedByteInputStream extends ByteArrayInputStream {
	private boolean wasClosed = false;
	
	public CheckedByteInputStream(@NotNull final byte[] buf) {
		super(buf);
	}
	
	public boolean isClosed() {
		return wasClosed;
	}
	
	@Override
	public void close() throws IOException {
		wasClosed = true;
		super.close();
	}
}