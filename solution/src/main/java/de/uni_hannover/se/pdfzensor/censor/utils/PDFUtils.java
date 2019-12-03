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
import java.io.IOException;
import java.util.Objects;

/**
 * PDFUtils is a specialized utility-class to provide short helper-functions centered around PDF-files.
 */
public final class PDFUtils {
	
	/**
	 * No instance of PDFUtils should be created. Thus it will always throw an exception.
	 */
	@Contract(value = " -> fail", pure = true)
	private PDFUtils() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Translates the given {@link PDRectangle} into a corresponding {@link Rectangle2D.Float}. Please note that the
	 * translation technically shifts the rectangle along the y-axis since Rectangle2D is created using the upper left
	 * corner to anchor it whereas PDRectangle provides the lower left corner.
	 *
	 * @param rect A rectangle from type PDRectangle with height, width and position
	 * @return A rectangle from type Rectangle2D with the properties of the input rectangle
	 */
	//TODO: the provided rect technically is shifted. It remains to be seen if we should negate that to work best with transformTextPosition.
	@NotNull
	@Contract("_ -> new")
	static Rectangle2D pdRectToRect2D(@NotNull PDRectangle rect) {
		return new Rectangle2D.Float(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
	}
	
	
	/**
	 * Generates a rectangle from type Rectangle2D with the height, width and position of a given text line
	 *
	 * @param pos A TextPosition representing a string and a position of characters on the screen
	 * @return A rectangle from type Rectangle2D with the properties of height, width and position from the TextPosition
	 * @throws IOException if the font could not be loaded correctly.
	 */
	public static Rectangle2D transformTextPosition(@NotNull TextPosition pos) throws IOException {
		Objects.requireNonNull(pos);
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
	}
	
}
