package de.uni_hannover.se.pdfzensor.processor;

import jdk.jfr.Experimental;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.IOException;

public class TextProcessorTest {
	final PDFStreamProcessor processor = new PDFStreamProcessor();
	private String path = "/home/siddhartha/Schreibtisch/html/GMCI-assignment-03-WebTechnologies-Introduction.pdf";
	
	public TextProcessorTest() throws IOException {}
	
	// TODO edit tests, there are no tests available yet
	
	@Test
	@Disabled
	void endPageTest() throws IOException {
		System.out.println("Hallo Processor");
		TextProcessor tp = new TextProcessor(new PDFHandler() {
			@Override
			public void beginDocument(final PDDocument doc) {
				System.out.println("beginDocument");
			}
			
			@Override
			public void beginPage(final PDDocument doc, final PDPage page, final int pageNum) {
				System.out.println("beginPage");
			}
			
			@Override
			public void endPage(final PDDocument doc, final PDPage page, final int pageNum) {
				System.out.println("EndPage");
				var index = doc.getPages().indexOf(page);
				PDPage index2 = doc.getPage(pageNum);
				System.out.println("PageNum: " + pageNum + " index of page : " + index + " index number 2 toString():" + index2.toString());
			}
			
			@Override
			public void endDocument(final PDDocument doc) {
				System.out.println("EndDocument");
				var endDoc = doc.getPages().getCount();
				System.out.println("Here endDoc: " + endDoc);
			}
			
			@Override
			public boolean shouldCensorText(final TextPosition pos) {
				return false;
			}
		});
		File file = new File(path);
		PDDocument doc;
		try {
			doc = PDDocument.load(file);
			tp.endDocument(doc);
		} catch (IOException | NullPointerException e) { // At yet throws NullPointerException because no PDFStreamProcessor
			e.printStackTrace();
		}
	}
}
