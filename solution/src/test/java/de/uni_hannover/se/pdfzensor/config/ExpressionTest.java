package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.utils.Utils;
import de.uni_hannover.se.pdfzensor.utils.UtilsTest;
import org.junit.jupiter.api.Test;

import static de.uni_hannover.se.pdfzensor.testing.argumentproviders.ColorProvider.COLORS;
import static org.junit.jupiter.api.Assertions.*;

class ExpressionTest {
	
	@SuppressWarnings("ConstantConditions")
	@Test
	void testConstructor() {
		assertThrows(NullPointerException.class, () -> new Expression(null, "#ffffff"));
		//More rigorous testing for this is done in UtiltsTest
		assertThrows(IllegalArgumentException.class, () -> new Expression("regex", "#"));
		assertThrows(IllegalArgumentException.class, () -> new Expression("regex", "ffffff"));
		assertThrows(IllegalArgumentException.class, () -> new Expression("regex", "fffffz"));
		
		var nullColor = new Expression("regex", null);
		assertEquals("regex", nullColor.getRegex());
		assertNull(nullColor.getColor());
		
		for (var pair : COLORS.entrySet()) {
			var exp = new Expression("regex", "#" + pair.getValue()[0]);
			assertEquals("regex", exp.getRegex());
			assertEquals(Utils.getColorOrNull("#" + pair.getValue()[0]), exp.getColor());
			assertDoesNotThrow(exp::toString);
		}
		
		
	}
	
}