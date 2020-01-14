package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.config.Settings;
import org.apache.logging.log4j.Level;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import java.io.IOException;
import java.util.Scanner;

/**
 * DecryptionUtil is a utility-class for handling encrypted PDFs and anything related to encrypted PDFs.
 */
public final class DecryptionUtil {
	/**
	 * This constructor should not be called as no instance of {@link DecryptionUtil} shall be created.
	 *
	 * @throws UnsupportedOperationException when called
	 */
    private DecryptionUtil(){
        throw new UnsupportedOperationException();
    }

	/**
	 * @param settings The {@link Settings} of the document which is analyzed.
	 * @return a boolean that indicates whether the document is ready to be handled or not
	 * (and therefore should be skipped)
	 */
    public static boolean handleEncryptedPDF(Settings settings){
        if(isCorrectPassword(settings)) return true;
        if(settings.getNoInteraction()){
            Logging.getLogger().log(Level.ERROR, "The document " + settings.getInput() + " is encrypted." +
                    " The password for decryption is either incorrect or missing.");
            return false;
        }
        boolean done = false;
        String password;
        while(!done){
            password = getPasswordFromCL(settings);
            settings.setPassword(password);
            done = (password.equals("") || isCorrectPassword(settings));
        }
        return isCorrectPassword(settings);
    }

    /**
     * @param settings The {@link Settings} of the document which is analyzed.
     * @return a boolean that describes whether the PDF is encrypted (true) or not (false);
     */
    public static boolean isEncrypted(Settings settings){
        try (final var ignored = PDDocument.load(settings.getInput())){
            return false;
        } catch (InvalidPasswordException ipe) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

	/**
	 * @param settings The {@link Settings} of the document which is analyzed.
	 * @return a boolean that is true if the settings contain the correct password.
	 */
    private static boolean isCorrectPassword(Settings settings){
		try (final var ignored = PDDocument.load(settings.getInput(), settings.getPassword())) {
            return true;
        } catch (IOException ipe) {
            return false;
        }
	}


	/**
	 * @param settings The {@link Settings} of the document which is analyzed.
	 * @return the String that has been entered by the user.
	 */
    private static String getPasswordFromCL(Settings settings){
        Scanner scanner = new Scanner(System.in);
        Logging.getLogger().log(Level.ERROR, "Please enter the password for encrypted document {}", settings.getInput());
        Logging.getLogger().log(Level.ERROR, "(Just leave it blank if you want to skip this document!)");
        return scanner.nextLine();
    }

	/**
	 * @param settings The {@link Settings} of the document which is analyzed.
	 * @return a boolean that is true if the PDF-File is parsable.
	 */
    public static boolean isParsablePDF(Settings settings){
        try(final var ignored = PDDocument.load(settings.getInput(), settings.getPassword())){
            return true;
        } catch (InvalidPasswordException ipe) {
            return true;
        } catch(IOException ex){
            return false;
        }
    }

}
