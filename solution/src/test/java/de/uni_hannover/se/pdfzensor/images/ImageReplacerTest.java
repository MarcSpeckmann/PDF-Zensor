package de.uni_hannover.se.pdfzensor.images;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ImageReplacerArgumentProvider;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.PDFProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;


public class ImageReplacerTest {
	ImageReplacer imageReplacer = new ImageReplacer();
	
	
	@Test
	void testReplaceImageInvalidParameter() {
		assertThrows(IllegalArgumentException.class, () -> imageReplacer.replaceImages(null, null));
	}
	
	// just a helper to check the coodinates
	@Test
	void printCoords() {
		String PATH = "src/test/resources/pdf-files/";
		try {
			PDDocument document = PDDocument.load(
					new File(PATH + "threeImages.pdf"));
			PDPage page = document.getPage(0);
			List<Rectangle2D> rl = imageReplacer.replaceImages(document, page);
			System.out.println(String.valueOf(rl));
		} catch (Exception e) {
		}
	}
	
	void rectContainedHelper(Rectangle2D rect, List<Rectangle2D> rectList) {
		if (!rectList.contains(rect)) {
			fail();
		}
	}
	
	@ArgumentsSource(ImageReplacerArgumentProvider.class)
	@ParameterizedTest(name = "Run {index}: ListOfImagePositions: {0}, testedDocument: {1}")
	void testReplaceImage(List<Rectangle2D> rectList, String path) {
		try {
			PDDocument document = PDDocument.load(new File(path));
			PDPage page = document.getPage(0);
			List<Rectangle2D> rectListOfDocument = imageReplacer.replaceImages(document, page);
			
			rectList.forEach(rect -> rectContainedHelper(rect, rectListOfDocument));
		} catch (Exception e) {
			fail();
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(PDFProvider.class)
	void testImageDataRemoval(File file) {
		try (var doc = PDDocument.load(file)) {
			var imgRepl = new ImageReplacer();
			for (var page : doc.getPages()) {
				imgRepl.replaceImages(doc, page);
				assertNoXObjectsInPage(page);
			}
		} catch (IOException e) {
			fail(e);
		}
	}
	
	private static void assertNoXObjectsInPage(@NotNull PDPage page) {
		var data = page.getResources().getXObjectNames();
		var xObjCount = StreamSupport.stream(data.spliterator(), false).count();
		assertEquals(0, xObjCount, "XObjects are still present");
	}
	
}
