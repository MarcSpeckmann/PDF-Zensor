package de.uni_hannover.se.pdfzensor.processor;

import java.io.IOException;
import java.util.Objects;

/**
 *  mockup for TextProcessor
 */
class TextProcessor extends PDFStreamProcessor {

    private PDFHandler handler;

    TextProcessor(PDFHandler handler) throws IOException {
        super();
        Objects.requireNonNull(handler);
        this.handler = handler;
    }
}

