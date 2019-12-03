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

    /**
     * test for {@link Annotations#isMarked(Rectangle2D, MarkCriterion)}
     */
    @Test
    void testIsMarked() throws IOException {
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, null));


        // rect is null, criteria is given
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, MarkCriterion.INTERSECT));


        // rect is given, criteria is null
        Assertions.assertThrows(NullPointerException.class, () -> {
            PDDocument testDocument = new PDDocument();
            // creating own blank pdf
            PDPage page = new PDPage();
            testDocument.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
            // drawing black rectangle to intersect
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

            PDPage testPage = testDocument.getPage(0);
            Annotations anno = new Annotations();
            anno.cachePage(testPage);
            anno.isMarked(new Rectangle(50, 0, 100, 100), null);
        });


        // rect and criteria is given
        PDDocument testDocument = new PDDocument();
        // creating own blank pdf
        PDPage page = new PDPage();
        testDocument.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
        // drawing black rectangle to intersect
        contentStream.addRect(50, 0, 100, 100);
        contentStream.fill();
        contentStream.close();

        // creating custom annotation
        List<PDAnnotation> annots = page.getAnnotations();
        PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        markup.setRectangle(new PDRectangle(0, 0, 100, 200));
        markup.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
        annots.add(markup);

        //uncomment for getting a generated sample
        //testDocument.save(new File("src/test/resources/pdf-files/generated/markedTest.pdf"));
        //testDocument.close();

        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        Assertions.assertTrue(anno.isMarked(new Rectangle(50, 0, 100, 100), MarkCriterion.INTERSECT));
        Assertions.assertFalse(anno.isMarked(new Rectangle(100, 0, 100, 100), MarkCriterion.INTERSECT));
    }

    /**
     * test for {@link Annotations#isMarked(Rectangle2D)}
     */
    @Test
    void testIsMarkedOnlyContain() throws IOException {
        // argument rect is null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null));


        // given rect is in any of the highlights
        // creating own blank pdf
        PDDocument testDocument = new PDDocument();
        PDPage page = new PDPage();
        testDocument.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
        // drawing black rectangle to intersect
        contentStream.addRect(50, 0, 100, 100);
        contentStream.fill();
        contentStream.close();

        // creating custom annotation
        List<PDAnnotation> annots = page.getAnnotations();
        PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        markup.setRectangle(new PDRectangle(0, 0, 100, 200));
        markup.setQuadPoints(new float[]{0, 0, 100, 0, 100, 200, 0, 200});
        annots.add(markup);

        //uncomment for getting a generated sample
        //testDocument.save(new File("src/test/resources/pdf-files/generated/markedTest.pdf"));
        //testDocument.close();

        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        Assertions.assertTrue(anno.isMarked(new Rectangle(0, 0, 50, 50)));
        Assertions.assertFalse(anno.isMarked(new Rectangle(51, 0, 50, 50)));
    }

    /**
     * test for {@link Annotations#isLinked(Rectangle2D, MarkCriterion)}
     */
    @Test
    void testIsLinked() throws IOException {
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, null));


        // rect is null, criteria is given
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, MarkCriterion.INTERSECT));


        // rect is given, criteria is null
        Assertions.assertThrows(NullPointerException.class, () -> {
            // creating own blank pdf
            PDDocument testDocument = new PDDocument();
            PDPage page = new PDPage();
            testDocument.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
            // drawing black rectangle to intersect
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

            PDPage testPage = testDocument.getPage(0);
            Annotations anno = new Annotations();
            anno.cachePage(testPage);
            anno.isLinked(new Rectangle(0, 0, 50, 50), null);
        });


        // rect and criteria is given
        // creating own blank pdf
        PDDocument testDocument = new PDDocument();
        PDPage page = new PDPage();
        testDocument.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
        // drawing black rectangle to intersect
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

        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        Assertions.assertTrue(anno.isLinked(new Rectangle(50, 0, 100, 100), MarkCriterion.INTERSECT));
        Assertions.assertFalse(anno.isLinked(new Rectangle(100, 0, 100, 100), MarkCriterion.INTERSECT));
    }

    /**
     * test for {@link Annotations#isLinked(Rectangle2D)}
     */
    @Test
    void testIsLinkedOnlyContain() throws IOException {
        // argument rect is null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null));


        // given rect in any of the links
        // creating own blank pdf
        PDDocument testDocument = new PDDocument();
        PDPage page = new PDPage();
        testDocument.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(testDocument, page);
        // drawing black rectangle to intersect
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

        PDPage testPage = testDocument.getPage(0);
        Annotations anno = new Annotations();
        anno.cachePage(testPage);
        Assertions.assertTrue(anno.isLinked(new Rectangle(0, 0, 50, 50)));
        Assertions.assertFalse(anno.isLinked(new Rectangle(51, 0, 50, 50)));
    }
}
