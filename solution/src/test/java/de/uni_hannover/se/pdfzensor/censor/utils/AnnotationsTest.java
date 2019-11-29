package de.uni_hannover.se.pdfzensor.censor.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import de.uni_hannover.se.pdfzensor.censor.utils.*;

import java.awt.geom.Rectangle2D;

/** AnnotationsTest should contain all unit-tests related to {@link Annotations}. */
public class AnnotationsTest {

    /** tests for {@link Annotations} constructor */
    @Test
    void testAnnotationsConstructor(){
        Assertions.assertDoesNotThrow(AnnotationsTest::new);
    }

    /** test for {@link Annotations#cachePage} */
    @Test
    void testCachePage(){
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().cachePage(null));
    }

    /** test for {@link Annotations#isMarked(Rectangle2D, MarkCriteria)} */
    @Test
    void testIsMarked(){
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null, null));
    }

    /** test for {@link Annotations#isMarked(Rectangle2D)} */
    @Test
    void testIsMarkedOnlyContain(){
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isMarked(null));
    }

    /** test for {@link Annotations#isLinked(Rectangle2D, MarkCriteria)} */
    @Test
    void testIsLinked(){
        // both arguments are null
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null, null));
    }

    /** test for {@link Annotations#isLinked(Rectangle2D)} */
    @Test
    void testIsLinkedOnlyContain(){
        Assertions.assertThrows(NullPointerException.class, () -> new Annotations().isLinked(null));
    }
}
