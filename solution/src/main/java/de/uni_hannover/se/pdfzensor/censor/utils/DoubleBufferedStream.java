package de.uni_hannover.se.pdfzensor.censor.utils;


import org.apache.pdfbox.pdmodel.common.PDStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * DoubleBufferedStream manages an {@link InputStream} and {@link OutputStream} to support concurrent reading and
 * writing. Data will be read from the inputstream and written to a buffer within the outputstreams. Once The
 * DoubleBufferedStream is closed, the outputsstream's contents will be fed into the provided {@link PDStream}
 * instance.
 */
public class DoubleBufferedStream implements AutoCloseable {
	/** The pdf-stream data will be written to upon closing */
	@NotNull
	private final PDStream stream;
	/** An input-stream providing the current data of the set {@link #stream}. */
	@NotNull
	private final InputStream is;
	/** All data is written into this stream and fed into the {@link #stream} when {@link #close()} is called. */
	@NotNull
	private final ByteArrayOutputStream os;
	
	/**
	 * Creates a new DoubleBufferedStream that acts on the passed PDStream and gets it's data from the InputStream.
	 *
	 * @param stream The PDF-Stream to write Data to when this stream is closed. May not be null.
	 * @param is     The InputStream containing the current data of the PDF-Stream. It may not be null nor closed
	 *               manually, use {@link #close()} instead.
	 */
	@Contract(pure = true)
	public DoubleBufferedStream(@NotNull PDStream stream, @NotNull InputStream is) {
		this.stream = Objects.requireNonNull(stream);
		this.is = Objects.requireNonNull(is);
		os = new ByteArrayOutputStream();
	}
	
	/**
	 * @return The PDF-Stream as set in the constructor.
	 */
	@NotNull
	@Contract(pure = true)
	public PDStream getStream() {
		return stream;
	}
	
	/**
	 * @return The InputStream as set in the constructor.
	 */
	@NotNull
	@Contract(pure = true)
	public InputStream getInputStream() {
		return is;
	}
	
	/**
	 * @return The OutputStream into which the data should be written that replaces current stream's content.
	 */
	@Contract(pure = true)
	public OutputStream getOutputStream() {
		return os;
	}
	
	/**
	 * Closes the underlying streams and writes the outputstream's contents to {@link #getStream()}.
	 *
	 * @throws Exception if the resource cannot be closed.
	 */
	@Override
	public void close() throws Exception {
		is.close();
		os.close();
		try (var s = stream.createOutputStream()) {
			s.write(os.toByteArray());
		}
	}
}
