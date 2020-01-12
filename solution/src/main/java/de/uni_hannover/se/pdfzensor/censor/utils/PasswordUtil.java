package de.uni_hannover.se.pdfzensor.censor.utils;


import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.utils.RectUtils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;

import java.util.Scanner;


/**
 * Utility-class for handling password input and missing or incorrect passwords.
 */
public final class PasswordUtil {

	/**
	 * This constructor should not be called as no instance of {@link PasswordUtil} shall be created.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	@Contract(value = " -> fail", pure = true)
    private PasswordUtil(){
		throw new UnsupportedOperationException();
    }

	/**
	 * @param settings The {@link Settings} that belong to the document that is encrypted..
 	 * @return A boolean that is used to determine, if the password-handling has been finished
	 * by giving an empty password or if --no-interaction is set.
	 */
    public static boolean handleIncorrectPassword(Settings settings){
        if(settings.getNoInteraction()){
            Logging.getLogger().log(Level.ERROR, "The document " + settings.getInput() + " is encrypted." +
             " The password for decryption is either incorrect or missing.");
            return true;
        } else {
            settings.setPassword(getPasswordFromCL(settings));
            return settings.getPassword().equals("");
        }
    }

	/**
	 * @param settings The {@link Settings} that belong to the document that is encrypted.
	 * @return The password entered by the user as a String.
	 */
    public static String getPasswordFromCL(Settings settings){
        Scanner scanner = new Scanner(System.in);
        Logging.getLogger().log(Level.ERROR, "Please enter the password for encrypted document {}", settings.getInput());
        Logging.getLogger().log(Level.ERROR, "(Just leave it blank if you want to skip this document!)");
        return scanner.nextLine();
    }


}
