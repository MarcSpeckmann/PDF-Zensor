package de.uni_hannover.se.pdfzensor.censor;

import de.uni_hannover.se.pdfzensor.censor.utils.Annotations;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFHandler;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.PDFCensorMarkedArgumentProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.censor.utils.PDFUtils.transformTextPosition;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.*;

class PDFCensorMarkedTest implements PDFHandler {
	/** Acts as a super instance. */
	private PDFCensor properCensor;
	
	/**
	 * index to iterate over the textPosition that should be censored
	 */
	private int currTextPosition;
	
	/** The number of total elements the bounds-pair list should contain after all combinations. */
	private int combinedBoundingBoxesNr;
	
	/** The BoundingBoxes of the individual elements of the PDF-file (not combined). */
	private Rectangle2D.Double[] uncombinedBoundingBoxes;
	
	/**
	 * Checks if the elements in the PDF-file equals the given elements and are added to the bounds-color-list
	 * correctly.
	 *
	 * @param input                   The input PDF-file to check.
	 * @param uncombinedBoundingBoxes The rectangle of the TextPosition in the input PDF-file that has to be censored at
	 *                                the end.
	 * @param combinedBoundingBoxesNr The expected length of the bounds-pair list at the end of a page after all
	 *                                combinations have been applied.
	 * @throws IOException If the document could not be loaded.
	 */
	@ParameterizedTest(name = "Run {index}: pdf: {0}, elements: {1}, finalExpectedElements {2}")
	@ArgumentsSource(PDFCensorMarkedArgumentProvider.class)
	/* Ignore SonarLint error because the constructor is for test cases only and in <code>dummyProcessor.process(doc)</code> */
	void testPDFCensor(@NotNull String[] input, @NotNull Rectangle2D.Double[] uncombinedBoundingBoxes,
					   int combinedBoundingBoxesNr) throws IOException {
		
		var dummySettings = new Settings(null, input);
		this.properCensor = new PDFCensor(dummySettings);
		this.currTextPosition = 0;
		this.uncombinedBoundingBoxes = uncombinedBoundingBoxes;
		this.combinedBoundingBoxesNr = combinedBoundingBoxesNr;
		
		final var dummyProcessor = new PDFProcessor(this);
		final var doc = PDDocument.load(dummySettings.getInput());
		dummyProcessor.process(doc);
		doc.close();
	}
	
	/**
	 * a mask function so that the code remains cleaner
	 */
	private List<ImmutablePair<Rectangle2D, Color>> getBoundingBoxes() {
		/* Ignore warning because we can not create a class with generic attributes */
		return getPrivateField(PDFCensor.class, this.properCensor, "boundingBoxes");
	}
	
	/**
	 * a mask function so that the code remains cleaner
	 */
	private Annotations getAnnotation() {
		return getPrivateField(PDFCensor.class, this.properCensor, "annotations");
	}
	
	/**
	 * checks if a element isn't marked
	 *
	 * @return true if the TextPosition is not marked up
	 */
	private boolean isMarked(@NotNull final TextPosition pos) {
		boolean excepted = false;
		try {
			excepted = getAnnotation().isMarked(transformTextPosition(pos));
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
		return excepted;
	}
	
	/**
	 * extends the inherited functions with tests
	 */
	@Override
	public void beginDocument(final PDDocument doc) {
		Objects.requireNonNull(properCensor);
		
		/* checks that the list has not been initialized yet */
		Assertions.assertNull(getBoundingBoxes(), "boundingBoxes should not be initialized before the document start");
		
		properCensor.beginDocument(doc);
		
		var list = getBoundingBoxes();
		Assertions.assertNotNull(list, "boundingBoxes should be initialized after the document start");
		
		Assertions.assertTrue(list.isEmpty(), "boundingBoxes should be empty at the beginning");
	}
	
	/**
	 * extends the inherited functions with tests
	 */
	@Override
	public void beginPage(final PDDocument doc, final PDPage page, final int pageNum) {
		Objects.requireNonNull(properCensor);
		
		properCensor.beginPage(doc, page, pageNum);
		
		var boundingBoxes = getBoundingBoxes();
		Assertions.assertNotNull(boundingBoxes,
								 "boundingBoxes should be initialized since the document has been started");
		Assertions.assertTrue(boundingBoxes.isEmpty(), "boundingBoxes should be empty at the beginning of each page");
	}
	
	/**
	 * extends the inherited functions with tests
	 */
	@Override
	public void endPage(final PDDocument doc, final PDPage page, final int pageNum) {
		Objects.requireNonNull(properCensor);
		
		var list = getBoundingBoxes();
		Assertions.assertNotNull(list, "boundingBoxes should be initialized since the document has been started");
		
		/* checks if the the list of the bounds is being combined correctly */
		Assertions.assertEquals(combinedBoundingBoxesNr, list.size(),
								"the number of the combined BoundingBoxes must equal the expected one");
		
		properCensor.endPage(doc, page, pageNum);
		
		try {
			/* checks whether the annotation has been deleted */
			Assertions.assertTrue(page.getAnnotations().isEmpty(), "the Annotation were not deleted");
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
		
	}
	
	/**
	 * extends the inherited functions with tests
	 */
	@Override
	public void endDocument(final PDDocument doc) {
		Objects.requireNonNull(properCensor);
		
		Assertions.assertNotNull(getBoundingBoxes(), "boundingBoxes should be initialized before the document is done");
		
		properCensor.endDocument(doc);
		
		Assertions.assertNull(getBoundingBoxes(), "boundingBoxes should be NULL after the document has been processed");
	}
	
	@Override
	public boolean shouldCensorText(final TextPosition pos) {
		Objects.requireNonNull(properCensor);
		
		/* before the TextPosition has been processed */
		var listBefore = getBoundingBoxes();
		int sizeBefore = listBefore.size();
		var lastBoundsBefore = (sizeBefore > 0) ? listBefore.get(sizeBefore - 1) : null;
		
		boolean actual = properCensor.shouldCensorText(pos);
		Assertions.assertEquals(isMarked(pos), actual, "a textPosition is misidentified");
		
		/* after the TextPosition has been processed */
		var listAfter = getBoundingBoxes();
		int sizeAfter = listAfter.size();
		var lastBoundsAfter = (sizeAfter > 0) ? listAfter.get(sizeAfter - 1) : null;
		/* tests when a new Box is being added */
		if (actual) {
			Assertions.assertTrue(sizeAfter > 0,
								  "the Bounding Boxes list must not be empty after a TextPosition has been censored");
			
			/* when the colors are different */
			if ((lastBoundsBefore != null) && !lastBoundsBefore.getRight().equals(lastBoundsAfter.getRight())) {
				Assertions.assertEquals(sizeBefore + 1, sizeAfter);
			}
			
			/* checks if the added Box get combined correctly */
			Rectangle2D.Double expBounds = uncombinedBoundingBoxes[currTextPosition];
			/* the last Box was extended*/
			if (sizeBefore == sizeAfter && lastBoundsBefore != null) {
				expBounds = (Rectangle2D.Double) uncombinedBoundingBoxes[currTextPosition]
						.createUnion(lastBoundsBefore.getLeft());
			}
			Assertions.assertTrue(checkRectanglesEqual(expBounds, lastBoundsAfter.getLeft(), EPSILON),
								  "a new Box should be added correctly");
			currTextPosition++;
		}
		return actual;
	}
}