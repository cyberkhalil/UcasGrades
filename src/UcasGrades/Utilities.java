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
        System.out.println(s + "\n" + e.toString() + "\n" + e.getMessage());
    }
}
