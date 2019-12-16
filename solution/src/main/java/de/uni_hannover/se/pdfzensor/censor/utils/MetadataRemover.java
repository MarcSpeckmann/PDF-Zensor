package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDResources;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * MetadataRemover is used to censor all the metadata in a {@link PDDocument}. The dates (date of creation and date of
 * modification) will be set to the time the PDFZensor was used on the document.
 */
public final class MetadataRemover {
	private static final Logger LOGGER = Logging.getLogger();
	
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
	public static void censorMetadata(@NotNull PDDocument document) {
		Objects.requireNonNull(document);
		document.setDocumentInformation(new PDDocumentInformation());
		//The following censors the XMP metadata as it was introduced in PDF 2.0
		document.getDocumentCatalog().setMetadata(null);
		document.getPages().forEach(page -> {
			page.setMetadata(null);
			page.getContentStreams().forEachRemaining(stream -> stream.setMetadata(null));
			censorXMPData(page.getResources());
		});
	}
	
	/**
	 * Censors all XMP-data from the XObjects in the provided resources.
	 *
	 * @param resources the resources from which to remove all XMP-data.
	 */
	private static void censorXMPData(@Nullable PDResources resources) {
		if (resources != null) {
			resources.getXObjectNames().forEach(name -> {
				try {
					var stream = resources.getXObject(name).getStream();
					stream.setMetadata(null);
				} catch (IOException e) {
					LOGGER.warn("Failed to retrieve the XObject " + name + " from the resources", e);
				}
			});
		}
	}
}
