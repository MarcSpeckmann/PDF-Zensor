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
import java.io.File;
import java.io.IOException;
import java.util.List;

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

    /* helper method for creating a virtual PDF document for the isMarked-tests with one highlight with
        the size of 100x200 */
    private PDDocument createMarkedDocument() {
        PDDocument testDocument = new PDDocument();
        // creating own blank pdf
        PDPage page = new PDPage();
        testDocument.addPage(page);
        try {
            PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
            // drawing black rectangle to validate
            contentStream.addRect(50, 0, 100, 100);
            contentStream.fill();
            contentStream.close();

            // creating custom annotation
            List<PDAnnotation> annots = page.getAnnotations();
            PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
            markup.setRectangle(new PDRectangle(0, 0, 100, 200));
            markup.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
            annots.add(markup);
            // uncomment for getting a generated sample
            //testDocument.save(new File("src/test/resources/pdf-files/generated/markedTest.pdf"));
            //testDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDocument;
    }

    /**
     * test for {@link Annotations#isMarked(Rectangle2D, MarkCriterion)}
     */
    @Test
    void testIsMarked() {
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, null));


        // rect is null, criteria is given
        Assertions.assertThrows(NullPointerException.class,
                () -> new Annotations().isMarked(null, MarkCriterion.INTERSECT));


        // rect is given, criteria is null
        Assertions.assertThrows(NullPointerException.class, () -> {
            PDDocument testDocument = createMarkedDocument();
            PDPage testPage = testDocument.getPage(0);
            Annotations anno = new Annotations();
            anno.cachePage(testPage);
            anno.isMarked(new Rectangle(50, 0, 100, 100), null);
        });


        // rect and criteria is given
        PDDocument testDocument = createMarkedDocument();
        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        // given rectangle is intersecting the rectangle of the highlight
        Assertions.assertTrue(anno.isMarked(new Rectangle(50, 0, 100, 100), MarkCriterion.INTERSECT));
        // given rectangle is outside the rectangle of the highlight respectively bordering so not intersecting
        Assertions.assertFalse(anno.isMarked(new Rectangle(100, 0, 100, 100), MarkCriterion.INTERSECT));
    }

    /**
     * test for {@link Annotations#isMarked(Rectangle2D)}
     */
    @Test
    void testIsMarkedOnlyContain() {
        // argument rect is null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null));


        // given rect is in any of the highlights
        PDDocument testDocument = createMarkedDocument();
        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        // given rectangle fully fits into the rectangle of the highlight
        Assertions.assertTrue(anno.isMarked(new Rectangle(0, 0, 50, 50)));
        // given rectangle is wider than the rectangle of the highlight so does not fully fit into it
        Assertions.assertFalse(anno.isMarked(new Rectangle(0, 0, 200, 50)));
    }

    /* helper method for creating a virtual PDF document for the isLinked-tests with one link with
        the size of 100x200 */
    PDDocument createLinkedDocument() {
        // creating own blank pdf
        PDDocument testDocument = new PDDocument();
        PDPage page = new PDPage();
        testDocument.addPage(page);
        try {
            PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
            // drawing black rectangle to validate
            contentStream.addRect(50, 0, 100, 100);
            contentStream.fill();
            contentStream.close();

            // creating custom annotation
            List<PDAnnotation> annots = page.getAnnotations();
            PDAnnotationLink link = new PDAnnotationLink();
            link.setRectangle(new PDRectangle(0, 0, 100, 200));
            link.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
            annots.add(link);
            //uncomment for getting a generated sample
            //testDocument.save(new File("src/test/resources/pdf-files/generated/linkedTest.pdf"));
            //testDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDocument;
    }

    /**
     * test for {@link Annotations#isLinked(Rectangle2D, MarkCriterion)}
     */
    @Test
    void testIsLinked() {
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, null));


        // rect is null, criteria is given
        Assertions.assertThrows(NullPointerException.class,
                () -> new Annotations().isLinked(null, MarkCriterion.INTERSECT));


        // rect is given, criteria is null
        Assertions.assertThrows(NullPointerException.class, () -> {
            PDDocument testDocument = createLinkedDocument();
            PDPage testPage = testDocument.getPage(0);
            Annotations anno = new Annotations();
            anno.cachePage(testPage);
            anno.isLinked(new Rectangle(0, 0, 50, 50), null);
        });


        // rect and criteria is given
        PDDocument testDocument = createLinkedDocument();
        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        // given rectangle is intersecting the rectangle of the link
        Assertions.assertTrue(anno.isLinked(new Rectangle(50, 0, 100, 100), MarkCriterion.INTERSECT));
        // given rectangle is outside the rectangle of the link respectively bordering so not intersecting
        Assertions.assertFalse(anno.isLinked(new Rectangle(100, 0, 100, 100), MarkCriterion.INTERSECT));
    }

    /**
     * test for {@link Annotations#isLinked(Rectangle2D)}
     */
    @Test
    void testIsLinkedOnlyContain() {
        // argument rect is null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null));


        // given rect in any of the links
        PDDocument testDocument = createLinkedDocument();
        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        // given rectangle fully fits into the rectangle of the link
        Assertions.assertTrue(anno.isLinked(new Rectangle(0, 0, 50, 50)));
        // given rectangle is wider than the rectangle of the link so does not fully fit into it
        Assertions.assertFalse(anno.isLinked(new Rectangle(0, 0, 200, 50)));
    }
}
