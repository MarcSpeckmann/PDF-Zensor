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

/** CLArgsTest should contain all unit-tests related to {@link CLArgs}. */
class ExpressionTest {

	/** Checks if the constructor throws a <code>NullPointerException</code> if the input-regex is null.*/
	@SuppressWarnings("ConstantConditions")
	@Test
	void testConstructor() {
		assertThrows(NullPointerException.class, () -> new Expression(null, "#ffffff"));
	}

	/** Checks if the constructor can handle the initialization when the color is <code>null</code>.*/
	@Test
	void testNullColor() {
		var nullColor = new Expression("regex", null);
		assertEquals("regex", nullColor.getRegex());
		assertNull(nullColor.getColor());
	}

	/** Checks if the constructor can handle the initialization when the color is valid.*/
	@ParameterizedTest
	@ArgumentsSource(ColorProvider.class)
	void testValidColor(@NotNull String[] colorCodes, Color color) {
		var colorCode = "#" + colorCodes[0];
		var exp = new Expression("regex", colorCode);
		assertEquals("regex", exp.getRegex());
		assertEquals(Utils.getColorOrNull(colorCode), exp.getColor());
		assertDoesNotThrow(exp::toString);
	}

	/** Checks if the constructor can handle the initialization when the color is invalid.*/
	//More rigorous testing for this is done in UtilsTest
	@ParameterizedTest
	@ValueSource(strings = {"#", "ffffff", "fffffz"})
	void testIllegalConstructorArguments(String color) {
		assertThrows(IllegalArgumentException.class, () -> new Expression("regex", color));
	}
	
}