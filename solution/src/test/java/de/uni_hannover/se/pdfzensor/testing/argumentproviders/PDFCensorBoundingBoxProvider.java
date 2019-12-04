package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.PDF_RESOURCE_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResourcePath;

public final class PDFCensorBoundingBoxProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		
		// element 0 and 1 should be combined, 2 should remain on its own:
		// 2 elements at the end.
		list.add(Arguments.arguments(getResourcePath(PDF_RESOURCE_PATH + "XsAtSetPosition.pdf"),
									 new Rectangle2D.Double[]{
											 new Rectangle2D.Double(0, 0, 20, 20),
											 new Rectangle2D.Double(25, 0, 20, 20),
											 new Rectangle2D.Double(0, 30, 20, 20)},
									 2));
		
		// element 0 and 1 should be combined, 2 and 3 should both remain on their own (different colors):
		// 3 elements at the end.
		list.add(Arguments.arguments(getResourcePath(PDF_RESOURCE_PATH + "XsAtSetPositionLinks.pdf"),
									 new Rectangle2D.Double[]{
											 new Rectangle2D.Double(0, 0, 20, 20),
											 new Rectangle2D.Double(25, 0, 20, 20),
											 new Rectangle2D.Double(0, 30, 20, 20),
											 new Rectangle2D.Double(25, 30, 20, 20)},
									 3));
		return list.stream();
	}
}
