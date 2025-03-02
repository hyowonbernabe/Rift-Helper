package model;

public class CurrentDirectory {
    public static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
}
