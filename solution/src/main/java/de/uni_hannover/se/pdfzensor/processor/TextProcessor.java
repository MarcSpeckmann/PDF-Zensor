package de.uni_hannover.se.pdfzensor.processor;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.List;


import static org.apache.pdfbox.contentstream.operator.OperatorName.SHOW_TEXT;
import static org.apache.pdfbox.contentstream.operator.OperatorName.SHOW_TEXT_ADJUSTED;
// TODO: Implement TextProcessor

public class TextProcessor extends PDFStreamProcessor {
	private PDFHandler handler;
	private boolean removedLastTextPosition = false;
	TextProcessor(PDFHandler handler) throws IOException {
		super();
		this.handler = handler;
	}
	
	@Override
	protected void startDocument(final @NotNull PDDocument document) throws IOException {
		super.startDocument(document);
		handler.beginDocument(document);
	}
	
	@Override
	protected void startPage(final @NotNull PDPage page) throws IOException {
		super.startPage(page);
		handler.beginPage(document, page, document.getPages().indexOf(page));
	}
	
	@Override
	protected void processTextPosition(final TextPosition text) {
		removedLastTextPosition = handler.shouldCensorText(text);
		super.processTextPosition(text);
	}
	
	@Override
	protected void endPage(final PDPage page) throws IOException {
		super.endPage(page);
		handler.endPage(document, page, document.getPages().indexOf(page));
	}
	
	@Override
	protected void endDocument(final PDDocument document) throws IOException {
		super.endDocument(document);
		handler.endDocument(document);
	}
	
	@Override
	protected void processOperator(final Operator operator, final List<COSBase> operands) throws IOException {
		ContentStreamWriter writer = getCurrentContentStream();
		if (!StringUtils.equalsAny(operator.getName(), SHOW_TEXT_ADJUSTED, SHOW_TEXT)){
			assert writer != null;
			writer.writeToken(operator);
			writer.writeTokens(operands);
		}
		super.processOperator(operator, operands);
		if (!StringUtils.equalsAny(operator.getName(), SHOW_TEXT_ADJUSTED, SHOW_TEXT) && !removedLastTextPosition){
			assert writer != null;
			writer.writeToken(operator);
			writer.writeTokens(operands);
		}
	}
	
	public static void main(String[] args) throws IOException {
		TextProcessor tp = new TextProcessor(new PDFHandler() {
			@Override
			public void beginDocument(final PDDocument doc) {
			
			}
			
			@Override
			public void beginPage(final PDDocument doc, final PDPage page, final int pageNum) {
			
			}
			
			@Override
			public void endPage(final PDDocument doc, final PDPage page, final int pageNum) {
			
			}
			
			@Override
			public void endDocument(final PDDocument doc) {
			
			}
			
			@Override
			public boolean shouldCensorText(final TextPosition pos) {
				return false;
			}
		});
	}
	
}
