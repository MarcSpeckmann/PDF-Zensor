package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.PDF_RESOURCE_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResourcePath;

/**
 * This class implements {@link ArgumentsProvider} and provides the arguments for the unit test of {@link
 * de.uni_hannover.se.pdfzensor.images.ImageReplacer}.
 * <br>
 * The types of the arguments are a {@link List} containing {@link Rectangle2D}s and a {@link String}. The {@link
 * String} is a path to a test pdf and the {@link List} of {@link Rectangle2D}s contains the information about the
 * positions of images inside the test pdf.
 */
public class ImageReplacerArgumentProvider implements ArgumentsProvider {
	/** Returns a stream of path to PDFs with images inside and the place where the images are placed inside the PDF. */
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		List<Rectangle2D> rectList1 = new ArrayList<>();
		rectList1.add(new Rectangle2D.Double(10, 50, 200, 200));
		rectList1.add(new Rectangle2D.Double(250, 250, 200, 200));
		rectList1.add(new Rectangle2D.Double(10, 400, 200, 200));
		String path1 = getResourcePath(PDF_RESOURCE_PATH + "threeImages.pdf");
		
		List<Rectangle2D> rectList2 = new ArrayList<>();
		rectList2.add(new Rectangle2D.Double(89, 345, 292, 413));
		String path2 = getResourcePath(PDF_RESOURCE_PATH + "pdfinpdf.pdf");
		
		return Stream.of(
				Arguments.of(rectList1, path1),
				Arguments.of(rectList2, path2)
		);
	}
}
