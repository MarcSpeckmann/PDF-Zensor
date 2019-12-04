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
		list.add(Arguments.arguments(getResourcePath(PDF_RESOURCE_PATH + "XsAtSetPosition.pdf"),
									 new Rectangle2D.Double[]{
											 new Rectangle2D.Double(0, 0, 20, 20),
											 new Rectangle2D.Double(25, 0, 20, 20),
											 new Rectangle2D.Double(0, 30, 20, 20)}));
		return list.stream();
	}
}
