package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ColorProvider;
import de.uni_hannover.se.pdfzensor.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/** ExpressionTest should contain all unit-tests related to {@link Expression}. */
class ExpressionTest {
	
	/** Checks if the constructor throws a <code>NullPointerException</code> if the input-regex is null. */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testConstructor() {
		assertThrows(NullPointerException.class, () -> new Expression(null, "#ffffff"));
	}
	
	/**
	 * Checks if the constructor can handle the initialization so its output will be DEFAULT_CENSOR_COLOR when the
	 * input-color is <code>null</code>.
	 */
	@Test
	void testNullColor() {
		var nullColor = new Expression("regex", (Color) null);
		assertEquals("regex", nullColor.getRegex());
		assertEquals(Settings.DEFAULT_CENSOR_COLOR, nullColor.getColor());
		
		nullColor = new Expression("regex", (String) null);
		assertEquals("regex", nullColor.getRegex());
		assertEquals(Settings.DEFAULT_CENSOR_COLOR, nullColor.getColor());
	}
	
	/**
	 * Checks if the constructor can handle the initialization when the color is valid.
	 *
	 * @param colorCode A valid hexadecimal color code string.
	 * @param color     The color which is expected to be set.
	 */
	@ParameterizedTest
	@ArgumentsSource(ColorProvider.class)
	void testValidColor(@NotNull String colorCode, Color color) {
		var exp = new Expression("regex", colorCode);
		assertEquals("regex", exp.getRegex());
		assertEquals(color, exp.getColor());
		assertDoesNotThrow(exp::toString);
	}
	
	/**
	 * Checks if the constructor can handle the initialization when the color is invalid. More rigorous testing for this
	 * is done in UtilsTest.
	 *
	 * @param color The invalid color code which is expected to throw an {@link IllegalArgumentException} when used for
	 *              constructing an Expression.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#", "ffffff", "fffffz"})
	void testIllegalConstructorArguments(String color) {
		assertThrows(IllegalArgumentException.class, () -> new Expression("regex", color));
	}
	
	/**
	 * Tests if {@link Expression#setColor(Color)} and {@link Expression#getColor()} act as expected.
	 *
	 * @param color A valid color code.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#FFF", "0xabcdef", "0X123456"})
	void testSetColor(String color) {
		var exp = new Expression("reg", (Color) null);
		var colorBefore = exp.getColor();
		assertFalse(exp.setColor(null));
		assertEquals(colorBefore, exp.getColor());
		assertTrue(exp.setColor(Utils.getColorOrNull(color)));
		assertEquals(Utils.getColorOrNull(color), exp.getColor());
		assertFalse(exp.setColor(Utils.getColorOrNull(color)));
	}
}