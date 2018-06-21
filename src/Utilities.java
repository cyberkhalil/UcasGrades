public final class Utilities {
    private Utilities() {
    }

    public static void printException(String s, Exception e) {
        System.out.println(s + "\n" + e.toString() + "\n" + e.getMessage());
    }
}
