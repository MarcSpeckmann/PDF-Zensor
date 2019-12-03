package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import org.apache.pdfbox.util.Matrix;
import org.jetbrains.annotations.NotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link PDFUtils#transformTextPosition(TextPosition)} and {@link PDFUtils#pdRectToRect2D(PDRectangle)}
 */
class PDFUtilsTest {
	
	
	/**
	 * New data structure TextPositionValue to bundle the values for transformTextPosition
	 */
	static class TextPositionValue {
		float endX;
		float endY;
		float fontSize;
		int fontSizeInPt;
		float maxHeight;
		float individualWidth;
		float spaceWidth;
		String unicode;
		int[] charCodes;
		
		TextPositionValue(float endX, float endY, float fontSize, int fontSizeInPt, float maxHeight, float individualWidth, float spaceWidth, String unicode, int[] charCodes) {
			this.endX = endX;
			this.endY = endY;
			this.fontSize = fontSize;
			this.fontSizeInPt = fontSizeInPt;
			this.maxHeight = maxHeight;
			this.individualWidth = individualWidth;
			this.spaceWidth = spaceWidth;
			this.unicode = unicode;
			this.charCodes = charCodes;
		}
	}
	
	/**
	 * A Hash Map containing bundled data as TextPosition objects and the corresponding expected output-rectangle
	 */
	private static final Map<Rectangle2D, TextPositionValue> TEXTPOSITION = new HashMap<>();
	
	private static TextPositionValue tpValue1 = new TextPositionValue(79.19946f, 800.769f, 10.9091f, 10, 7.51637f, 8.333466f, 3.0545478f, "D", new int[]{68});
	private static TextPositionValue tpValue2 = new TextPositionValue(23.1547f, 44.32212f, 11.95f, 11, 7.51637f, 8.333466f, 3.0545478f, "DE", new int[]{68, 69});
	
	static {
		TEXTPOSITION.put(new Rectangle2D.Float(tpValue1.endX, tpValue1.endY, 7.876370270042557f, 9.796371887116607f), tpValue1);
		TEXTPOSITION.put(new Rectangle2D.Float(tpValue2.endX, tpValue2.endY, 15.929350502353941f, 10.731100338419985f), tpValue2);
	}
	
	
	/**
	 * Provides a set of arguments for {@link #transformTextPositionTest(TextPositionValue, Rectangle2D)} generated from {@link #TEXTPOSITION}.
	 */
	private static Stream<Arguments> textPositionProvider() {
		return TEXTPOSITION.entrySet().stream().map(e -> Arguments.of(e.getValue(), e.getKey()));
	}
	
	@ParameterizedTest(name = "Run {index}: TextPosition: {0}")
	@MethodSource("textPositionProvider")
	void transformTextPositionTest(@NotNull TextPositionValue input, @NotNull Rectangle2D expected) {
		TextPosition tp = new TextPosition(0, 595.276f, 841.89f, new Matrix(input.fontSize, 0f, 0f, input.fontSize, input.endX, input.endY), input.endX, input.endY, input.maxHeight, input.individualWidth, input.spaceWidth, input.unicode, input.charCodes, PDType1Font.TIMES_ROMAN, input.fontSize, input.fontSizeInPt);
		try {
			assertEquals(Math.round(expected.getHeight()), Math.round(Objects.requireNonNull(PDFUtils.transformTextPosition(tp)).getHeight()));
			assertEquals(Math.round(expected.getWidth()), Math.round(Objects.requireNonNull(PDFUtils.transformTextPosition(tp)).getWidth()));
			assertEquals(Math.round(expected.getX()), Math.round(Objects.requireNonNull(PDFUtils.transformTextPosition(tp)).getX()));
			assertEquals(Math.round(expected.getY()), Math.round(Objects.requireNonNull(PDFUtils.transformTextPosition(tp)).getY()));
		} catch (IOException e) {
			fail("IOException: font of TextPosition object couldn't be loaded correctly");
		}
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