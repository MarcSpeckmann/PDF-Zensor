package de.uni_hannover.se.pdfzensor.images;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.PDF_RESOURCE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

//TODO: add further tests

/**
 * PDFProcessorTest should contain all unit-tests related to {@link ImageRemover}.
 */
class ImageRemoverTest {
	private static Stream<Arguments> testArguments() throws IOException {
		return Files.walk(Paths.get(TestUtility
				.getResource(PDF_RESOURCE_PATH).getAbsolutePath()))
				.map(Path::toFile)
				.filter(File::isFile)
				.map(Arguments::of);
	}

	/**
	 * Path to the pdf-tests Resources
	 */
	//private static final String pdfPath = getResourcePath(PDF_RESOURCE_PATH + "threeImages.pdf");
	@ParameterizedTest
	@MethodSource("testArguments")
	void testRemove(File file) throws IOException {
		PDDocument testDocument = PDDocument.load(file);
		//assertEquals(3, countPDImageXObjects(testDocument));
		//
		ImageRemover.remove(testDocument);
		//
		assertEquals(0, countPDImageXObjects(testDocument));
		testDocument.close();
	}

	/**
	 * Counts the number of {@link PDImageXObject} in the given {@link PDDocument}.
	 *
	 * @param document is the {@link PDDocument} that is looked through.
	 * @return the number of {@link PDImageXObject} found.
	 * @throws IOException when there is an Error retrieving the xObjectNames.
	 */
	private int countPDImageXObjects(PDDocument document) throws IOException {
		var pdImageObjectCounter = 0;
		// TODO added try and catch with ignore to discus how to handle this
		try {
			for (var page : document.getPages()) {
				for (var name : page.getResources().getXObjectNames()) {
					var xObject = page.getResources().getXObject(name);
					if (xObject instanceof PDImageXObject)
						pdImageObjectCounter++;
				}
			}
		} catch (NullPointerException ignore) {
		}
		return pdImageObjectCounter;
	}

}
