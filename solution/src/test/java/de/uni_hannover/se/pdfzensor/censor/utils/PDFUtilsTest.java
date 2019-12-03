package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.TestUtility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.util.Matrix;
import org.jetbrains.annotations.NotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFUtilsTest extends PDFTextStripper {
	
	/**
	 * Instantiate a new PDFTextStripper object.
	 *
	 * @throws IOException If there is an error loading the properties.
	 */
	PDFUtilsTest() throws IOException {
	}
	
	/**
	 * Override the writeString function just to get a textPosition element.
	 *
	 * @param string
	 * @param textPositions
	 */
	@Override
	protected void writeString(String string, @NotNull List<TextPosition> textPositions) {
		textPositions.stream().map(PDFUtils::transformTextPosition).forEach(System.out::println);
	}
	
	@Test
	void transformTestPositionTest() throws IOException {
		File file = new File(TestUtility.getResource("/pdf-files/sample.pdf").getAbsolutePath());
		var doc = PDDocument.load(file);
		PDFTextStripper stripper = new PDFUtilsTest();
		stripper.setSortByPosition(true);
		stripper.setStartPage(0);
		stripper.setEndPage(doc.getNumberOfPages());
		Writer dummey = new OutputStreamWriter(new ByteArrayOutputStream());
		stripper.writeText(doc, dummey);
	}
	
	/**
	 * Hash Map for 1: height,width and 2: position x, y
	 */
	private static final Map<Rectangle2D, PDRectangle> DIMENSIONS = new HashMap<>();
	
	static {
		// adds pairs of input rectangles (as {@link PDRectangle}) and output rectangles (as {@link Rectangle2D}).
		DIMENSIONS.put(new Rectangle2D.Float(0f, 0f, 0f, 0f), new PDRectangle(0f, 0f, 0f, 0f));
		DIMENSIONS.put(new Rectangle2D.Float(-1f, -2f, -3f, -4f), new PDRectangle(-1f, -2f, -3f, -4f));
		DIMENSIONS.put(new Rectangle2D.Float(1f, 2f, 3f, 4f), new PDRectangle(1f, 2f, 3f, 4f));
		DIMENSIONS.put(new Rectangle2D.Float(1.5f, 2.5f, 3.5f, 4.5f), new PDRectangle(1.5f, 2.5f, 3.5f, 4.5f));
	}
	
	/**
	 * Provides a set of arguments for {@link #pdRectToRect2DTest(PDRectangle, Rectangle2D)} generated from {@link #DIMENSIONS}.
	 */
	private static Stream<Arguments> dimensionsProvider() {
		return DIMENSIONS.entrySet().stream().map(e -> Arguments.of(e.getValue(), e.getKey()));
	}
	
	@ParameterizedTest(name = "Run {index}: Dimensions: {0}")
	@MethodSource("dimensionsProvider")
	void pdRectToRect2DTest(@NotNull PDRectangle input, @NotNull Rectangle2D expected) {
		assertEquals(expected, PDFUtils.pdRectToRect2D(input));
	}
}