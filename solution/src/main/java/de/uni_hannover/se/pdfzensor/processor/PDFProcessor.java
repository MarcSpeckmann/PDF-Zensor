package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.Objects;

/**
 * PDFProcessor is a class that runs a TextProcessor with a PDFHandler
 */
public class PDFProcessor {
    private final PDFHandler handler;
    
    /**
     * @param handler The PDFHandler that is used for the TextProcessor to open and close the document and pages
     */
    public PDFProcessor(PDFHandler handler) {
        this.handler = Objects.requireNonNull(handler, "PDFHandler must not be null");
    }
    
    /**
     * This function runs the TextProcessor with the delivered PDF document
     * @param document The document to be processed in the TextProcessor
     * @throws IOException The exception is thrown in the constructor of the TextProcessor
     */
    public void process(PDDocument document) throws IOException {
        final PDFStreamProcessor processor = new TextProcessor(handler);
        processor.getText(document);
    }
}
