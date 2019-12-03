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
public class AnnotationsTest {
	
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
	@Test
	void testCachePage() {
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().cachePage(null));
	}
	
	PDDocument createMarkedDocument() {
		PDDocument testDocument = new PDDocument();
		// creating own blank pdf
		PDPage page = new PDPage();
		testDocument.addPage(page);
		try {
			PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
			// drawing black rectangle to intersect
			contentStream.addRect(50, 0, 100, 100);
			contentStream.fill();
			contentStream.close();
			
		} catch (Exception e) {
			fail();
		}
        return testDocument;
	}
	
	/**
	 * test for {@link Annotations#isMarked(Rectangle2D, MarkCriterion)}
	 */
	@Test
	void testIsMarked1() throws IOException {
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, null));
        
        
        // rect is null, criteria is given
        Assertions.assertThrows(NullPointerException.class,
                                () -> new Annotations().isMarked(null, MarkCriterion.INTERSECT));
        
        
        PDDocument testDocument1 = createMarkedDocument();
        PDPage testPage1 = testDocument1.getPage(0);
        
        
        Assertions.assertThrows(NullPointerException.class, () -> {
            // creating custom annotation
            List<PDAnnotation> annotationList1 = testPage1.getAnnotations();
            PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
            markup.setRectangle(new PDRectangle(0, 0, 100, 200));
            markup.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
            annotationList1.add(markup);
            Annotations annotations1 = new Annotations();
            annotations1.cachePage(testPage1);
            annotations1.isMarked(new Rectangle(50, 0, 100, 100), null);
        });
    }
    
    @Test
    void testIsMarked2() {
		// creating a new PDF
		PDDocument testDocument = createMarkedDocument();
		PDPage testPage = testDocument.getPage(0);
		try {
            // creating custom annotation
            List<PDAnnotation> annotationList = testPage.getAnnotations();
            PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
            markup.setRectangle(new PDRectangle(0, 0, 100, 200));
            markup.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
            annotationList.add(markup);
        } catch (Exception e) {
			fail();
		}
        Annotations annotations = new Annotations();
        annotations.cachePage(testPage);
        
		Assertions.assertTrue(annotations.isMarked(new Rectangle(50, 0, 100, 100), MarkCriterion.INTERSECT));
		Assertions.assertFalse(annotations.isMarked(new Rectangle(100, 0, 100, 100), MarkCriterion.INTERSECT));
	}
	
	/**
	 * test for {@link Annotations#isMarked(Rectangle2D)}
	 */
	@Test
	void testIsMarkedOnlyContain() throws IOException {
		// argument rect is null
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null));
		
		
		PDDocument testDocument = createMarkedDocument();
		PDPage testPage = testDocument.getPage(0);
		List<PDAnnotation> annotationList1 = testPage.getAnnotations();
		PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
		markup.setRectangle(new PDRectangle(0, 0, 100, 200));
		markup.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
		annotationList1.add(markup);
		
		Annotations annotations = new Annotations();
		annotations.cachePage(testPage);
		Assertions.assertTrue(annotations.isMarked(new Rectangle(0, 0, 50, 50)));
		Assertions.assertFalse(annotations.isMarked(new Rectangle(51, 0, 50, 50)));
	}
	
	PDDocument createLinkedDocument() {
		PDDocument testDocument = new PDDocument();
		PDPage page = new PDPage();
		testDocument.addPage(page);
		try {
			PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
			// drawing black rectangle to intersect
			contentStream.addRect(50, 0, 100, 100);
			contentStream.fill();
			contentStream.close();
			
			
		} catch (Exception e) {
			fail();
		}
        return testDocument;
	}
	
	/**
	 * test for {@link Annotations#isLinked(Rectangle2D, MarkCriterion)}
	 */
	@Test
	void testIsLinked1() throws IOException {
		// both arguments are null
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, null));
		
		
		// rect is null, criteria is given
		Assertions.assertThrows(NullPointerException.class,
								() -> new Annotations().isLinked(null, MarkCriterion.INTERSECT));
		
		PDDocument testDocument = createLinkedDocument();
		PDPage testPage = testDocument.getPage(0);
		
		// creating custom annotation
		List<PDAnnotation> annotationList1 = testPage.getAnnotations();
		PDAnnotationLink link = new PDAnnotationLink();
		link.setRectangle(new PDRectangle(0, 0, 100, 200));
		link.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
		annotationList1.add(link);
		
		Annotations annotations1 = new Annotations();
		
		// rect is given, criteria is null
		Assertions.assertThrows(NullPointerException.class, () -> {
			annotations1.isLinked(new Rectangle(0, 0, 50, 50), null);
		});
	}
	
	@Test
	void testIsLinked2() {
		// rect and criteria is given
		// creating own blank pdf
		PDDocument testDocument = createLinkedDocument();
		PDPage testPage = testDocument.getPage(0);
		try {
            PDPageContentStream contentStream = new PDPageContentStream(testDocument, testPage,
                                                                        PDPageContentStream.AppendMode.APPEND, true,
                                                                        true);
			// drawing black rectangle to intersect
			contentStream.addRect(50, 0, 100, 100);
			contentStream.fill();
			contentStream.close();
			
			// creating custom annotation
			List<PDAnnotation> annotationList = testPage.getAnnotations();
			PDAnnotationLink link = new PDAnnotationLink();
			link.setRectangle(new PDRectangle(0, 0, 100, 200));
			link.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
			annotationList.add(link);
			
		} catch (Exception e) {
			fail();
		}
        Annotations annotations = new Annotations();
		annotations.cachePage(testPage);
		Assertions.assertTrue(annotations.isLinked(new Rectangle(50, 0, 100, 100), MarkCriterion.INTERSECT));
		Assertions.assertFalse(annotations.isLinked(new Rectangle(100, 0, 100, 100), MarkCriterion.INTERSECT));
	}
	
	/**
	 * test for {@link Annotations#isLinked(Rectangle2D)}
	 */
	@Test
	void testIsLinkedOnlyContain() throws IOException {
		// argument rect is null
		Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null));
		
		
		PDDocument testDocument = createLinkedDocument();
		PDPage testPage = testDocument.getPage(0);
		// creating custom annotation
		List<PDAnnotation> annotationList = testPage.getAnnotations();
		PDAnnotationLink link = new PDAnnotationLink();
		link.setRectangle(new PDRectangle(0, 0, 100, 200));
		link.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
		annotationList.add(link);
		
		Annotations annotations = new Annotations();
		annotations.cachePage(testPage);
		Assertions.assertTrue(annotations.isLinked(new Rectangle(0, 0, 50, 50)));
		Assertions.assertFalse(annotations.isLinked(new Rectangle(51, 0, 50, 50)));
	}
}
