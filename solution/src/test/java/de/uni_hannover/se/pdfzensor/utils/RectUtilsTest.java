package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.JoinedRectProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.geom.Rectangle2D;

import static de.uni_hannover.se.pdfzensor.testing.TestUtility.assertIsUtilityClass;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** RectUtilsTest should contain all unit-tests related to {@link RectUtils}. */
class RectUtilsTest {
	
	@Test
	void general() {
		assertIsUtilityClass(RectUtils.class);
	}
	
	/**
	 * Checks if {@link RectUtils#getRectBetween(Rectangle2D, Rectangle2D)} performs as expected for each argument
	 * provided by {@link JoinedRectProvider}.
	 *
	 * @param r1       the first input-rectangle.
	 * @param r2       the second input-rectangle.
	 * @param expected the expected output for {@link RectUtils#getRectBetween(Rectangle2D, Rectangle2D)}.
	 */
	@ParameterizedTest
	@ArgumentsSource(JoinedRectProvider.class)
	void testGetRectBetween(Rectangle2D r1, Rectangle2D r2, Rectangle2D expected) {
		assertEquals(expected, RectUtils.getRectBetween(r1, r2));
	}
	
}