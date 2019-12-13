package de.uni_hannover.se.pdfzensor.images;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static de.uni_hannover.se.pdfzensor.testing.TestConstants.PDF_RESOURCE_PATH;
import java.io.File;
import java.io.IOException;


import static de.uni_hannover.se.pdfzensor.testing.TestUtility.*;

//TODO: add further tests
/** PDFProcessorTest should contain all unit-tests related to {@link ImageRemover}. */
class ImageRemoverTest {
    /** Path to the pdf-tests Resources */
    private static final String pdfPath = getResourcePath(PDF_RESOURCE_PATH + "threeImages.pdf");

    @Test
    void testRemove() throws IOException {
        PDDocument testDocument = PDDocument.load(new File(pdfPath));
        assertEquals(3, countPDImageXObjects(testDocument));
        //
        ImageRemover.remove(testDocument);
        //
        assertEquals(0, countPDImageXObjects(testDocument));
        testDocument.close();
    }

    /**
     * Counts the number of {@link PDImageXObject} in the given {@link PDDocument}.
     * @param document is the {@link PDDocument} that is looked through.
     * @return the number of {@link PDImageXObject} found.
     * @throws IOException when there is an Error retrieving the xObjectNames.
     */
    private int countPDImageXObjects(PDDocument document) throws IOException {
        var pdImageObjectCounter = 0;
        for (var page : document.getPages()) {
            for (var name : page.getResources().getXObjectNames()) {
                var xObject = page.getResources().getXObject(name);
                if (xObject instanceof PDImageXObject)
                    pdImageObjectCounter++;
            }
        }
        return pdImageObjectCounter;
    }

}
