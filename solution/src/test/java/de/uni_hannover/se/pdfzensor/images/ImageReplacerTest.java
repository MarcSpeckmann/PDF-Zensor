package de.uni_hannover.se.pdfzensor.images;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.ImageReplacerArgumentProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ImageReplacerTest {
	ImageReplacer imageReplacer = new ImageReplacer();
	
	/**
	 * Tests the replaceImages functions with invalid parameters.
	 */
	@Test
	void testReplaceImageInvalidParameter() {
		assertThrows(NullPointerException.class, () -> imageReplacer.replaceImages(null, null));
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		assertThrows(NullPointerException.class, () -> imageReplacer.replaceImages(null, page));
		assertThrows(NullPointerException.class, () -> imageReplacer.replaceImages(document, null));
	}
	
	// just a helper to check the coodinates
	//TODO: remove before merge
	@Test
	void printCoords() {
		String PATH = "src/test/resources/pdf-files/";
		try {
			PDDocument document = PDDocument.load(
					new File(PATH + "pdfinpdf.pdf"));
			PDPage page = document.getPage(0);
			List<Rectangle2D> rl = imageReplacer.replaceImages(document, page);
			System.out.println(String.valueOf(rl));
		} catch (Exception e) {
		}
	}
	
	/**
	 * This function fails the current test if in rectlist is no rectangle similar to rect.
	 *
	 * @param rect A rectangle
	 * @param rectList A list of rectangles
	 */
	void rectContainedHelper(Rectangle2D rect, List<Rectangle2D> rectList) {
		if (!rectList.contains(rect)) {
			fail("rectangle not found");
		}
	}
	
/*	Rectangle2D rectAbsHelper(Rectangle2D rect){
		return new Rectangle2D.Double(Math.abs(rect.getX()), Math.abs(rect.getY()),
									  Math.abs(rect.getHeight()), Math.abs(rect.getWidth()));
	}
*/
	/**
	 * This function tests if all pictures in a document are found at the correct position.
	 *
	 * @param rectList A list of rectangles (coordinates).
	 * @param path The path to the pdf to be tested.
	 */
	@ArgumentsSource(ImageReplacerArgumentProvider.class)
	@ParameterizedTest(name = "Run {index}: ListOfImagePositions: {0}, testedDocument: {1}")
	void testReplaceImage(List<Rectangle2D> rectList, String path) {
		try {
			PDDocument document = PDDocument.load(new File(path));
			PDPage page = document.getPage(0);
			List<Rectangle2D> rectListOfDocument = imageReplacer.replaceImages(document, page);
			System.out.println(String.valueOf(rectListOfDocument));
			System.out.println(String.valueOf(rectList));
			rectList.forEach(rect -> rectContainedHelper(rect, rectListOfDocument));
		} catch (Exception e) {
			fail(e);
		}
	}
	
}
