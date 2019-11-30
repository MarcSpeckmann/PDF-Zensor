package de.uni_hannover.se.pdfzensor.censor.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import java.awt.geom.Rectangle2D;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class PDFUtilsTest {
	
	/** Hash Map for 1: height,width and 2: position x, y */
	private static final Map<Rectangle2D, PDRectangle> DIMENSIONS = new HashMap<>();
	
	static {
		// This adds paires of inputs (rectangles as PDRectangle) and outputs (rectangles as Rectangle2D).
		DIMENSIONS.put(new Rectangle2D.Float(0f,0f,0f,0f),
				new PDRectangle(0f, 0f, 0f, 0f));
		DIMENSIONS.put(new Rectangle2D.Float(-1f, -2f, -3f, -4f),
				new PDRectangle(-1f, -2f, -3f, -4f));
		DIMENSIONS.put(new Rectangle2D.Float(1f, 2f, 3f, 4f),
				new PDRectangle(1f, 2f, 3f, 4f));
		DIMENSIONS.put(new Rectangle2D.Float(1.5f, 2.5f, 3.5f, 4.5f),
				new PDRectangle(1.5f, 2.5f, 3.5f, 4.5f));
	}
	
	/** Provides a set of arguments for {@link #pdrectToRect2DTest(PDRectangle, Rectangle2D)} generated from {@link #DIMENSIONS}. */
	private static Stream<Arguments> dimensionsProvider() {
		return DIMENSIONS.entrySet()
				.stream()
				.map(e -> Arguments.of(e.getValue(), e.getKey()));
	}
	
	@ParameterizedTest(name = "Run {index}: Dimensions: {0}")
	@MethodSource("dimensionsProvider")
	void pdrectToRect2DTest(@NotNull PDRectangle input, Rectangle2D expected) {
		assertEquals(expected, PDFUtils.pdrectToRect2D(input));
	}
}