/*
 * Copyright (C) 2019 khalil2535
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    private static String HOME = System.getProperty("user.home");
    private static String OS = System.getProperty("os.name", "generic").toLowerCase();
    private static File CACHE_FOLDER = null;

    /**
     * main method which start the program.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        int option = JOptionPane.showConfirmDialog(null,
                "Do you want ucasGrades to pop up when you get a new grade ?");

        if (option != 0) {   // 0 = yes choice
            if (option == 1) // 1 = no choice
                JOptionPane.showMessageDialog(null, "Thanks for using this program");
            return;
        }

        if (getCACHE_FOLDER() == null) {
            if (!createCACHE_FOLDER()) {
                JOptionPane.showMessageDialog(null, "Error : Can't create cache folder !" +
                        "\n Please report this error to github.com/khalil2535/UcasGrades");
                return;
            }
            if (!hideCACHE_FOLDER())
                JOptionPane.showMessageDialog(null, "Error : Can't create cache folder !\n"
                        + " Please report this error to github.com/khalil2535/UcasGrades");
        }

        Authorization userAuth = Authorization.NOT_FOUND;
        while (userAuth != Authorization.ACCEPTED) {
            if (getLOGIN_CACHE_FILE() != null) {
                if (fetchLoginFromLOGIN_CACHE_FILE(userAuth)) {
                    option = JOptionPane.showConfirmDialog
                            (null, "Continue as " + Authorization.username + " ?");
                    if (option == 0) {
                        userAuth = UcasWeb.getAuthorized(userAuth);
                        if (userAuth == Authorization.DENIED)
                            JOptionPane.showMessageDialog(null,
                                    "You've Entered wrong username or password !");
                        else if (userAuth == Authorization.ACCEPTED) break;
                        else continue;
                    }
                }
            }
            while (!requestUserNameAndPassword(userAuth)) {
                option = JOptionPane.showConfirmDialog(null, "Can't continue without username and password." +
                        "\n Do you want to enter the username and password again ?");
                if (option != 0) {   // 0 = yes choice
                    if (option == 1) // 1 = no choice
                        JOptionPane.showMessageDialog(null, "Thanks for using this program");
                    return;
                }
            }

            if (JOptionPane.showConfirmDialog(null, "Remember username & password ?") == 0) {
                if (!createLOGIN_CACHE_FILE(
                        Authorization.username + "\n"
                                + Encryption.encode(Authorization.password, Integer.parseInt(Authorization.username))))
                    JOptionPane.showMessageDialog(null, "Warning : didn't create login cache file");
            }

            userAuth = UcasWeb.getAuthorized(userAuth);
            if (userAuth == Authorization.DENIED)
                JOptionPane.showMessageDialog(null, "You've Entered wrong username or password !");
            else if (userAuth == Authorization.ACCEPTED) break;

            try {
                Thread.sleep(5_000);// 5 Seconds
            } catch (InterruptedException e) {
                Utilities.printException("Exception in sleep NOT_FOUND Authorization", e);
            }
        }

        // create first copy of MARKS_SITE_CACHE
        do {
            String newMarksSiteAsString = UcasWeb.getMarksPage();
            if (newMarksSiteAsString == null || newMarksSiteAsString.isEmpty()) {
                try {
                    Thread.sleep(5_000);
                    continue;
                } catch (InterruptedException e) {
                    Utilities.printException("From main method", e);
                }
            }
            writeOnMarksSite1(newMarksSiteAsString);
            break;
        }
        while (getMARKS_SITE_CACHE_FILE() == null);

        do {
            do {
                String newMarksSiteAsString = UcasWeb.getMarksPage();
                if (newMarksSiteAsString == null || newMarksSiteAsString.isEmpty()) {
                    try {
                        Thread.sleep(5_000);
                        continue;
                    } catch (InterruptedException e) {
                        Utilities.printException("From main method", e);
                    }
                }
                writeOnMarksSite2(newMarksSiteAsString);
                break;
            }
            while (getMARKS_SITE_CACHE_FILE2() == null);
            if (!getSavedMarksSite().equalsIgnoreCase(getSavedMarksSite2())) break;
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                Utilities.printException("From main method", e);
            }
        } while (true);

        JOptionPane.showMessageDialog(null, "There is a new Mark Detected !!");
        // TODO which mark is new (name : number) ?
    }

    private static boolean requestUserNameAndPassword(Authorization userAuth) {
        Authorization.username = JOptionPane.showInputDialog(null,
                "We need your Username to login to my.ucas.edu.ps (Student ID)");
        if (Authorization.username == null || Authorization.username.isEmpty()) return false;

        Authorization.password = JOptionPane.showInputDialog(null,
                "We need your Password to login to my.ucas.edu.ps (Student Password)");
        return Authorization.password != null && !Authorization.password.isEmpty();
    }

    private static boolean fetchLoginFromLOGIN_CACHE_FILE(Authorization userAuth) {
        if (getLOGIN_CACHE_FILE() != null)
            try (Scanner cacheScanner = new Scanner(getLOGIN_CACHE_FILE())) {
                if (cacheScanner.hasNext()) {
                    Authorization.username = cacheScanner.next();
                    if (cacheScanner.hasNext()) {
                        Authorization.password =
                                Encryption.decode(cacheScanner.next(), Integer.parseInt(Authorization.username));
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                Utilities.printException("Exception in the Scanner of the cache file", e);
            }
        return false;
    }

    private static File getCACHE_FOLDER() {
        if (CACHE_FOLDER != null) return CACHE_FOLDER;

        File file = new File(HOME + File.separator + ".ucasGrades");
        if (file.exists()) {
            CACHE_FOLDER = file;
        }

        return CACHE_FOLDER;
    }

    private static boolean hideCACHE_FOLDER() {
        try {
            if (OS.contains("win")) {
                Runtime.getRuntime().exec("attrib +H " + CACHE_FOLDER).waitFor();
                return true;
            } else return OS.contains("linux");
        } catch (IOException | InterruptedException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
        return false;
    }

    private static boolean createCACHE_FOLDER() {
        CACHE_FOLDER = new File(HOME + File.separator + ".ucasGrades");
        return CACHE_FOLDER.mkdir();
    }

    private static File getLOGIN_CACHE_FILE() {
        File file = new File(getCACHE_FOLDER().getPath() + File.separator + "Login.cache");
        if (file.exists()) return file;
        return null;
    }

    /**
     * write the username and password in the cache file
     *
     * @param text (id & password)
     */
    private static boolean createLOGIN_CACHE_FILE(String text) {
        File LOGIN_CACHE_FILE = new File(getCACHE_FOLDER().getPath() + File.separator + "Login.cache");
        try (FileWriter fw = new FileWriter(LOGIN_CACHE_FILE)) {
            fw.append(text).flush();
            return true;
        } catch (IOException e) {
            Utilities.printException("Exception in writing on cache file", e);
        }
        return false;
    }

    private static File getMARKS_SITE_CACHE_FILE() {
        File file = new File(getCACHE_FOLDER().getPath() + File.separator + "MarksSite.html");
        return file.exists() ? file : null;
    }

    private static File getMARKS_SITE_CACHE_FILE2() {
        File file = new File(getCACHE_FOLDER().getPath() + File.separator + "newMarksSite.html");
        return file.exists() ? file : null;
    }

    private static String getSavedMarksSite() {
        if (getMARKS_SITE_CACHE_FILE() == null) return null;
        Scanner MarksSiteScanner;
        try {
            MarksSiteScanner = new Scanner(getMARKS_SITE_CACHE_FILE()).useDelimiter("\\Z");
        } catch (FileNotFoundException e) {
            Utilities.printException("FileNotFoundException in getMarksSite method", e);
            return null;
        }
        String MarksSiteAsString = MarksSiteScanner.next();
        MarksSiteScanner.close();
        return MarksSiteAsString;
    }

    private static String getSavedMarksSite2() {
        if (getMARKS_SITE_CACHE_FILE2() == null) return null;
        Scanner MarksSiteScanner;
        try {
            MarksSiteScanner = new Scanner(getMARKS_SITE_CACHE_FILE2()).useDelimiter("\\Z");
        } catch (FileNotFoundException e) {
            Utilities.printException("FileNotFoundException in getMarksSite method", e);
            return null;
        }
        String MarksSiteAsString = MarksSiteScanner.next();
        MarksSiteScanner.close();
        return MarksSiteAsString;
    }

    private static boolean writeOnMarksSite1(String HTMLPageAsString) {
        File MARKS_SITE_CACHE_FILE = new File(getCACHE_FOLDER().getPath() + File.separator + "MarksSite.html");
        try (FileWriter fw = new FileWriter(MARKS_SITE_CACHE_FILE)) {
            fw.append(HTMLPageAsString).flush();
            return true;
        } catch (IOException e) {
            Utilities.printException("Exception in writeOnMarksSite method", e);
            return false;
        }
    }

    private static boolean writeOnMarksSite2(String HTMLPageAsString) {
        File MARKS_SITE_CACHE_FILE = new File(getCACHE_FOLDER().getPath() + File.separator + "newMarksSite.html");
        try (FileWriter fw = new FileWriter(MARKS_SITE_CACHE_FILE)) {
            fw.append(HTMLPageAsString).flush();
            return true;
        } catch (IOException e) {
            Utilities.printException("Exception in writeOnMarksSite method", e);
            return false;
        }
    }

}
