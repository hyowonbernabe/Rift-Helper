package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LCUAuth {
    public static String port;
    public static String token;

    public static boolean getLCUAuth() {
        try {
            Process process = Runtime.getRuntime().exec("wmic PROCESS WHERE name='LeagueClientUx.exe' GET commandline");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("--app-port=") && line.contains("--remoting-auth-token=")) {
                    System.out.println("League Cleint Detected!");

                    port = line.replaceAll(".*--app-port=([0-9]+).*", "$1");
                    token = line.replaceAll(".*--remoting-auth-token=([\\w-]+).*", "$1");

                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving LCU authentication.");
            e.printStackTrace();
        }
        return false;
    }
}
