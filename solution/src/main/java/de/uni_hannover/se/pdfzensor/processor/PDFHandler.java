package de.uni_hannover.se.pdfzensor.processor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;

public interface PDFHandler {
	void beginDocument(PDDocument doc);
	void beginPage(PDDocument doc, PDPage page, int pageNum);
	void endPage(PDDocument doc, PDPage page, int pageNum);
	void endDocument(PDDocument doc);
	
	boolean onText(TextPosition pos);
}