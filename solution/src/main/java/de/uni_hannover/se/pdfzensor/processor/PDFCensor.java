package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.config.Settings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;



import java.io.IOException;
import java.util.Objects;

public final class PDFCensor implements PDFHandler {

    public PDFCensor(@NotNull Settings settings) throws IOException {
        Objects.requireNonNull(settings);
    }

    @Override
    public void beginDocument(PDDocument doc) {

    }

    @Override
    public void beginPage(PDDocument doc, PDPage page, int pageNum) {

    }

    @Override
    public void endPage(PDDocument doc, PDPage page, int pageNum) {

    }

    @Override
    public void endDocument(PDDocument doc) {
    }

    @Override
    public boolean shouldCensorText(TextPosition pos){
        return true;
    }
}
