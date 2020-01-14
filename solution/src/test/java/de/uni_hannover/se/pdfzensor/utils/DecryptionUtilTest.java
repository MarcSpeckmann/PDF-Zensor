package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.config.Settings;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.ENCRYPTED_PDF_RESOURCE_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;

public class DecryptionUtilTest {
	@Test
	void handleEncryptedPDFTest() {
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

	@Test
	void isEncryptedTest(){

	}
}
