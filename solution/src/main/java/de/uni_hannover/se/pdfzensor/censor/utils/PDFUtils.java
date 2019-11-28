package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;

public final class PDFUtils {
	
	@Contract(value = " -> fail", pure = true)
	private PDFUtils() {
	
	}
	
	@NotNull
	@Contract("_ -> new")
	static Rectangle2D pdrectToRect2D(@NotNull PDRectangle rect) {
		return null;
	}
	
	public static Rectangle2D transformTextPosition(TextPosition pos) {
		return null;
	}
	
}
