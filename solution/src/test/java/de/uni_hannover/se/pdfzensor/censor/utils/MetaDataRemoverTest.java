package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

//TODO add more tests and maybe alter the structure of the tests
class MetaDataRemoverTest {
    /**
     * Tests if all of the metadata have been censored correctly using a valid PDF.
     * @throws IOException if an IO error occurs.
     */
    @Test
    void testCensorMetadataValidFile() throws IOException {
        String path = "src/test/resources/pdf-files/fullMetadata.pdf";

        File file = new File(path);
        PDDocument document = PDDocument.load(file);
        PDDocumentInformation docInfo = document.getDocumentInformation();

        MetadataRemover.censorMetadata(document);

        docInfo = document.getDocumentInformation();
        assertTrue(StringUtils.isEmpty(docInfo.getAuthor()));
        assertTrue(StringUtils.isEmpty(docInfo.getCreator()));
        assertTrue(StringUtils.isEmpty(docInfo.getProducer()));
        assertTrue(StringUtils.isEmpty(docInfo.getTitle()));
        assertTrue(StringUtils.isEmpty(docInfo.getSubject()));
        assertTrue(StringUtils.isEmpty(docInfo.getKeywords()));
        assertNull(docInfo.getCreationDate());
        assertNull(docInfo.getModificationDate());
        document.close();
    }

    /**
     * Tests if the MetadateRemover does not throw an exception if it is unable
     * to retrieve the document information of the given file
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    void testCensorMetadataInvalidFile(){
        assertThrows(NullPointerException.class, () -> MetadataRemover.censorMetadata(null));
    }
}
