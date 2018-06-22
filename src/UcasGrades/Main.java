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

    private static final File LOGIN_CACHE_FILE = new File("cache/Login.cache");
    private static final File MARKS_SITE_CACHE_FILE = new File("cache/MarksSite.html");

    /**
     * main method which start the program.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        final int option = JOptionPane.showConfirmDialog(null, "Do you want me to pop up when you get new grades ?");

        if (option != 0)// 0 = yes choice
        {
            if (option == 1) //1 = no choice
            {
                JOptionPane.showMessageDialog(null, "Thanks for using this program");
            }
            System.exit(0);
        }
        String username = null;
        String password = null;

        if (LOGIN_CACHE_FILE.exists()) {
            try (Scanner cacheScanner = new Scanner(LOGIN_CACHE_FILE)) {
                if (cacheScanner.hasNext()) {
                    username = cacheScanner.nextLine();
                }
                if (cacheScanner.hasNext()) {
                    password = cacheScanner.nextLine();
                }
                // if cacheScanner doesn't have next that means LOGIN_CACHE_FILE is empty or don't contain username or password
            } catch (FileNotFoundException e) {
                Utilities.printException("Exception in the Scanner of the cache file", e);
                System.exit(0);
            }
        } else {
            //reaching here means LOGIN_CACHE_FILE doesn't exist but we just need it's parent path to exist

            if (LOGIN_CACHE_FILE.getParentFile().mkdirs()) {
                // TODO make the cache directory empty
            } else {
                Utilities.printException("in LOGIN_CACHE_FILE mkdirs method", new Exception("Cannot create cache named directory"));
            }
        }

        if (username == null || password == null) {
            username = JOptionPane.showInputDialog("Enter your university ID");
            password = JOptionPane.showInputDialog("Enter your password");
            int keepLoginStateNumber = JOptionPane.showConfirmDialog(null, "Remember username & password ?");

            boolean keepLogin = keepLoginStateNumber == 0;//0 = yes choice

            if (username == null || password == null || keepLoginStateNumber == 2) {
                JOptionPane.showMessageDialog(null, "Sorry for asking you some private data but we can't see your grades without it.\n Thanks for using this program wish you'll change your mind later.");
                System.exit(0);
            } else if (keepLogin) {
                writeOnLoginCacheFile(username + "\n" + password);
            }
        }

        Authorization userAuthorization = Authorization.NOT_FOUND;
        while (userAuthorization != Authorization.ACCEPTED) {
            userAuthorization = UcasWeb.getAuthorized(username, password);
            if (userAuthorization == Authorization.DENIED) {
                JOptionPane.showMessageDialog(null, "You've Entered wrong ID or password !");
                break;
                //TODO re ask user for username and password
            } else if (userAuthorization == Authorization.NOT_FOUND) {
                try {
                    Thread.sleep(5_000);// 5 Seconds
                } catch (InterruptedException e) {
                    Utilities.printException("Exception in sleep NOT_FOUND Authorization", e);
                }
            }
        }

        String newMarksSite = "ф";
        if (!MARKS_SITE_CACHE_FILE.exists()) {
            writeOnMarksSite(newMarksSite);
        }
        while (newMarksSite.contains("ф")) {

            String oldMarksSite = getSavedMarksSite();

            StringBuilder newMarksSiteAsStringBuilder = new StringBuilder();
            Scanner s = new Scanner(UcasWeb.getMarksPage());
            while (s.hasNext()) {
                newMarksSiteAsStringBuilder.append(s.next());
            }
            newMarksSite = newMarksSiteAsStringBuilder.toString();

            if (oldMarksSite.contains("ф")) {
                writeOnMarksSite(newMarksSite);
                newMarksSite = "ф";
                continue;
            } else if (!oldMarksSite.equals(newMarksSite)) {
                JOptionPane.showMessageDialog(null, "Hey ! \n new grade has been submitted !!");
                writeOnMarksSite("ф" + newMarksSite);
                break;
            }
            try {
                Thread.sleep(60_000);// check every minute
            } catch (InterruptedException e) {
                Utilities.printException("InterruptedException in while (there isn't new mark) loop ", e);
            }
            newMarksSite = "ф";
        }
    }

    /**
     * write the username and password in the cache file
     *
     * @param text (id & password)
     */
    private static void writeOnLoginCacheFile(String text) {
        try (FileWriter fw = new FileWriter(LOGIN_CACHE_FILE)) {
            fw.append(text).flush();
        } catch (IOException e) {
            Utilities.printException("Exception in writing on cache file", e);
        }
    }

    /**
     * write new marks site instead of old one
     *
     * @param HTMLPageAsString to write in MARKS_SITE_CACHE_FILE
     */
    private static void writeOnMarksSite(String HTMLPageAsString) {
        try (FileWriter fw = new FileWriter(MARKS_SITE_CACHE_FILE)) {
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
            MarksSiteScanner = new Scanner(MARKS_SITE_CACHE_FILE);
        } catch (FileNotFoundException e) {
            Utilities.printException("FileNotFoundException in getMarksSite method", e);
            return "ф";
        }
        StringBuilder MarksSiteAsStringBuilder = new StringBuilder();
        while (MarksSiteScanner.hasNext()) {
            MarksSiteAsStringBuilder.append(MarksSiteScanner.next());
        }
        return MarksSiteAsStringBuilder.toString();
    }
}
