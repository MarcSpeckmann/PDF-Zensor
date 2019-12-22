package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * PDFProcessor's primary use is to be an abstraction of {@link TextProcessor} for the public API. As such the
 * PDFProcessor should be used for processing a provided document using a handler that was specified beforehand.
 */
public final class PDFProcessor {
	/** A {@link Logger}-instance that should be used by this class' member methods to log their state and errors. */
	private static final Logger LOGGER = Logging.getLogger();
	/** The handler that is called back on state changes in the {@link TextProcessor}. */
	private final PDFHandler handler;
	
	/**
	 * Creates a new instance of a PDFProcessor and sets the handler that should be responsible for managing the
	 * text-processing initiated by subsequent calls to {@link #process(PDDocument)}.
	 *
	 * @param handler the PDFHandler responsible for managing the text-processing.
	 */
	@Contract(pure = true)
	public PDFProcessor(@NotNull PDFHandler handler) {
		this.handler = Objects.requireNonNull(handler, "PDFHandler must not be null");
	}
	
	/**
	 * Runs the entire processing for the provided project by creating a new {@link TextProcessor} that processes the
	 * page using the formerly &ndash; in the constructor &ndash; specified {@link PDFHandler}.
	 *
	 * @param document the document that should be processed.
	 * @throws IOException if and I/O error occurs.
	 */
	public void process(@NotNull PDDocument document) throws IOException {
		final var processor = new TextProcessor(handler);
		final var information = Objects.requireNonNull(document).getDocumentInformation();
		LOGGER.debug("Processing {} by {}", information::getTitle, information::getAuthor);
		processor.getText(document);
		LOGGER.debug("Done processing");
	}
}
