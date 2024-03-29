package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * AnnotationsTest should contain all unit-tests related to {@link Annotations}.
 */
class AnnotationsTest {
	
	/**
	 * tests for {@link Annotations} constructor
	 */
	@Test
	void testAnnotationsConstructor() {
		Assertions.assertDoesNotThrow(AnnotationsTest::new);
	}
	
	/**
	 * test for {@link Annotations#cachePage}
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testCachePage() {
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().cachePage(null));
	}
	
	/* helper method for creating a virtual, blank PDF document with only one annotation highlight with
		the size of 100x200 to test a specific rectangle if it fits into or intersects with the annotation highlight */
	private PDDocument createMarkedDocument() {
		PDDocument testDocument = new PDDocument();
		// creating own blank pdf
		PDPage page = new PDPage();
		testDocument.addPage(page);
		try {
			PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
			// drawing black rectangle to validate the PDFdocument and PDPage is working
			contentStream.addRect(50, 0, 100, 100);
			contentStream.fill();
			contentStream.close();
			
			// creating custom annotation
			List<PDAnnotation> annots = page.getAnnotations();
			PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
			markup.setRectangle(new PDRectangle(0, 0, 100, 200));
			markup.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
			annots.add(markup);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return testDocument;
	}
	
	/**
	 * test for {@link Annotations#isMarked(Rectangle2D, MarkCriterion)}
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testIsMarked() {
		// both arguments are null
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, null));
		
		// rect is null, criteria is given
		Assertions.assertThrows(NullPointerException.class,
								() -> new Annotations().isMarked(null, MarkCriterion.INTERSECT));
		
		try (final var testDocument = createMarkedDocument()) {
			PDPage testPage = testDocument.getPage(0);
			Annotations anno = new Annotations();
			anno.cachePage(testPage);
			// given rectangle is outside the rectangle of the highlight respectively bordering so not intersecting
			Assertions.assertFalse(anno.isMarked(new Rectangle(100, 0, 100, 100), MarkCriterion.INTERSECT));
			
			Assertions.assertThrows(NullPointerException.class,
									() -> anno.isMarked(new Rectangle(50, 0, 100, 100), null));
			
			// given rectangle is intersecting the rectangle of the highlight
			Assertions.assertTrue(anno.isMarked(new Rectangle(50, 0, 100, 100), MarkCriterion.INTERSECT));
		} catch (IOException e) {
			fail(e);
		}
	}
	
	/**
	 * test for {@link Annotations#isMarked(Rectangle2D)}
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testIsMarkedOnlyContain() {
		// argument rect is null
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null));
		
		
		// given rect is in any of the highlights
		try (final var testDocument = createMarkedDocument()) {
			PDPage testPage = testDocument.getPage(0);
			Annotations anno = new Annotations();
			anno.cachePage(testPage);
			// given rectangle fully fits into the rectangle of the highlight
			Assertions.assertTrue(anno.isMarked(new Rectangle(0, 0, 50, 50)));
			// given rectangle is wider than the rectangle of the highlight so does not fully fit into it
			Assertions.assertFalse(anno.isMarked(new Rectangle(100, 200, 200, 50)));
		} catch (IOException e) {
			fail(e);
		}
	}
	
	/* helper method for creating a virtual, blank PDF document with only one annotation link with
		the size of 100x200 to test a specific rectangle if it fits into or intersects with the annotation link */
	PDDocument createLinkedDocument() {
		// creating own blank pdf
		PDDocument testDocument = new PDDocument();
		PDPage page = new PDPage();
		testDocument.addPage(page);
		try {
			PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
			// drawing black rectangle to validate the PDFdocument and PDPage is working
			contentStream.addRect(50, 0, 100, 100);
			contentStream.fill();
			contentStream.close();
			
			// creating custom annotation
			List<PDAnnotation> annots = page.getAnnotations();
			PDAnnotationLink link = new PDAnnotationLink();
			link.setRectangle(new PDRectangle(0, 0, 100, 200));
			link.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
			annots.add(link);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return testDocument;
	}
	
	/**
	 * test for {@link Annotations#isLinked(Rectangle2D, MarkCriterion)}
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testIsLinked() {
		// both arguments are null
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, null));
		
		// rect is null, criteria is given
		Assertions.assertThrows(NullPointerException.class,
								() -> new Annotations().isLinked(null, MarkCriterion.INTERSECT));
		
		try (final var testDocument = createLinkedDocument()) {
			PDPage testPage = testDocument.getPage(0);
			Annotations anno = new Annotations();
			anno.cachePage(testPage);
			// given rectangle is outside the rectangle of the link respectively bordering so not intersecting
			Assertions.assertFalse(anno.isLinked(new Rectangle(100, 0, 100, 100), MarkCriterion.CONTAIN));
			// rect is given, criteria is null
			Assertions.assertThrows(NullPointerException.class, () -> anno.isLinked(new Rectangle(0, 0, 50, 50), null));
			
			Assertions.assertTrue(anno.isLinked(new Rectangle(50, 100, 100, 100), MarkCriterion.INTERSECT));
		} catch (IOException e) {
			fail(e);
		}
	}
	
	/**
	 * test for {@link Annotations#isLinked(Rectangle2D)}
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testIsLinkedOnlyIntersect() {
		// argument rect is null
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null));
		
		// given rect in any of the links
		try (final var testDocument = createLinkedDocument()) {
			PDPage testPage = testDocument.getPage(0);
			Annotations anno = new Annotations();
			anno.cachePage(testPage);
			// given rectangle fully fits into the rectangle of the link
			Assertions.assertTrue(anno.isLinked(new Rectangle(0, 150, 50, 50)));
			// given rectangle is wider than the rectangle of the link so does not fully fit into it
			Assertions.assertTrue(anno.isLinked(new Rectangle(0, 150, 100, 50)));
		} catch (IOException e) {
			fail(e);
		}
	}
}
