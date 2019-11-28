package de.uni_hannover.se.pdfzensor.censor.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

enum MarkCriteria {
	INTERSECT(Rectangle2D::intersects), CONTAIN(Rectangle2D::contains);
	
	private final BiPredicate<Rectangle2D, Rectangle2D> predicate;
	
	@Contract(pure = true)
	MarkCriteria(BiPredicate<Rectangle2D, Rectangle2D> predicate) {
		this.predicate = predicate;
	}
	
	@NotNull
	@Contract(pure = true)
	Predicate<Rectangle2D> getPredicate(Rectangle2D other) {
		return null;
	}
}