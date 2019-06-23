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
package ciphers;

/**
 * A Java implementation of Caesar Cipher. /It is a type of substitution cipher
 * in which each letter in the plain text is replaced by a letter some fixed
 * number of positions down the alphabet. /
 *
 * @author khalil2535
 */
public class Encryption {

    /**
     * Encrypt text by shifting every Latin char by add number shift for ASCII
     * Example : A + 1 -> B
     *
     * @param message plain text
     * @param shift   number of shifts
     * @return Encrypted message
     */
    public static String encode(String message, int shift) {
        StringBuilder encoded = new StringBuilder();

        while (shift >= 26) { // 26 = number of latin letters
            shift -= 26;
        }

        final int length = message.length();
        for (int i = 0; i < length; i++) {

//            int current = message.charAt(i); //using char to shift characters because ascii is in-order latin alphabet
            char current = message.charAt(i); // Java law : char + int = char

            if (IsCapitalLatinLetter(current)) {

                current += shift;
                encoded.append((char) (current > 'Z' ? current - 26 : current)); // 26 = number of latin letters

            } else if (IsSmallLatinLetter(current)) {

                current += shift;
                encoded.append((char) (current > 'z' ? current - 26 : current)); // 26 = number of latin letters

            } else {
                encoded.append(current);
            }
        }
        return encoded.toString();
    }

    /**
     * Decrypt message by shifting back every Latin char to previous the ASCII
     * Example : B - 1 -> A
     *
     * @param encryptedMessage message want to decrypt
     * @param shift            number of shifts
     * @return message
     */
    public static String decode(String encryptedMessage, int shift) {
        StringBuilder decoded = new StringBuilder();

        while (shift >= 26) { // 26 = number of latin letters
            shift -= 26;
        }

        final int length = encryptedMessage.length();
        for (int i = 0; i < length; i++) {
            char current = encryptedMessage.charAt(i);
            if (IsCapitalLatinLetter(current)) {

                current -= shift;
                decoded.append((char) (current < 'A' ? current + 26 : current));// 26 = number of latin letters

            } else if (IsSmallLatinLetter(current)) {

                current -= shift;
                decoded.append((char) (current < 'a' ? current + 26 : current));// 26 = number of latin letters

            } else {
                decoded.append(current);
            }
        }
        return decoded.toString();
    }

    /**
     * @param c the character we want to check
     * @return true if character is capital Latin letter or false for others
     */
    private static boolean IsCapitalLatinLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    /**
     * @param c the character we want to check
     * @return true if character is small Latin letter or false for others
     */
    private static boolean IsSmallLatinLetter(char c) {
        return c >= 'a' && c <= 'z';
    }
}
