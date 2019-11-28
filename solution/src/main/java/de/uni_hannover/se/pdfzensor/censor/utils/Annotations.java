package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

public final class Annotations {
	@NotNull
	private List<Rectangle2D> highlights;
	@NotNull
	private List<Rectangle2D> links;
	
	public Annotations() {
		highlights = List.of();
		links = List.of();
	}
	
	@NotNull
	private static Rectangle2D getAnnotationRect(@NotNull PDAnnotation annotation) {
		return null;
	}
	
	public void cachePage(@NotNull PDPage page) {
	
	}
	
	private void cacheLinks(@NotNull PDPage page) {
		Objects.requireNonNull(page);
	}
	
	private void cacheHighlights(@NotNull PDPage page) {
		Objects.requireNonNull(page);
	}
	
	public boolean isMarked(@NotNull Rectangle2D rect) {
		return true;
	}
	
	boolean isMarked(@NotNull Rectangle2D rect, @NotNull MarkCriteria criteria) {
		Objects.requireNonNull(rect);
		Objects.requireNonNull(criteria);
		return true;
	}
	
	boolean isLinked(@NotNull Rectangle2D rect) {
		return true;
	}
	
	boolean isLinked(@NotNull Rectangle2D rect, @NotNull MarkCriteria criteria) {
		Objects.requireNonNull(rect);
		Objects.requireNonNull(criteria);
		return true;
	}
	
	@Contract("null -> false")
	private static boolean isHighlightAnnotation(PDAnnotation annotation) {
		return true;
	}
}
