package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.*;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;

/** DecryptionUtilTest should contain all unit-tests related to {@link DecryptionUtil}. */
public class DecryptionUtilTest {

	/**
	 * Tests if all given Settings are handled correctly by {@link DecryptionUtil}.handleEncryptedPDF().
	 */
	@Test
	void testHandleEncryptedPDF() {
		//right password
		String[] arr =  {getResource(ENCRYPTED_PDF_RESOURCE_PATH + "Cryptography_Sample_encrypted.pdf").getPath(), "-p", "testpassword"};
		assertTrue(DecryptionUtil.handleEncryptedPDF(new Settings(null, arr)));
		//No interaction and no password
		String[] arr2 =  {getResource(ENCRYPTED_PDF_RESOURCE_PATH + "Cryptography_Sample_encrypted.pdf").getPath(), "-n"};
		assertFalse(DecryptionUtil.handleEncryptedPDF(new Settings(null, arr2)));
		//No interaction and wrong password
		String[] arr3 =  {getResource(ENCRYPTED_PDF_RESOURCE_PATH + "Cryptography_Sample_encrypted.pdf").getPath(), "-n", "-p", "wrongpassword"};
		assertFalse(DecryptionUtil.handleEncryptedPDF(new Settings(null, arr3)));
	}

	/**
	 * @return the {@link Stream} that contains the PD-files from the resource-folder
	 * @throws IOException when there is an I/O-Error.
	 */
	private static Stream<Arguments> testEncryptedArguments() throws IOException {
		return Files.walk(Paths.get(TestUtility
				.getResource(ENCRYPTED_PDF_RESOURCE_PATH).getAbsolutePath()))
				.map(Path::toFile)
				.filter(File::isFile)
				.map(Arguments::of);
	}

	/**
	 * Tests if {@link DecryptionUtil}.isEncrypted() returns true for all encrypted PDFs.
	 * @param file File that is encrypted and used for the {@link Settings} given to {@link DecryptionUtil}.isEncrypted().
	 */
	@ParameterizedTest
	@MethodSource("testEncryptedArguments")
	void testIsEncrypted(File file){
		assertTrue(DecryptionUtil.isEncrypted(new Settings(null, file.getPath())));
	}

	/**
	 * @return A Stream of {@link Arguments} that contains the {@link File}s for testIsNotEncrypted().
 	 * @throws IOException if an I/O-Error occurs.
	 */
	private static Stream<Arguments> testNotEncryptedArguments() throws IOException {
		return Files.walk(Paths.get(TestUtility
				.getResource(PDF_RESOURCE_PATH).getAbsolutePath()))
				.map(Path::toFile)
				.filter(File::isFile)
				.map(Arguments::of);
	}

	/**
	 *  Tests if {@link DecryptionUtil}.isEncrypted() returns false for all non-encrypted PDFs.
	 * @param file File that is encrypted and used for the {@link Settings} given to {@link DecryptionUtil}.isEncrypted().
	 */
	@ParameterizedTest
	@MethodSource("testNotEncryptedArguments")
	void testIsNotEncrypted(File file) {
		assertFalse(DecryptionUtil.isEncrypted(new Settings(null, file.getPath())));
	}

	/**
	 * Tests if {@link DecryptionUtil}.isParsablePDF() returns the correct boolean.
	 */
	@Test
	void testIsParsablePDF(){
		assertFalse(DecryptionUtil.isParsablePDF(new Settings(null, "notexisting.pdf")));
		assertFalse(DecryptionUtil.isParsablePDF(new Settings(null, "pom.xml")));
		assertFalse(DecryptionUtil.isParsablePDF(new Settings(null, getResource(CORRUPTED_PDF_RESOURCE_PATH + "sample(pdfVersionDeleted).pdf").getPath())));
	}
}
