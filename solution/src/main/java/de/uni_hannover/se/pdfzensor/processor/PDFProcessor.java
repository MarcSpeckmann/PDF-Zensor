package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.Objects;

/**
 * PDFProcessor is a class that runs a {@link TextProcessor}  with a {@link PDFHandler}
 */
public class PDFProcessor {
	private static final Logger LOGGER = Logging.getLogger();
	
	private final PDFHandler handler;
	
	/**
	 * @param handler The PDFHandler that is used for the TextProcessor to open and close the document and pages
	 */
	public PDFProcessor(PDFHandler handler) {
		this.handler = Objects.requireNonNull(handler, "PDFHandler must not be null");
		LOGGER.log(Level.DEBUG, "Initialized a new PDFProcessor-instance");
	}
	
	/**
	 * This function runs the TextProcessor with the delivered PDF document depending on the {@link #handler}
	 *
	 * @param document The document to be processed in the TextProcessor
	 * @throws IOException The exception is thrown in the constructor of the TextProcessor
	 */
	public void process(PDDocument document) throws IOException {
		final PDFStreamProcessor processor = new TextProcessor(handler);
		
		var information = Objects.requireNonNull(document).getDocumentInformation();
		LOGGER.log(Level.DEBUG, "Starting to process the document: {} ", information::getTitle);
		
		processor.getText(document);
	}
}
