package de.uni_hannover.se.pdfzensor.censor.utils;


import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.config.Settings;
import org.apache.logging.log4j.Level;

import java.util.Scanner;

//TODO: add comments
public final class PasswordUtil {

    private PasswordUtil(){
        //
    }

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

    public static String getPasswordFromCL(Settings settings){
        Scanner scanner = new Scanner(System.in);
        Logging.getLogger().log(Level.ERROR, "Please enter the password for encrypted document {}", settings.getInput());
        Logging.getLogger().log(Level.ERROR, "(Just leave it blank if you want to skip this document!)");
        return scanner.nextLine();
    }


}
