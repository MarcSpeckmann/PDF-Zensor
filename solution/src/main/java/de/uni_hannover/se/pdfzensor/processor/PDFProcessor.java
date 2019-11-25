package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.Objects;

public class PDFProcessor {
    private final PDFHandler handler;

    public PDFProcessor(PDFHandler handler) {
        this.handler = Objects.requireNonNull(handler, "PDFHandler must not be null");
    }
    public void process(PDDocument document) throws IOException {
        final PDFStreamProcessor processor = new TextProcessor(handler);
        processor.getText(document);
    }
}
