package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.logging.log4j.Level;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.jetbrains.annotations.NotNull;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * MetadataRemover is used to censor all the metadata in a {@link PDDocument}.
 * The dates (date of creation and date of modification) will be set to the time the PDFZensor was used on the document.
 */
abstract class MetadataRemover {
    /**
     * Receives a {@link PDDocument} and censors all the metadata.
     * @param document The PDDocument that will be censored. May not be null.
     */
    static void censorMetadata(@NotNull PDDocument document){
        PDDocumentInformation docinfo = null;
        try {
            docinfo = document.getDocumentInformation();
        }catch (Exception e){
            Logging.getLogger().log(Level.ERROR, "Could not remove metadata - unable to retrieve document information");
        }
        if(docinfo != null){
            docinfo.setAuthor("Censored Author");
            docinfo.setCreator("Censored Creator");
            docinfo.setTitle("Censored Title");
            docinfo.setSubject("Censored Subject");
            docinfo.setProducer("Censored Producer");
            docinfo.setKeywords("Censored Keywords");
            Calendar date = new GregorianCalendar();
            docinfo.setCreationDate(date);
            docinfo.setModificationDate(date);
        }
    }
}
