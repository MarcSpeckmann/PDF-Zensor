package de.uni_hannover.se.pdfzensor.censor.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

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
    void testIsMarked() {
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, null));
        // rect is null, criteria is given
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, MarkCriterion.INTERSECT));
        // rect is given, criteria is null

        //rect and criteria is given
    }
    
    /**
     * test for {@link Annotations#isMarked(Rectangle2D)}
     */
    @Test
    void testIsMarkedOnlyContain() {
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null));
    }
    
    /**
     * test for {@link Annotations#isLinked(Rectangle2D, MarkCriterion)}
     */
    @Test
    void testIsLinked() {
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, null));
        // rect is null, criteria is given
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, MarkCriterion.INTERSECT));
        // rect is given, criteria is null

        //rect and criteria is given
    }
    
    /**
     * test for {@link Annotations#isLinked(Rectangle2D)}
     */
    @Test
    void testIsLinkedOnlyContain() {
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null));
    }
}
