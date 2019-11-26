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
import java.util.List;


import static org.apache.pdfbox.contentstream.operator.OperatorName.SHOW_TEXT;
import static org.apache.pdfbox.contentstream.operator.OperatorName.SHOW_TEXT_ADJUSTED;

/** The processor informs the handler about important events and transfers the documents.*/
public class TextProcessor extends PDFStreamProcessor {
	private PDFHandler handler;
	private boolean removedLastTextPosition = false;
	
	/**
	 * The processor informs the handler about important events and transfers the documents.
	 *
	 * @param handler the internal handler which acts to process the documents.
	 * @throws IOException if no superior class does not exist
	 */
	TextProcessor(PDFHandler handler) throws IOException {
		super();
		this.handler = handler;
	}
	
	/**
	 * Start the current document and transfer it to the handler for processing.
	 *
	 * @param document The PDF document that is being processed.
	 * @throws IOException if the document is in invalid state.
	 */
	@Override
	protected void startDocument(final @NotNull PDDocument document) throws IOException {
		super.startDocument(document);
		handler.beginDocument(document);
	}
	
	/**
	 * Start the current page and passed to the handler.
	 *
	 * @param page The page we are about to process.
	 * @throws IOException if the page is in invalid state.
	 */
	@Override
	protected void startPage(final @NotNull PDPage page) throws IOException {
		super.startPage(page);
		handler.beginPage(document, page, document.getPages().indexOf(page));
	}
	
	/**
	 * Checks whether the last position to be processed is reached.
	 * If yes removedTextPosition is set to true.
	 *
	 * @param text Text position to be processed.
	 */
	@Override
	protected void processTextPosition(final TextPosition text) {
		removedLastTextPosition = handler.shouldCensorText(text);
        super.processTextPosition(text);
	}
	
	/**
	 * End editing page and passed it to the handler.
	 *
	 * @param page The page we are just got processed.
	 * @throws IOException  If there is an error loading the properties.
	 */
	@Override
	protected void endPage(final PDPage page) throws IOException {
		handler.endPage(document, page, document.getPages().indexOf(page));
		super.endPage(page);
	}
	
	/**
	 * Ends the current document and give it to the Handler.
	 *
	 * @param document The PDF document that has been processed.
	 * @throws IOException if the document is in invalid state.
	 */
	@Override
	protected void endDocument(final PDDocument document) throws IOException {
		handler.endDocument(document);
		super.endDocument(document);
	}
	
	/**
	 * This is used to handle an operation.
	 *
	 * @param operator The operation to perform.
	 * @param operands The list of arguments.
	 * @throws IOException  If there is an error processing the operation.
	 */
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
}
