package UcasGrades;

import ciphers.Encryption;

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

    private static File CACHE_FOLDER = new File("cache");

    private static File getCACHE_FOLDER() {
        return getCacheFolder();
    }

    private static void makeCashFolderHidden() {
        String OS = System.getProperty("os.name", "generic").toLowerCase();
        if (OS.contains("win")) {
            try {
                Process p = Runtime.getRuntime().exec("attrib +H " + getCACHE_FOLDER().getParent());
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                Utilities.printException("error in making folder cache hidden", e);
            }
        } else {
            if (new File(".cache").exists()) {
                if (!getCacheFolder().delete()) {
                    Utilities.printException("in makeCashFolderHidden method", new Exception("can't remove cache folder"));
                }
                setCacheFolder(new File(".cache"));

            } else {
                if (getCacheFolder().renameTo(new File("." + getCacheFolder().getPath()))) {
                    setCacheFolder(new File("." + getCacheFolder().getPath()));
                } else {
                    Utilities.printException("Error in make cache folder hidden", new Exception("there is no any "
                            + "folder named .cache but can't rename cache folder to .cache"));
                }
            }
        }
    }

    private static File getLOGIN_CACHE_FILE() {
        if (!getCACHE_FOLDER().isHidden()) makeCashFolderHidden();
        return new File(getCacheFolder().getPath() + File.separator + "UCAS Grades" + File.separator
                + "Login.cache");
    }

    private static File getMARKS_SITE_CACHE_FILE() {
        if (!getCACHE_FOLDER().isHidden()) makeCashFolderHidden();
        return new File(getCacheFolder().getPath() + File.separator + "UCAS Grades" + File.separator
                + "MarksSite.html");
    }

    /**
     * main method which start the program.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        final int option = JOptionPane.showConfirmDialog(null, "Do you want me to pop up when"
                + " you get new grades ?");

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

        if (getLOGIN_CACHE_FILE().exists()) {
            try (Scanner cacheScanner = new Scanner(getLOGIN_CACHE_FILE())) {
                if (cacheScanner.hasNext()) {
                    username = cacheScanner.next();
                    if (cacheScanner.hasNext()) {
                        password = Encryption.decode(cacheScanner.next(), Integer.parseInt(username));
                    }
                }
            } catch (FileNotFoundException e) {
                Utilities.printException("Exception in the Scanner of the cache file", e);
                System.exit(0);
            }
        } else {
            //reaching here means LOGIN_CACHE_FILE doesn't exist but we just need it's parent path to exist
            if (getCACHE_FOLDER().mkdirs()) {
                makeCashFolderHidden();
            } else {
                Utilities.printException("in getCACHE_FOLDER mkdirs or makeCashFolderHidden ", new Exception("Cannot create cache named directory"));
            }
        }
        if (username == null || password == null) {
            username = JOptionPane.showInputDialog("Enter your university ID");
            if (username == null) {
                JOptionPane.showMessageDialog(null, "Sorry for asking you some private data but we can't see your grades without it.\n Thanks for using this program wish you'll change your mind later.");
                System.exit(0);
            }
            password = JOptionPane.showInputDialog("Enter your password");
            if (password == null) {
                JOptionPane.showMessageDialog(null, "Sorry for asking you some private data but we can't see your grades without it.\n Thanks for using this program wish you'll change your mind later.");
                System.exit(0);
            }
            int keepLoginStateNumber = JOptionPane.showConfirmDialog(null, "Remember username & password ?");
            if (keepLoginStateNumber == 2) {
                JOptionPane.showMessageDialog(null, "Sorry for asking you some private data but we can't see your grades without it.\n Thanks for using this program wish you'll change your mind later.");
                System.exit(0);
            } else if (keepLoginStateNumber == 0) {
                writeOnLoginCacheFile(username + "\n" + Encryption.encode(password, Integer.parseInt(username)));
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
        if (!getMARKS_SITE_CACHE_FILE().exists()) {
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
        File LOGIN_CACHE_FILE = getLOGIN_CACHE_FILE();
        if (!LOGIN_CACHE_FILE.getParentFile().exists()) {
            LOGIN_CACHE_FILE.getParentFile().mkdir();
        }
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
        File MARKS_SITE_CACHE_FILE = getMARKS_SITE_CACHE_FILE();
        if (!MARKS_SITE_CACHE_FILE.getParentFile().exists()) {
            MARKS_SITE_CACHE_FILE.getParentFile().mkdir();
        }
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
            MarksSiteScanner = new Scanner(getMARKS_SITE_CACHE_FILE());
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

    private static File getCacheFolder() {
        return CACHE_FOLDER;
    }

    private static void setCacheFolder(File cacheFolder) {
        CACHE_FOLDER = cacheFolder;
    }
}
