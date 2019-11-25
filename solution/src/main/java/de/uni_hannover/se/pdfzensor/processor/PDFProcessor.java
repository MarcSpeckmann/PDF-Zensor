package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.Objects;

public class PDFProcessor {
    private final PDFHandler handler;

    public PDFProcessor(PDFHandler handler) {
        Objects.requireNonNull(handler);
        this.handler = handler;
    }
    public void process(PDDocument document) throws IOException {
        final PDFStreamProcessor processor = new TextProcessor(handler);
        processor.getText(document);
    }
}
