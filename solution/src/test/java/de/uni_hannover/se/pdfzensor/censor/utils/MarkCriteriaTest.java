package de.uni_hannover.se.pdfzensor.censor.utils;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MarkCriteriaTest {
    
    /** Hash Map containing Booleans for expected outputs 1: 'INTERSECT' and 2: 'CONTAIN' and the input-rectangles */
    public static final Map<Boolean[], Rectangle2D> RECTANGLES = new HashMap<>();
    
    static Rectangle2D rect = new Rectangle2D.Double(0, 0, 2, 2);
    
    static {
        RECTANGLES.put(new Boolean[]{true,true}, rect);   // (0,0) (2,2), rectangle itself
        RECTANGLES.put(new Boolean[]{true,false}, new Rectangle2D.Double(1, 1, 2, 2));   // (1,1) (3,3)
        RECTANGLES.put(new Boolean[]{true,false}, new Rectangle2D.Double(-1, -1, 2, 2)); // (-1,-1) (1,1)
        RECTANGLES.put(new Boolean[]{true,false}, new Rectangle2D.Double(-1, 1, 2, 2));   // (-1,1) (1,3)
        RECTANGLES.put(new Boolean[]{true,false}, new Rectangle2D.Double(1, -1, 2, 2)); // (1,-1) (3,1)
        RECTANGLES.put(new Boolean[]{true,true}, new Rectangle2D.Double(-1, -1, 4, 4)); // (-1,-1) (3,3)
        RECTANGLES.put(new Boolean[]{false,false}, new Rectangle2D.Double(5, 5, 2, 2)); // (5,5) (7,7)
        RECTANGLES.put(new Boolean[]{false,false}, new Rectangle2D.Double(2, 2, 2, 2)); // (2,2) (4,4)
        RECTANGLES.put(new Boolean[]{false,false}, new Rectangle2D.Double(2, 2, -1, -1));    //empty
        // TODO maybe add more tests
    }
    
    /** tests for {@link MarkCriteria} constructor  */
    @Test
    void testMarkCriteria(){
        assertDoesNotThrow(() -> MarkCriteria.CONTAIN);
        assertDoesNotThrow(() -> MarkCriteria.INTERSECT);
    }
    
    /** Provides a set of arguments for {@link #getPredicateTest(Rectangle2D, Boolean[])} generated from {@link #RECTANGLES}. */
    private static Stream<Arguments> rectangleProvider() {
        return RECTANGLES.entrySet()
                .stream()
                .map(e -> Arguments.of(e.getValue(), e.getKey()));
    }
    
    /** tests for {@link MarkCriteria#getPredicate} function */
    @ParameterizedTest(name = "Run {index}: Rectangles: {0}")
    @MethodSource("rectangleProvider")
    void getPredicateTest(@NotNull Rectangle2D input, Boolean[] expected) {
        var predIntersect = MarkCriteria.INTERSECT.getPredicate(rect);
        var predContain = MarkCriteria.CONTAIN.getPredicate(rect);
        assertEquals(expected[0], predIntersect.test(input));  //checks if output Criteria is {@link Rectangle2D#intersects}
        assertEquals(expected[1], predContain.test(input));    //checks if output Criteria is {@link Rectangle2D#contains}
    }
}