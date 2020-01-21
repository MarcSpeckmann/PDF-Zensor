package de.uni_hannover.se.pdfzensor.censor;

import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFHandler;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.PDFCensorBoundingBoxProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.apache.pdfbox.contentstream.operator.OperatorName.DRAW_OBJECT;
import static org.junit.jupiter.api.Assertions.fail;

class PDFCensorTest implements PDFHandler {
	/** Acts as a super instance. */
	private PDFCensor properCensor;
	
	/**
	 * The number of processed TextPositions. Does <b>not</b> equal the size of the bounds-color pair list, since
	 * elements' bounds might get combined.
	 */
	private int element;
	
	/** The number of total elements the bounds-pair list should contain after all combinations. */
	private int finalExpectedElements;
	
	/** The bounds of the individual elements of the PDF-file (not combined). */
	private Rectangle2D.Double[] elements;
	
	/**
	 * Returns the list of bounds-color pairs of the instance.
	 *
	 * @param fromInstance The instance from which the list should be retrieved.
	 * @return The bounds-color pair list of the given instance.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static List<ImmutablePair<Rectangle2D, Color>> getBoundingBoxes(@NotNull PDFCensor fromInstance) {
		try {
			var boundingBoxesField = PDFCensor.class.getDeclaredField("boundingBoxes");
			boundingBoxesField.setAccessible(true);
			return (List<ImmutablePair<Rectangle2D, Color>>) boundingBoxesField.get(fromInstance);
		} catch (Exception e) {
			Assertions.fail("Could not retrieve the bounds-color pair list.", e);
		}
		return null;
	}
	
	/** Checks for invalid settings argument. */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testInvalidSettings() {
		Assertions.assertThrows(NullPointerException.class, () -> new PDFCensor(null));
	}
	
	/**
	 * Checks if the elements in the PDF-file equal the given elements and are added to the bounds-color-list
	 * correctly.
	 *
	 * @param input                 The input PDF-file to check.
	 * @param elements              The elements in the input PDF-file.
	 * @param finalExpectedElements The expected length of the bounds-pair list at the end of a page after all
	 *                              combinations have been applied.
	 * @throws IOException If the document could not be loaded.
	 */
	@ParameterizedTest(name = "Run {index}: pdf: {0}, elements: {1}, finalExpectedElements {2}")
	@ArgumentsSource(PDFCensorBoundingBoxProvider.class)
	void testPDFCensor(@NotNull String input, @NotNull Rectangle2D.Double[] elements,
					   int finalExpectedElements) throws IOException {
		var dummySettings = new Settings(input);
		this.properCensor = new PDFCensor(dummySettings);
		this.elements = elements;
		this.element = 0;
		this.finalExpectedElements = finalExpectedElements;
		
		final var dummyProcessor = new PDFProcessor(this);
		try (final var doc = PDDocument.load(dummySettings.getInput())) {
			dummyProcessor.process(doc);
		}
	}
	
	/**
	 * This tests checks if after processing the PDF Document all DrawObject operator are removed
	 *
	 * @param input The input PDF-file to check.
	 * @throws IOException If the document could not be loaded.
	 */
	@ParameterizedTest(name = "Run {index}: pdf: {0}")
	@ValueSource(strings = {"src/test/resources/pdf-files/cusatop-intro.pdf",
			"src/test/resources/pdf-files/formAndTransparencyGroup.pdf",
			"src/test/resources/pdf-files/sample.pdf"})
	void testRemoveDrawObject(@NotNull String input) throws IOException {
		
		var engine = new PDFStreamEngine() {
			@Override
			protected void processOperator(final Operator operator, final List<COSBase> operands) {
				if (DRAW_OBJECT.equals(operator.getName())) {
					fail("Not all Images are removed");
				}
			}
		};
		
		var settings = new Settings(input);
		var censor = new PDFCensor(settings);
		
		final var processor = new PDFProcessor(censor);
		final var doc = PDDocument.load(settings.getInput());
		processor.process(doc);
		
		for (var page : doc.getPages()) {
			engine.processPage(page);
		}
	}
	
	@Override
	public void beginDocument(final PDDocument doc) {
		Objects.requireNonNull(properCensor);
		Assertions.assertNull(getBoundingBoxes(properCensor));
		
		properCensor.beginDocument(doc);
		
		var listAfter = getBoundingBoxes(properCensor);
		Assertions.assertNotNull(listAfter);
		Assertions.assertTrue(listAfter.isEmpty());
	}
	
	@Override
	public void beginPage(final PDDocument doc, final PDPage page, final int pageNum) {
		Objects.requireNonNull(properCensor);
		
		properCensor.beginPage(doc, page, pageNum);
		
		var listAfter = getBoundingBoxes(properCensor);
		Assertions.assertNotNull(listAfter);
		Assertions.assertTrue(listAfter.isEmpty());
	}
	
	@Override
	public void endPage(final PDDocument doc, final PDPage page, final int pageNum) {
		Objects.requireNonNull(properCensor);
		Assertions.assertNotNull(getBoundingBoxes(properCensor));
		// Checks if the expected number of elements have been combined
		// (requires colors to be added because differently colored elements should not be combined)
		
		//TODO: adjust to the tokenized censoring
		//This test would not and should not necessarily work with the tokenized censoring
		//Assertions.assertEquals(finalExpectedElements, Objects.requireNonNull(getBoundingBoxes(properCensor)).size());
		
		properCensor.endPage(doc, page, pageNum);
		try {
			Assertions.assertTrue(page.getAnnotations().isEmpty());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
		
	}
	
	@Override
	public void endDocument(final PDDocument doc) {
		Objects.requireNonNull(properCensor);
		Assertions.assertNotNull(getBoundingBoxes(properCensor));
		
		properCensor.endDocument(doc);
		
		Assertions.assertNull(getBoundingBoxes(properCensor));
	}
	
	@Override
	public boolean shouldCensorText(PDPage page, final TextPosition pos) {
		Objects.requireNonNull(properCensor);
		Assertions.assertTrue(element < elements.length, "Not all elements were listed for comparison.");
		
		var listBefore = Objects.requireNonNull(getBoundingBoxes(properCensor));
		var sizeBefore = listBefore.size();
		var oldLast = (sizeBefore > 0) ? listBefore.get(sizeBefore - 1) : null;
		
		var actual = properCensor.shouldCensorText(page, pos);
		
		var listAfter = Objects.requireNonNull(getBoundingBoxes(properCensor));
		var sizeAfter = listAfter.size();
		
		//TODO: this part has to be rewritten and adjusted to the now tokenized censoring
		//This assertion is not true anymore since with the tokenizer censor-bars are not added directly.
		//Assertions.assertTrue(sizeAfter > 0);
		//var newLast = listAfter.get(sizeAfter - 1);
		
		//TODO: this too
		//Assertions.assertTrue(sizeAfter > 0);
		//var newLast = listAfter.get(sizeAfter - 1);
		
		// Colors differ, expect element to be added instead of combined.
		//if (oldLast != null && !oldLast.getRight().equals(newLast.getRight()))
		//	Assertions.assertEquals(sizeBefore + 1, sizeAfter);
		
		//var expBounds = elements[element];
		//if (sizeBefore == sizeAfter) // element was extended
		//	expBounds = (Rectangle2D.Double) elements[element].createUnion(oldLast.getLeft());
		//Assertions.assertTrue(checkRectanglesEqual(expBounds, newLast.getLeft()));
		
		element++;
		return actual;
	}
}