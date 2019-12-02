package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.censor.utils.DoubleBufferedStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//           PLEASE NOTE: during prototyping this class did not copy all data correctly for FormXObjects.             //
//           Thus there might be some functions missing that should further push and pop the stream-stack.            //
//           This should be elicited further during development. Since FormXObjects will be censored like             //
//           images it is not a huge issue but one that should be considered.                                         //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * PDFStreamProcessor builds on the {@link org.apache.pdfbox.contentstream.PDFStreamEngine} via the {@link
 * PDFTextStripper} to add writing-functionality on top of the input streams. Thus for any stream opened by the
 * PDFStreamEngine to read from, the PDFStreamProcessor will open a corresponding output stream. After the input stream
 * got closed the corresponding output stream will swap buffers. Thus the original content of the stream is replaced by
 * what the output stream currently holds. Therefore, to leave the content of the PDF-file untouched, the user of this
 * API has to copy any data from the input stream into the corresponding output stream.<br> Use {@link
 * #getText(PDDocument)} to process the given document.
 */
class PDFStreamProcessor extends PDFTextStripper {
	/** A {@link Logger}-instance that should be used by this class' member methods to log their state and errors. */
	private static final Logger LOGGER = Logging.getLogger();
	/** Whenever a stream is entered it gets added to the top of the stack... and taken off when the stream is closed. */
	@Nullable
	private Deque<DoubleBufferedStream> currentStream = null;
	
	/**
	 * Creates a new instance of the PDFStreamProcessor.
	 *
	 * @throws IOException If there is an error loading the properties.
	 * @see PDFTextStripper#PDFTextStripper()
	 */
	PDFStreamProcessor() throws IOException {
		super();
		LOGGER.log(Level.DEBUG, "Initialized a new PDFStreamProcessor-instance");
	}
	
	/**
	 * Gets a {@link ContentStreamWriter} that writes to the stream on top of the stream-stack. Returns
	 * <code>null</code> if no stack is currently initialized.
	 *
	 * @return a {@link ContentStreamWriter} that writes to the stream on top of the stream-stack.
	 */
	@Nullable
	protected ContentStreamWriter getCurrentContentStream() {
		ContentStreamWriter result = null;
		if (currentStream != null) {
			var currentOS = Objects.requireNonNull(currentStream.peek()).getOutputStream();
			result = new ContentStreamWriter(currentOS);
		}
		return result;
	}
	
	/**
	 * Pushes a stream to the top of the stream-stack. Does nothing if no stack is currently initialized.
	 *
	 * @param bs the stream that should be pushed to the top of the stack.
	 */
	private void pushStream(@NotNull final DoubleBufferedStream bs) {
		if (currentStream == null)
			LOGGER.log(Level.WARN,
					   "It was tried to push a stream when the PDFStreamProcessor-instance's stack has not yet been initialized.");
		else
			currentStream.push(Objects.requireNonNull(bs));
	}
	
	/**
	 * Removes the top stream of the current stream-stack, closes it and returns it.
	 *
	 * @return the former top stream on the stack.
	 */
	@NotNull
	private DoubleBufferedStream popStream() {
		var ret = Optional.ofNullable(currentStream).orElseThrow(NoSuchElementException::new).pop();
		Objects.requireNonNull(ret);
		try {
			ret.close();
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "Failed to close the PDFStreamProcessor-instance's current input stream.", e);
		}
		return ret;
	}
	
	/**
	 * Prepends PDFTextStripper's {@link PDFTextStripper#writeText(PDDocument, Writer)} by additionally checking for
	 * invalid parameters.
	 *
	 * @param doc          The document to get the data from. Not null.
	 * @param outputStream The location to put the text. Not null.
	 * @throws IOException          If the doc is in an invalid state.
	 * @throws NullPointerException If doc or outputStream are null.
	 */
	@Override
	public void writeText(@NotNull final PDDocument doc, @NotNull final Writer outputStream) throws IOException {
		// This is not necessary but cleaner as PDFStripper does not check its arguments
		super.writeText(Objects.requireNonNull(doc), Objects.requireNonNull(outputStream));
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Prepends PDFTextStripper's {@link PDFTextStripper#startDocument(PDDocument)} by initializing a new empty
	 * DoubleBufferedStream-stack.
	 *
	 * @param document The PDF document that is about to be processed. May not be <code>null</code>.
	 * @throws IOException          if an I/O error occurs.
	 * @throws NullPointerException if <code>document</code> is <code>null</code>.
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
	 * @param document The PDF document that has been processed. May not be <code>null</code>.
	 * @throws IOException          if an I/O error occurs.
	 * @throws NullPointerException if document is <code>null</code>
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
	 * Prepends PDFTextStripper's {@link PDFTextStripper#startPage(PDPage)} by creating a new PDStream for the page
	 * which is about to be processed and adding said PDStream to the top of the stack.
	 *
	 * @param page The page which will be processed. May not be <code>null</code>.
	 * @throws IOException          if an I/O error occurs.
	 * @throws NullPointerException if page is <code>null</code>.
	 */
	@Override
	protected void startPage(@NotNull final PDPage page) throws IOException {
		Objects.requireNonNull(page);
		LOGGER.log(Level.DEBUG, "Starting to process page {}/{}", this::getCurrentPageNo, document::getNumberOfPages);
		var bufferedStream = new DoubleBufferedStream(new PDStream(document), page.getContents());
		pushStream(bufferedStream);
		super.startPage(page);
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Appends PDFTextStripper's {@link PDFTextStripper#endPage(PDPage)} by removing the top stream from the stack and
	 * replacing the data of the page that was read by the contents of the popped stream.
	 *
	 * @param page The page which just got processed. May not be <code>null</code>.
	 * @throws IOException          if an IO error occurs.
	 * @throws NullPointerException if page is <code>null</code>.
	 */
	@Override
	protected void endPage(PDPage page) throws IOException {
		super.endPage(page);
		Objects.requireNonNull(page).setContents(popStream().getStream());
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Appends PDFTextStripper's {@link PDFTextStripper#showTransparencyGroup(PDTransparencyGroup)} by pushing and
	 * popping the stream-stack accordingly.
	 *
	 * @param form transparency group (form) XObject. May not be <code>null</code>.
	 * @throws IOException          if the transparency group cannot be processed.
	 * @throws NullPointerException if form is <code>null</code>.
	 */
	@Override
	public void showTransparencyGroup(@NotNull final PDTransparencyGroup form) throws IOException {
		Objects.requireNonNull(form);
		LOGGER.log(Level.DEBUG, "Entering transparency group");
		pushStream(new DoubleBufferedStream(form.getContentStream(), form.getContents()));
		super.showTransparencyGroup(form);
		popStream();
		LOGGER.log(Level.DEBUG, "Exiting transparency group");
	}
	
	/**
	 * <i><b>Do not call this method directly</b></i><br>
	 * Appends PDFTextStripper's {@link PDFTextStripper#showForm(PDFormXObject)} by pushing and popping the stream-stack
	 * accordingly.
	 *
	 * @param form form XObject. May not be <code>null</code>.
	 * @throws IOException          if the form cannot be processed.
	 * @throws NullPointerException if <code>form</code> is <code>null</code>.
	 */
	@Override
	public void showForm(@NotNull final PDFormXObject form) throws IOException {
		Objects.requireNonNull(form);
		LOGGER.log(Level.DEBUG, "Entering FormXObject");
		pushStream(new DoubleBufferedStream(form.getContentStream(), form.getContents()));
		super.showForm(form);
		popStream();
		LOGGER.log(Level.DEBUG, "Exiting FormXObject");
	}
}
