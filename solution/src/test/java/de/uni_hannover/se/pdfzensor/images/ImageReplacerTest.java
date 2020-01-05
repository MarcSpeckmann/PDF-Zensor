package de.uni_hannover.se.pdfzensor.images;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
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
	
	private static void assertNoXObjectsInPage(@NotNull PDPage page) {
		var data = page.getResources().getXObjectNames();
		var xObjCount = StreamSupport.stream(data.spliterator(), false).count();
		assertEquals(0, xObjCount, "XObjects are still present");
	}
	
	/**
	 * Tests the replaceImages functions with invalid parameters.
	 */
	@Test
	void testReplaceImageInvalidParameter() {
		ImageReplacer imageReplacer = new ImageReplacer();
		assertThrows(NullPointerException.class, () -> imageReplacer.replaceImages(null, null));
		try (var document = new PDDocument()) {
			PDPage page = new PDPage();
			assertThrows(NullPointerException.class, () -> imageReplacer.replaceImages(null, page));
			assertThrows(NullPointerException.class, () -> imageReplacer.replaceImages(document, null));
		} catch (IOException e) {
			fail(e);
		}
	}
	
	/**
	 * This function fails the current test if in rectList is no rectangle similar to rect.
	 *
	 * @param rect     A rectangle
	 * @param rectList A list of rectangles
	 */
	void assertContainsRect(Rectangle2D rect, @NotNull List<Rectangle2D> rectList) {
		boolean contained = rectList.stream().anyMatch(r -> TestUtility.checkRectanglesEqual(rect, r, 1));
		assertTrue(contained, rect + " was not present in " + rectList);
	}
	
	/**
	 * This function tests if all pictures in a document are found at the correct position.
	 *
	 * @param rectList A list of rectangles (coordinates).
	 * @param path     The path to the pdf to be tested.
	 */
	@ArgumentsSource(ImageReplacerArgumentProvider.class)
	@ParameterizedTest(name = "Run {index}: ListOfImagePositions: {0}, testedDocument: {1}")
	void testReplaceImage(List<Rectangle2D> rectList, String path) {
		ImageReplacer imageReplacer = new ImageReplacer();
		try (var doc = PDDocument.load(new File(path))) {
			PDPage page = doc.getPage(0);
			List<Rectangle2D> rectListOfDocument = imageReplacer.replaceImages(doc, page);
			rectList.forEach(rect -> assertContainsRect(rect, rectListOfDocument));
		} catch (IOException e) {
			fail(e);
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
	
}
