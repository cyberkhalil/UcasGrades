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

/**
 * this class for static methods that are useful to use in the application
 *
 * @author khalil2535
 */
final class Utilities {

    private Utilities() {
    }

    /**
     * this method to print exceptions.
     *
     * @param s message to print before printing the exception and it's status.
     * @param e the Exception to print it's stack and it's message.
     */
    static void printException(String s, Exception e) {
        System.out.println(s + "\n" + e);
    }
}
