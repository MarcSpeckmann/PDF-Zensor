package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.text.TextPosition;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * PDFUtils is a utility class for the censor package
 */
final class PDFUtils {
	
	/**
	 * Constructor of PDFUtils not usable!
	 */
	@Contract(value = " -> fail", pure = true)
	private PDFUtils() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Generates a Rectangle2D element with the properties of a PDRectangle
	 *
	 * @param rect A rectangle from type PDRectangle with height, width and position
	 * @return A rectangle from type Rectangle2D with the properties of the input rectangle
	 */
	@NotNull
	@Contract("_ -> new")
	static Rectangle2D pdRectToRect2D(@NotNull PDRectangle rect) {
		return new Rectangle2D.Float(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
	}
	
	
	/**
	 * Generates a rectangle from type Rectangle2D with the height, width and position of a given text line
	 *
	 * @param pos A TextPosition representing a string and a position of characters on the screen
	 * @return A rectangle from type Rectangle2D with the properties of height, width and position from the
	 * TextPosition or returns a Rectangle2D with height, width and position equals 0 if an error occurred
	 */
	private static Rectangle2D transformTextPosition(TextPosition pos) {
		try {
			PDFont font = pos.getFont();
			BoundingBox bb = font.getBoundingBox();
			int totalWidth = 0;        // total width of all characters in this line
			for (int i : pos.getCharacterCodes())
				totalWidth += font.getWidth(i);
			AffineTransform at = pos.getTextMatrix().createAffineTransform();
			if (font instanceof PDType3Font)    // specific type of font
				at.concatenate(font.getFontMatrix().createAffineTransform());
			else at.scale(1 / 1000f, 1 / 1000f);
			Rectangle2D r = new Rectangle2D.Float(0, 0, totalWidth, bb.getHeight() + bb.getLowerLeftY());
			Shape s = at.createTransformedShape(r);
			return s.getBounds2D();
		} catch (Exception ignored) {/**/}
		return new Rectangle2D.Float(0, 0, 0, 0); // if an error occurred
	}
	
}
