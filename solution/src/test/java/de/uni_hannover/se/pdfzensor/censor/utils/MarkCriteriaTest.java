package de.uni_hannover.se.pdfzensor.censor.utils;

import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.jupiter.api.Assertions.*;

class MarkCriteriaTest {

    /** tests for {@link MarkCriteria} constructor  */
    @Test
    void testMarkCriteria(){
        assertDoesNotThrow(() -> MarkCriteria.CONTAIN);
        assertDoesNotThrow(() -> MarkCriteria.INTERSECT);
    }

    /** tests for {@link MarkCriteria#getPredicate} function */
    @Test
    void testGetPredicate() {
        //some random Rectangle to test

        //opposite corners
        //(0,0) (2,2)
        var r1 = new Rectangle2D.Double(0, 0, 2, 2);
        //(1,1) (3,3)
        var r2 = new Rectangle2D.Double(1, 1, 2, 2);
        //(-1,-1) (1,1)
        var r3 = new Rectangle2D.Double(-1, -1, 2, 2);
        //(2,2) (4,4)
        var r4 = new Rectangle2D.Double(2, 2, 2, 2);
        //empty
        var r5 = new Rectangle2D.Double(2, 2, -1, -1);

        //checks if output Criteria is {@link Rectangle2D#intersects}
        var predicate = MarkCriteria.INTERSECT.getPredicate(r1);

        //r1 with itself
        assertTrue(predicate.test(r1));

        assertTrue(predicate.test(r2));
        assertTrue(predicate.test(r3));

        assertFalse(predicate.test(r4));
        assertFalse(predicate.test(r5));

        //(0,0) (1,1)
        r1 = new Rectangle2D.Double(0, 0, 1, 1);
        //(-1,-1) (3,3)
        r2 = new Rectangle2D.Double(-1, -1, 4, 4);
        //(-1,-1) (1,1)
        r3 = new Rectangle2D.Double(-1, -1, 2, 2);
        //(5,5) (7,7)
        r4 = new Rectangle2D.Double(5, 5, 2, 2);
        //(2,2) (-1,-1)
        r5 = new Rectangle2D.Double(2, 2, -1, -1);

        //checks if output Criteria is {@link Rectangle2D#contains}
        predicate = MarkCriteria.CONTAIN.getPredicate(r1);

        //r1 with itself
        assertTrue(predicate.test(r1));

        assertTrue(predicate.test(r2));
        assertTrue(predicate.test(r3));

        assertFalse(predicate.test(r4));
        assertFalse(predicate.test(r5));

    }
}