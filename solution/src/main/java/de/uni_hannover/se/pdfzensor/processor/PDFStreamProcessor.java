package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.censor.utils.DoubleBufferedStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * PDFStreamProcessor builds on the {@link org.apache.pdfbox.contentstream.PDFStreamEngine} via the {@link
 * PDFTextStripper} to add writing-functionality on top of the input streams. Thus for any stream opened by the
 * PDFStreamEngine to read from the PDFStreamProcessor will open a corresponding output stream. After the input stream
 * got closed the corresponding output stream will swap buffers. Thus the original content of the stream is replaced by
 * what the output stream currently holds. That means that, to leave the content of the PDF-file untouched the user of
 * this API has to copy any data from the input stream into the corresponding output stream.<br> Use {@link
 * #getText(PDDocument)} to process the given document.
 */
class PDFStreamProcessor extends PDFTextStripper {
	private static final Logger LOGGER = Logging.getLogger();
	/** Whenever a stream is entered it gets added to the top of the stack... and taken off when the stream is closed. */
	@Nullable
	private Deque<DoubleBufferedStream> currentStream = null;
	
	/**
	 * @throws IOException If there is an error loading the properties.
	 */
	public PDFStreamProcessor() throws IOException {
		super();
		LOGGER.log(Level.DEBUG, "Initialized a new PDFStreamProcessor-instance");
	}
	
	/**
	 * Gets a {@link ContentStreamWriter} that writes to the stream on top of the stream-stack.
	 *
	 * @return a {@link ContentStreamWriter} that writes to the stream on top of the stream-stack.
	 */
	@Nullable
	protected ContentStreamWriter getCurrentContentStream() {
		if (currentStream == null)
			return null;
		var currentOS = Objects.requireNonNull(currentStream.peek()).getOutputStream();
		return new ContentStreamWriter(currentOS);
	}
	
	/**
	 * Pushes a stream to the top of the stream-stack. Does nothing if no stack is currently initialized.
	 *
	 * @param bs the stream that should be pushed to the top of the stack.
	 */
	private void pushStream(@NotNull final DoubleBufferedStream bs) {
		if (currentStream == null) {
			LOGGER.log(Level.WARN, "It was tried to push a stream when the stack is not initialized.");
			return;
		}
		currentStream.push(Objects.requireNonNull(bs));
	}
	
	/**
	 * Removes the top stream of the current stream-stack, closes it and returns it.
	 *
	 * @return the former top stream on the stack.
	 */
	@NotNull
	private DoubleBufferedStream popStream() {
		var ret = Optional.ofNullable(currentStream)
						  .orElseThrow(NoSuchElementException::new)
						  .pop();
		Objects.requireNonNull(ret);
		try {
			ret.close();
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, "Failed to close the current output stream.", e);
		}
		return ret;
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Appends PDFTextStripper's {@link PDFTextStripper#startDocument(PDDocument)} by initializing a new empty
	 * DoubleBufferedStream-Stack.
	 *
	 * @param document The PDF document that is being processed.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void startDocument(@NotNull final PDDocument document) throws IOException {
		var information = Objects.requireNonNull(document).getDocumentInformation();
		LOGGER.log(Level.DEBUG, "Starting to process a new document: {} by {}",
				   information::getTitle, information::getAuthor);
		currentStream = new ArrayDeque<>();
		super.startDocument(document);
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Appends PDFTextStripper's {@link PDFTextStripper#endDocument(PDDocument)} by deinitializing the stream stack.
	 *
	 * @param document The PDF document that has been processed.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void endDocument(PDDocument document) throws IOException {
		super.endDocument(document);
		Objects.requireNonNull(currentStream);
		if (!currentStream.isEmpty()) {
			LOGGER.log(Level.ERROR,
					   "The stream stack was not empty after the whole document was processed." +
					   "This should not happen as it indicates that there is an issue with pushing" +
					   "and popping the current streams as they get read from the PDF-file.");
		}
		currentStream.clear();
		currentStream = null;
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Appends PDFTextStripper's {@link PDFTextStripper#startPage(PDPage)} by creating a new PDStream for the page about
	 * to be processed and adding it to the top of the stack.
	 *
	 * @param page The page we are about to process.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void startPage(@NotNull final PDPage page) throws IOException {
		Objects.requireNonNull(page);
		LOGGER.log(Level.DEBUG, "Starting to process page {}/{}", document::getNumberOfPages, this::getCurrentPageNo);
		var bufferedStream = new DoubleBufferedStream(new PDStream(document), page.getContents());
		pushStream(bufferedStream);
		super.startPage(page);
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Appends PDFTextStripper's {@link PDFTextStripper#endPage(PDPage)} by removing the top stream from the stack and
	 * replacing the data of the page that was read by the contents of the popped stream.
	 *
	 * @param page The page we are just got processed.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void endPage(PDPage page) throws IOException {
		super.endPage(page);
		page.setContents(popStream().getStream());
	}
	
}
