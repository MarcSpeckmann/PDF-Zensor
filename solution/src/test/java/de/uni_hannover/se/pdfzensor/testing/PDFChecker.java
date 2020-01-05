package de.uni_hannover.se.pdfzensor.testing;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PDFChecker is a static utility-class that provides assertions about PDF-files to the outside.
 */
public final class PDFChecker {
	
	/**
	 * Asserts that no meta-information is present in the given PDF-document. This includes document information
	 * <b>and</b> XMP-data. Meta-information is considered removed if all the document-information is empty or
	 * <code>null</code> and if all XMP-data is <code>null</code>.
	 *
	 * @param document the document for which to check if all meta-information was removed.
	 */
	public static void assertNoMetaInformation(@NotNull PDDocument document) {
		//Check for any metadata in the /Info of the document
		var docInfo = document.getDocumentInformation();
		for (var key : docInfo.getMetadataKeys()) {
			var value = docInfo.getCustomMetadataValue(key);
			assertTrue(StringUtils.isEmpty(value), "The value for " + key + " was not removed (is " + value + ")");
		}
		//Check for PDF Version 2.0+
		var metadata = document.getDocumentCatalog().getMetadata();
		assertNull(metadata, "XMP Metadata was not removed");
		for (var page : document.getPages()) {
			assertNull(page.getMetadata(), "XMP Metadata was not removed entirely");
			assertNoMetadata(page.getResources());
			page.getContentStreams()
				.forEachRemaining(stream -> assertNull(stream.getMetadata(), "XMP Metadata was not removed entirely"));
		}
	}
	
	/**
	 * Asserts that no XMP-data is present in the given resources. Meta-information is considered removed if all the
	 * XMP-data is <code>null</code>.
	 *
	 * @param resources the resources for which to check if all meta-information was removed.
	 */
	private static void assertNoMetadata(@Nullable PDResources resources) {
		if (resources == null)
			return;
		for (var resourceName : resources.getXObjectNames()) {
			try {
				var resourceStream = resources.getXObject(resourceName).getStream();
				assertNull(resourceStream.getMetadata(), "XMP Metadata was not removed entirely");
			} catch (IOException e) {
				fail(e);
			}
		}
	}
	
}
