package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Boolean.TRUE;
import static org.apache.pdfbox.contentstream.operator.OperatorName.*;


/**
 * TextProcessor has two main purposes: for one is it responsible to abstract {@link
 * org.apache.pdfbox.text.PDFTextStripper}'s {@link org.apache.pdfbox.text.PDFTextStripper#startDocument(PDDocument)}
 * and similar methods to the API outside of this package by forwarding these events to a {@link PDFHandler}. For the
 * other is it responsible to copy all read operators into {@link PDFStreamProcessor}'s builtin output-stream. The
 * latter has as only exception the Show-Text-Operators (TJ and Tj) as they should only be copied if the callback to
 * {@link PDFHandler#shouldCensorText(TextPosition)} had returned false.
 */
public class TextProcessor extends PDFStreamProcessor {
	private static final Logger LOGGER = Logging.getLogger();
	private PDFHandler handler;
	private List<Boolean> shouldBeCensored = new ArrayList<>();
	
	/**
	 * The processor informs the handler about important events and transfers the documents.
	 *
	 * @param handler the internal handler which acts to process the documents.
	 * @throws IOException if object of superior class does not exist
	 */
	TextProcessor(PDFHandler handler) throws IOException {
		super();
		if (handler == null)
			LOGGER.log(Level.ERROR, "Handler is null");
		this.handler = Objects.requireNonNull(handler);
		
	}
	
	/**
	 * Transforms the provided COSString into an COSArray where each character is added if it should not be censored. If
	 * a character should be censored its text-adjustment is added.
	 *
	 * @param string the string that should be transformed into a COSArray that may be used for TJ-operations.
	 * @param font   the current font. It is used to provide the size-informationabout characters.
	 * @param censor a list of booleans to check if each character should be censored. This list will be modified.
	 * @return a COSArray representing the censored string as a TJ-operand.
	 */
	@NotNull
	private static COSArray removeCharsFromString(COSString string, PDFont font, @NotNull List<Boolean> censor) {
		var newOperands = new COSArray();
		try (var is = new ByteArrayInputStream(string.getBytes())) {
			while (is.available() > 0 && !censor.isEmpty()) {
				int before = is.available();
				int code = font.readCode(is);
				int after = is.available();
				
				if (TRUE.equals(censor.remove(0))) {
					var tj = -font.getWidth(code);
					if (font.isVertical())
						tj = -font.getHeight(code);
					newOperands.add(new COSFloat(tj));
				} else {
					int startIndex = string.getBytes().length - before;
					int endIndex = string.getBytes().length - after;
					var data = ArrayUtils.subarray(string.getBytes(), startIndex, endIndex);
					newOperands.add(new COSString(data));
				}
			}
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return newOperands;
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
	 * Ends the current document and gives it to the Handler.
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
	 * Start the current page and pass it to the handler.
	 *
	 * @param page The page we are about to process.
	 * @throws IOException if the page is in invalid state.
	 */
	@Override
	protected void startPage(final @NotNull PDPage page) throws IOException {
		super.startPage(page);
		handler.beginPage(document, page, getCurrentPageNo());
	}
	
	/**
	 * End editing page and pass it to the handler.
	 *
	 * @param page The page we just got processed.
	 * @throws IOException If there is an error loading the properties.
	 */
	@Override
	protected void endPage(final PDPage page) throws IOException {
		super.endPage(page);
		handler.endPage(document, page, getCurrentPageNo());
	}
	
	/**
	 * Checks whether the current text should be censored. If so, shouldBeCensored is set to true.
	 *
	 * @param text Text position to be processed.
	 */
	@Override
	protected void processTextPosition(final TextPosition text) {
		shouldBeCensored.add(handler.shouldCensorText(text));
		super.processTextPosition(text);
	}
	
	/**
	 * Used to handle an operation. SHOW_TEXT_ADJUSTED and SHOW_TEXT are operators for text in the PDF structure. The
	 * function copies everything that is not defined as text in the PDF structure. Then the processOperator implemented
	 * in the {@link org.apache.pdfbox.text.PDFTextStripper} is called which calls shouldCensored to decide if text
	 * should be censored or not. In shouldCensored a bool is stored to decide if text should be censored or not.
	 *
	 * @param operator The operation to perform.
	 * @param operands The list of arguments.
	 * @throws IOException If there is an error processing the operation.
	 */
	@Override
	protected void processOperator(final Operator operator, final List<COSBase> operands) throws IOException {
		if (StringUtils
				.equalsAny(operator.getName(), SHOW_TEXT_LINE, SHOW_TEXT_LINE_AND_SPACE, MOVE_TEXT_SET_LEADING)) {
			super.processOperator(operator, operands);
			return;
		}
		ContentStreamWriter writer = Objects.requireNonNull(getCurrentContentStream());
		shouldBeCensored.clear();
		if (!StringUtils.equalsAny(operator.getName(), SHOW_TEXT_ADJUSTED, SHOW_TEXT)) {
			writer.writeTokens(operands);
			writer.writeToken(operator);
		}
		super.processOperator(operator, operands);
		if (StringUtils.equalsAny(operator.getName(), SHOW_TEXT_ADJUSTED, SHOW_TEXT)) {
			COSArray newOperands;
			if (SHOW_TEXT.equals(operator.getName()))
				newOperands = removeCharsFromText(operands, shouldBeCensored);
			else newOperands = removeCharsFromTextAdjusted(operands, shouldBeCensored);
			writer.writeToken(newOperands);
			writer.writeToken(Operator.getOperator(SHOW_TEXT_ADJUSTED));
		}
	}
	
	/**
	 * Removes the chars that should be censored from the operands and replaces them by their widths (height for
	 * vertical fonts). The resulting COSArray may be used as an operand for a TJ-operation.
	 *
	 * @param operands the operand of a Tj call that should be transformed into the censored operands for a TJ call.
	 * @param censor   a list containing information about what character should be censored. For each censored
	 *                 character the first element of the list is deleted such that the first element always shows if
	 *                 the next character should be censored.
	 * @return a TJ-operand for drawing the censored string.
	 */
	@NotNull
	private COSArray removeCharsFromText(@NotNull List<COSBase> operands, List<Boolean> censor) {
		var font = getGraphicsState().getTextState().getFont();
		return removeCharsFromString((COSString) operands.get(0), font, censor);
	}
	
	/**
	 * Removes the chars that should be censored from the operands and replaces them by their widths (height for
	 * vertical fonts). The resulting COSArray may be used as an operand for a TJ-operation.
	 *
	 * @param operands the operand of a TJ call that should be transformed into the censored operands for a TJ call.
	 * @param censor   a list containing information about what character should be censored. For each censored
	 *                 character the first element of the list is deleted such that the first element always shows if
	 *                 the next character should be censored.
	 * @return a TJ-operand for drawing the censored string.
	 */
	@NotNull
	private COSArray removeCharsFromTextAdjusted(@NotNull List<COSBase> operands, List<Boolean> censor) {
		var font = getGraphicsState().getTextState().getFont();
		var newOperands = new COSArray();
		
		var operand = (COSArray) operands.get(0);
		for (var op : operand) {
			if (op instanceof COSNumber) {
				newOperands.add(op);
			} else if (op instanceof COSString) {
				var ops = removeCharsFromString((COSString) op, font, censor);
				newOperands.addAll(ops);
			}
		}
		return newOperands;
	}
}
