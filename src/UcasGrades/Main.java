package UcasGrades;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Main class to start the program
 *
 * @author khalil2535
 */
public class Main {

    /**
     * main method which start the program.
     *
     * @param args
     */
    public static void main(String[] args) {
        final int option = JOptionPane.showConfirmDialog(null, "Do you want me to pop up when you get new grades ?");
        if (option == 0)// 0 = yes choice
        {
            String username = null;
            String password = null;
            boolean keepLogin = false;
            Scanner cacheScanner = null;

            File cache = new File("cache/cache");
            if (cache.exists()) {
                try {
                    cacheScanner = new Scanner(new File("cache/cache"));
                } catch (FileNotFoundException e) {
                    Utilities.printException("Exception in the Scanner of the cache file", e);
                }
                if (cacheScanner != null && cacheScanner.hasNext()) {
                    username = cacheScanner.nextLine();
                    password = cacheScanner.nextLine();
                    cacheScanner.close();
                }
            } else {
                username = JOptionPane.showInputDialog("Enter your university ID");
                password = JOptionPane.showInputDialog("Enter your password");
                keepLogin = JOptionPane.showConfirmDialog(null, "Remember username & password ?") == 0;//0 = yes choice
            }
            cache.getParentFile().mkdirs();

            Authorization authorization = Authorization.NOT_FOUND;
            while (authorization != Authorization.ACCEPTED) {

                authorization = UcasWeb.getAuthorized(username, password);

                if (authorization == Authorization.DENIED) {
                    JOptionPane.showMessageDialog(null, "You've Entered wrong ID or password !");
                    writeOnCacheFile("");
                    break;
                    //TODO reask user for username and password
                } else if (authorization == Authorization.NOT_FOUND) {
                    try {
                        Thread.sleep(5_000);// 5 Seconds
                    } catch (InterruptedException e) {
                        Utilities.printException("Exception in sleep NOT_FOUND Authorization", e);
                    }
                }
            }

            if (keepLogin) {
                writeOnCacheFile(username + "\n" + password);
            }

            String newMarksSite = "ф";
            if (!new File("cache/MarksSite.html").exists()) {
                writeOnMarksSite(newMarksSite);
            }
            while (newMarksSite.contains("ф")) {

                String oldMarksSite = getSavedMarksSite();

                newMarksSite = "";
                Scanner s = new Scanner(UcasWeb.getMarksPage());
                while (s.hasNext()) {
                    newMarksSite += s.next();
                }

                if (oldMarksSite.contains("ф")) {
                    writeOnMarksSite(newMarksSite);
                    continue;
                } else if (!oldMarksSite.equals(newMarksSite)) {
                    JOptionPane.showMessageDialog(null, "Hey ! \n new grade has been submitted !!");
                    writeOnMarksSite(newMarksSite);
                    break;
                }
                try {
                    Thread.sleep(60_000);// check every minute
                } catch (InterruptedException e) {
                    Utilities.printException("InterruptedException in while (there isn't new mark) loop ", e);
                }
                newMarksSite = "ф";
            }

        } else if (option == 1) //1 = no choice
        {
            JOptionPane.showMessageDialog(null, "Thanks for using this program");
        }
    }

    /**
     * write the username and password in the cache file
     *
     * @param text
     */
    private static void writeOnCacheFile(String text) {
        try (FileWriter fw = new FileWriter("cache/cache")) {
            fw.append(text).flush();
        } catch (IOException e) {
            Utilities.printException("Exception in writing on cache file", e);
        }
    }

    /**
     * write new marks site instead of old one
     *
     * @param HTMLPageAsString
     */
    private static void writeOnMarksSite(String HTMLPageAsString) {
        try (FileWriter fw = new FileWriter("cache/MarksSite.html")) {
            fw.append(HTMLPageAsString).flush();
        } catch (IOException e) {
            Utilities.printException("Exception in writeOnMarksSite method", e);
        }
    }

    /**
     * method to get the saved old marks site
     *
     * @return Old marks site as HTML
     */
    private static String getSavedMarksSite() {
        Scanner MarksSiteScanner;
        try {
            MarksSiteScanner = new Scanner(new File("cache/MarksSite.html"));
        } catch (FileNotFoundException e) {
            Utilities.printException("FileNotFoundException in getMarksSite method", e);
            return "ф";
        }
        String MarksSiteAsString = "";
        while (MarksSiteScanner.hasNext()) {
            MarksSiteAsString += MarksSiteScanner.next();
        }
        return MarksSiteAsString;
    }
}
