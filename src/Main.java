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
    public static void main(String[] args) {
        final int option = JOptionPane.showConfirmDialog(null, "Do you want me to pop up when you get new grades ?");
        if (option == 0)// 0 = yes choice
        {
            String username = null;
            String password = null;
            boolean keepLogin = false;
            Scanner cacheScanner = null;

            File cache = new File("src/cache");
            if (cache.exists()) {
                try {
                    cacheScanner = new Scanner(new File("src/cache"));
                } catch (FileNotFoundException e) {
                    Utilities.printException("Exception in the Scanner of the cache file", e);
                }
                if (cacheScanner.hasNext()) {
                    username = cacheScanner.nextLine();
                    password = cacheScanner.nextLine();
                    cacheScanner.close();
                }
            } else {
                username = JOptionPane.showInputDialog("Enter your university ID");
                password = JOptionPane.showInputDialog("Enter your password");
                keepLogin = JOptionPane.showConfirmDialog(null, "Remember username & password ?") == 0;//0 = yes choice
            }
            Authorization authorization = Authorization.NOT_FOUND;
            while (authorization != Authorization.ACCEPTED) {

                authorization = UcasWeb.getAuthorized(username, password);

                if (authorization == Authorization.DENIED) {
                    JOptionPane.showMessageDialog(null, "You've Entered wrong ID or password !");
                    writeOnCacheFile("");
                    break;
                    //TODO reasgk user for username and password
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
            if (!new File("src/MarksSite.html").exists())
                writeOnMarksSite(newMarksSite);
            while (newMarksSite.contains("ф")) {
                System.out.println("waiting really");
                newMarksSite = UcasWeb.getMarksPage();
                if (newMarksSite.contains("ф")) continue;
                else if (getSavedMarksSite().contains("ф")) {
                    writeOnMarksSite(newMarksSite);
                    continue;
                } else if (!getSavedMarksSite().equals(newMarksSite)) {
                    JOptionPane.showMessageDialog(null, "Hey ! \n new grade has been submitted !!");
                    writeOnMarksSite(newMarksSite);
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


    private static void writeOnCacheFile(String text) {
        try {
            FileWriter fw = new FileWriter("src/cache");
            fw.append(text).flush();
            fw.close();
        } catch (IOException e) {
            Utilities.printException("Exception in writing on cache file", e);
        }
    }

    private static void writeOnMarksSite(String HTMLPageAsString) {
        try {
            FileWriter fw = new FileWriter("src/MarksSite.html");
            fw.append(HTMLPageAsString).flush();
            fw.close();
        } catch (IOException e) {
            Utilities.printException("Exception in writeOnMarksSite method", e);
        }
    }

    private static String getSavedMarksSite() {
        Scanner MarksSiteScanner = null;
        try {
            MarksSiteScanner = new Scanner(new File("src/MarksSite.html"));
        } catch (FileNotFoundException e) {
            Utilities.printException("FileNotFoundException in getMarksSite method", e);
            return "ф";
        }
        StringBuilder MarksSiteAsString = new StringBuilder();
        while (MarksSiteScanner.hasNext())
            MarksSiteAsString.append(MarksSiteScanner.nextLine());
        return MarksSiteAsString.toString();
    }
}
