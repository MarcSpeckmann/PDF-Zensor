package de.uni_hannover.se.pdfzensor.censor.utils;

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
        Calendar creationDate = docInfo.getCreationDate();
        Calendar modificationDate = docInfo.getModificationDate();

        MetadataRemover.censorMetadata(document);

        docInfo = document.getDocumentInformation();
        assertEquals("Censored Author", docInfo.getAuthor());
        assertEquals("Censored Creator", docInfo.getCreator());
        assertEquals("Censored Producer", docInfo.getProducer());
        assertEquals("Censored Title", docInfo.getTitle());
        assertEquals("Censored Subject", docInfo.getSubject());
        assertEquals("Censored Keywords", docInfo.getKeywords());
        assertNotEquals(creationDate, docInfo.getCreationDate());
        assertNotEquals(modificationDate, docInfo.getModificationDate());
        document.close();
    }

    /**
     * Tests if the MetadateRemover does not throw an exception if it is unable
     * to retrieve the document information of the given file
     */
    @Test
    void testCensorMetadataInvalidFile(){
        assertDoesNotThrow(() -> MetadataRemover.censorMetadata(null));
    }
}
