package de.uni_hannover.se.pdfzensor.censor.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * MetadataRemover is used to censor all the metadata in a {@link PDDocument}. The dates (date of creation and date of
 * modification) will be set to the time the PDFZensor was used on the document.
 */
final class MetadataRemover {
	
	/**
	 * MetadataRemover is a pure static utility-class. As such no instances of it should be created. Thus its
	 * constructor may not be called and always throws an exception if it is tried.
	 */
	@Contract(value = " -> fail", pure = true)
	private MetadataRemover() {
		throw new UnsupportedOperationException("private MetadataRemover() called. This is not supported.");
	}
	
	/**
	 * Receives a {@link PDDocument} and censors all the metadata.
	 *
	 * @param document The PDDocument that will be censored. May not be null.
	 */
	static void censorMetadata(@NotNull PDDocument document) {
		Objects.requireNonNull(document);
		document.setDocumentInformation(new PDDocumentInformation());
		document.getDocumentCatalog().setMetadata(null);
	}
}
