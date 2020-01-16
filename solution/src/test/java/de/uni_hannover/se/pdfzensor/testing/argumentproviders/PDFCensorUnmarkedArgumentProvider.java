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
 * PDFCensorUnmarkedArgumentProvider can be used as a value-source for methods taking <code>{@code String,
 * Rectangle2D[], int}</code> for and argument. The string is the path to the pdf-file that should be read. The
 * Rectangle2D[] are the uncombined bounding boxes. It is later checked if they - joined - are approximately the same as
 * the actual, censored bounding-boxes. Lastly the int gives the amount of combined bounding-boxes that are expected.
 */
public final class PDFCensorUnmarkedArgumentProvider implements ArgumentsProvider {
	
	/** {@inheritDoc} */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		
		// in case there is a Markup Annotation
		// both elements should be combined
		list.add(Arguments.arguments(getResourcePath(PDF_RESOURCE_PATH + "XsAtSetPosition.pdf"),
									 1));
		// dummy test to check if it works even it if there are no Markup Annotations
		// element 0 and 1 should be combined, 2 and 3 should both remain on their own (different colors)
		// 3 elements at the end.
		list.add(Arguments.arguments(getResourcePath(PDF_RESOURCE_PATH + "XsAtSetPositionLinks.pdf"),
									 3));
		return list.stream();
	}
}
