package de.uni_hannover.se.pdfzensor.censor.utils;


import org.apache.pdfbox.pdmodel.common.PDStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * DoubleBufferedStream manages an {@link InputStream} and {@link OutputStream} to support concurrent reading and
 * writing. Data will be read from the InputStream and written to a buffer within the OutputStream. Once The
 * DoubleBufferedStream is closed, the OutputStream's contents will be fed into the provided {@link PDStream} instance.
 */
public class DoubleBufferedStream implements AutoCloseable {
	/** The pdf-stream to which the data will be written upon closing. */
	@NotNull
	private final PDStream stream;
	/** An input-stream providing the current data of the set {@link #stream}. */
	@NotNull
	private final InputStream is;
	/** All data is written into this stream and fed into the {@link #stream} when {@link #close()} is called. */
	@NotNull
	private final ByteArrayOutputStream os;
	
	/**
	 * Creates a new DoubleBufferedStream that acts on the passed PDStream and gets its data from the InputStream.
	 *
	 * @param stream The PDF-Stream to write data to when this stream is closed. May not be null.
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
	 * Returns the pdf-stream as it was set in the constructor. All buffered data will be written into this stream uppon
	 * closing.
	 *
	 * @return The PDF-Stream as set in the constructor.
	 */
	@SuppressWarnings("ReturnPrivateMutableField")//we want to return a reference here
	@NotNull
	@Contract(pure = true)
	public PDStream getStream() {
		return stream;
	}
	
	/**
	 * Returns the input stream as set in the constructor.
	 *
	 * @return The InputStream as set in the constructor.
	 */
	@SuppressWarnings("ReturnPrivateMutableField")//we want to return a reference here
	@NotNull
	@Contract(pure = true)
	public InputStream getInputStream() {
		return is;
	}
	
	/**
	 * Returns the output stream into which the data which eventually replaces the current stream's content should be *
	 * written.
	 *
	 * @return The output-stream to the buffer that holds data until {@link #close()} is called.
	 */
	@NotNull
	@SuppressWarnings("ReturnPrivateMutableField")//we want to return a reference here
	@Contract(pure = true)
	public OutputStream getOutputStream() {
		return os;
	}
	
	/**
	 * Closes the underlying streams and writes the OutputStream's contents to {@link #getStream()}.
	 *
	 * @throws IOException if the resource cannot be closed.
	 */
	@Override
	public void close() throws IOException {
		is.close();
		os.close();
		try (var s = stream.createOutputStream()) {
			s.write(os.toByteArray());
		}
	}
}
