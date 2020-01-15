package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.PDF_RESOURCE_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResourcePath;

/**
 * This class implements {@link ArgumentsProvider} and provides the arguments for the unit test of PDFCensorMarkedTest.
 * <br>
 * The types of the arguments are {@code String, Rectangle2D[], int}. The {@link String} is a path to a test pdf and the
 * {@link Rectangle2D}-array contains the uncombined bounding-boxes. It is later checked if they - joined - are
 * approximately the same as the actual, censored bounding-boxes. Lastly the {@link int} specifies the expected amount
 * of remaining bounding-boxes, after they have been combined.
 */
public final class PDFCensorMarkedArgumentProvider implements ArgumentsProvider {
	/** {@inheritDoc} */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		
		// in case there is a Markup Annotation
		// both elements should be combined
		list.add(Arguments.arguments(getResourcePath(PDF_RESOURCE_PATH + "XsAtSetPosition.pdf"),
									 new Rectangle2D[]{new Rectangle2D.Double(0, 30, 20, 20)},
									 1));
		// dummy test to check if it works even it if there are no Markup Annotations
		// element 0 and 1 should be combined, 2 and 3 should both remain on their own (different colors)
		// 3 elements at the end.
		list.add(Arguments.arguments(getResourcePath(PDF_RESOURCE_PATH + "XsAtSetPositionLinks.pdf"),
									 new Rectangle2D[]{},
									 0));
		return list.stream();
	}
}
